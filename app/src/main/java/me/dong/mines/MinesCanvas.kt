package me.dong.mines

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import me.dong.mines.mines.Mines
import kotlin.math.round

//val LIST_COLOR = arrayOf(
//    Color(0xFF43B244),
//    Color(0xFF20A162),
//    Color(0xFFAE92FA),
//    Color(0xFF9E82F0),
//    Color(0xFFF7E8AA),
//    Color(0xFFF9D27D),
//    Color(0xFF42A5F5),
//)

/** 正确标记地雷色 */
val COLOR_MINES_FLAGGED = Color(0xFF43B244)

/** 隐藏地雷色 */
val COLOR_MINES_HIDDEN = Color(0xFFC864B4)

/** 引爆地雷色 */
val COLOR_MINES_BURST = Color(0xFFFF5050)

/** 标记色 */
val COLOR_CELL_FLAG = Color(0xFF29B7CB)

/** 已开单位颜色 */
val COLOR_CELL_REVEAL = Pair(
    Color(0xFFF7E8AA),
    Color(0xFFF9D27D),
)

/** 未开单位颜色 */
val COLOR_CELL_HIDDEN = Pair(
    Color(0xFFAE92FA),
    Color(0xFF9E82F0)
)

val STYLE_TXT = TextStyle(fontSize = 18.sp)

/**
 * 绘制栅格
 */
@Composable
fun MinesCanvas(modifier: Modifier = Modifier) {
    val col = Mines.col
    val row = Mines.row
    var pts by remember { mutableStateOf(PositionTransformer(col, row, Offset.Zero, 0f)) }
    var downPosition by remember { mutableStateOf(Offset.Unspecified) }
    var downColRow by remember { mutableStateOf(INVALID_OFFSET) }
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
    val txtMeasurer = rememberTextMeasurer()
    // 监听点击状态
    // - 按下时记录行列位置
    // - 移动时撤销记录
    // - 松开时检查位置，错位则撤销记录
    val drawModifier = modifier
        .fillMaxSize()
        // 指针/手势事件
        .pointerInput(Unit) {
            detectMotionEvents(
                onDown = {
                    motionEvent = MotionEvent.Down
                    if (!it.position.isUnspecified) {
                        downPosition = pts.cellPosition(it.position)
                        downColRow = pts.colRow(downPosition)
                    }
                    it.consume()
                },
                onMove = {
                    motionEvent = MotionEvent.Move
                    downPosition = Offset.Unspecified
                    downColRow = INVALID_OFFSET
                    it.consume()
                },
                onUp = {
                    motionEvent = MotionEvent.Up
                    it.consume()
                },
                delayAfterDownInMillis = 20L
            )
        }

    Canvas(modifier = drawModifier) {
        //region 计算宽高
        val cs = round(size.width / (col + 1))
        val boxWidth = round(cs * col) // 盒子宽度
        val cellSize = Size(cs, cs) // 格子宽度
        val boxSize = Size(boxWidth, round(cs * row))// 盒子大小
        val boxOffset = Offset(round(cs / 2), round(cs * 2f))// 盒子偏移量
        val txtOffset = Offset(round(cs / 3.5f), round(cs / 30))// 文本偏移量
        if (boxOffset != pts.offset || cs != pts.cellSize) {
            pts = PositionTransformer(row, col, boxOffset, cs)
        }
        //endregion
        //region 绘制栅格
        translate(boxOffset.x, boxOffset.y) {
            // 大色块垫底
            drawRect(color = COLOR_CELL_HIDDEN.first, size = boxSize)
            //region: 小色块画出栅格
            var flag = 0
            for (y in 0..<row) {
                translate(top = round(cs * y)) {
                    for (x in 0..<col) {
                        val cellOffset = Offset(round(cs * x), 0f)
                        val cell = Mines[x, y]
                        if (!cell.isReveal()) {
                            //region: 画标记单位
                            if (cell.isFlagged()) {
                                drawRect(
                                    size = cellSize,
                                    topLeft = cellOffset,
                                    color = COLOR_CELL_FLAG,
                                )
                            }
                            //endregion
                            //region: 画栅格
                            else if ((x and 1) != flag) {
                                drawRect(
                                    size = cellSize,
                                    topLeft = cellOffset,
                                    color = COLOR_CELL_HIDDEN.second,
                                )
                            }
                            //endregion
                            continue
                        }// if (no reveal && flagged)
                        //region: 画已打开单位
                        val color = if ((x and 1) != flag)
                            COLOR_CELL_REVEAL.first
                        else COLOR_CELL_REVEAL.second
                        drawRect(
                            topLeft = cellOffset,
                            size = cellSize,
                            color = color,
                        )
                        //endregion
                        //region: 写地雷值
                        val v = cell.getWarn().toInt()
                        if (v < 1) continue
                        drawText(
                            textLayoutResult = txtMeasurer.measure(
                                text = v.toString(),
                                style = STYLE_TXT,
                            ),
                            topLeft = cellOffset + txtOffset,
                        )
                        //endregion
                    }// for (x in 0..<col)
                }// translate(top = cs * y)
                flag = if (flag == 0) 1 else 0
            }//// for (y in 0..<row)
            //endregion
        }// translate(boxOffset.x, boxOffset.y)
        //endregion
        //region: 计算点击命中单元
        if (downPosition.isUnspecified || !downPosition.isValid()) return@Canvas
        when (motionEvent) {
            MotionEvent.Down -> {
                drawRect(
                    size = cellSize,
                    topLeft = downPosition,
                    color = COLOR_CELL_FLAG,
                )
            }

            MotionEvent.Up -> {
                if (downColRow.isValid()) {
                    val (x, y) = downColRow
                    if (Mines.playing) {
                        Log.i("MinesCanvas", "try reveal(${x},${y})")
                        Mines.reveal(x, y)
                    } else {
                        Log.i("MinesCanvas", "new game at(${x},${y})")
                        Mines.newGame(x, y)
                    }
                    downColRow = INVALID_OFFSET
                }
                motionEvent = MotionEvent.Idle
            }

            else -> Unit
        }
        //endregion
    }
}
