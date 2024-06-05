package me.dong.mines

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
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

val COLOR_CELL_FLAG = Color(0xFF43B244)

/**
 * 已开单位颜色
 */
val COLOR_CELL_REVEAL = Pair(
    Color(0xFFF7E8AA),
    Color(0xFFF9D27D),
)

val STYLE_TXT = TextStyle(fontSize = 18.sp)

/**
 * 未开单位颜色
 */
val COLOR_CELL_HIDDEN = Pair(
    Color(0xFFAE92FA),
    Color(0xFF9E82F0)
)

/**
 * 绘制栅格
 */
@Composable
fun MinesCanvas(modifier: Modifier = Modifier) {
    val col = Mines.col
    val row = Mines.row
    var log by remember { mutableStateOf("???") }
    var posTrans by remember {
        mutableStateOf(
            PositionTransformer(
                col,
                row,
                Offset.Zero,
                0f
            )
        )
    }
    var downPosition by remember { mutableStateOf(Offset.Unspecified) }
    var downColRow by remember { mutableStateOf(INVALID_OFFSET) }
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
    val txtMeasurer = rememberTextMeasurer()
    val drawModifier = modifier
        .fillMaxSize()
        // 指针/手势事件
        .pointerInput(Unit) {
            detectMotionEvents(
                onDown = {
                    motionEvent = MotionEvent.Down
                    if (!it.position.isUnspecified) {
                        downPosition = posTrans.cellPosition(it.position)
                        downColRow = posTrans.colRow(downPosition)
                        log = "${downColRow.x},${downColRow.y}"
                    }
                    it.consume()
                },
                onMove = {
                    motionEvent = MotionEvent.Move
//                    log = "move " + it.position
//                    movePosition = it.position
                    it.consume()
                },
                onUp = {
                    motionEvent = MotionEvent.Up
                    if (!it.position.isUnspecified) {
                        val upPos = posTrans.cellPosition(it.position)
                        val upColRow = posTrans.colRow(upPos)
                        if (upColRow ne downColRow)
                            downColRow = INVALID_OFFSET
                        log = "${upColRow.x},${upColRow.y}"
                    }
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
        if (boxOffset != posTrans.offset || cs != posTrans.cellSize) {
            posTrans = PositionTransformer(row, col, boxOffset, cs)
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
                        val cell = Mines.get(x, y)
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
                            //region: 画行列数
                            if (x < 1) {
                                drawText(
                                    textLayoutResult = txtMeasurer.measure(
                                        text = (y % 10).toString(),
                                        style = STYLE_TXT,
                                    ),
                                    topLeft = cellOffset + txtOffset,
                                )
                            } else if (y < 1) {
                                drawText(
                                    textLayoutResult = txtMeasurer.measure(
                                        text = (x % 10).toString(),
                                        style = STYLE_TXT,
                                    ),
                                    topLeft = cellOffset + txtOffset,
                                )
                            }
                            //endregion
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
            MotionEvent.Down, MotionEvent.Move -> {
                drawRect(
                    size = cellSize,
                    topLeft = downPosition,
                    color = COLOR_CELL_FLAG,
                )
            }

            MotionEvent.Up -> {
                motionEvent = MotionEvent.Idle
                if (downColRow.isValid()) {
//                        if (Mines.playing) {
//                            Log.i("GridCanvas", "reveal(${downColRow.x},${downColRow.y})")
//                            Mines.reveal(downColRow.x, downColRow.y)
//                        } else {
//                            Log.i("GridCanvas", "new game at(${downColRow.x},${downColRow.y})")
//                            Mines.newGame(downColRow.x, downColRow.y)
//                        }
                    downColRow = INVALID_OFFSET
                }
            }

            else -> Unit
        }
        //endregion
    }
    Text(
        text = log,
        fontSize = 24.sp,
        color = Color.White,
        modifier = Modifier.padding(top = 30.dp, start = 20.dp)
    )
}

/*// 绘制透明块时使用以下方式
with(drawContext.canvas.nativeCanvas) {
    val checkPoint = saveLayer(null, null)
    drawRect(
        size = cellSize,
        color = Color.Transparent,
        blendMode = BlendMode.Clear,
    )
    restoreToCount(checkPoint)
}*/
