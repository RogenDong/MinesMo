package me.dong.mines

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import me.dong.mines.mines.GameStatus
import me.dong.mines.mines.Mines
import kotlin.math.round

private const val TAG = "ui"

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
val COLOR_MINES_FLAGGED = Pair(
    Color(0xFF43B244),
    Color(0xFF33B134),
)

/** 隐藏地雷色 */
val COLOR_MINES_HIDDEN = Pair(
    Color(0xFFFF5050),
    Color(0xFFFE4040),
)

/** 引爆地雷色 */
val COLOR_MINES_BURST = Color.Black

/** 找错标记色 */
val COLOR_CELL_WRONG = Pair(
    Color(0xFF989898),
    Color(0xFF888888),
)

/** 标记色 */
val COLOR_CELL_FLAG = Pair(
    Color(0xFF29B7CB),
    Color(0xFF19A7CA),
)

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

val STYLE_WRONG = TextStyle(fontSize = 18.sp, color = Color.White)
val STYLE_TXT = TextStyle(fontSize = 18.sp)

private operator fun <V> Pair<V, V>.get(b: Int, f: Int) = if (b and 1 != f) first else second

/**
 * 绘制栅格
 */
@Composable
fun MinesCanvas(modifier: Modifier = Modifier) {
    val col = Mines.col
    val row = Mines.row
    var pts by remember { mutableStateOf(PositionTransformer(col, row, Offset.Zero, 0f)) }
//    var downPosition by remember { mutableStateOf(Offset.Unspecified) }
    var motionEvent by remember { mutableStateOf(MotionEvent.Idle) }
    var downColRow by remember { mutableStateOf(INVALID_OFFSET) }
    var downTime by remember { mutableLongStateOf(Long.MAX_VALUE) }
    var pressOn by remember { mutableStateOf(false) }
    val wrongTxtMeasurer = rememberTextMeasurer()
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
                    if (!it.position.invalid()) {
                        downColRow = pts.colRow(it.position)
                        downTime = System.currentTimeMillis()
                        // TODO 长按n秒后轻轻震动
                    }
                    it.consume()
                },
                onMove = {
                    motionEvent = MotionEvent.Move
//                    downPosition = Offset.Unspecified
                    downColRow = INVALID_OFFSET
                    downTime = Long.MAX_VALUE
                    pressOn = false
                    it.consume()
                },
                onUp = {
                    motionEvent = MotionEvent.Up
                    if (it.position.invalid()) {
                        downColRow = INVALID_OFFSET
                        downTime = Long.MAX_VALUE
                        pressOn = false
                    } else pressOn = System.currentTimeMillis() - downTime > 1000
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
                        //region: 画爆炸点
                        if (Mines.isBurst(x, y)) {
                            drawRect(
                                size = cellSize,
                                topLeft = cellOffset,
                                color = COLOR_MINES_BURST,
                            )
                            continue
                        }
                        //endregion
                        val cell = Mines[x, y]
                        //region: 通关时画出所有地雷
                        if (Mines.status == GameStatus.Swept && cell.isMine()) {
                            drawRect(
                                color = COLOR_MINES_FLAGGED[x, flag],
                                topLeft = cellOffset,
                                size = cellSize,
                            )
                            continue
                        }
                        //endregion
                        //region: 画未打开的单位
                        if (!cell.isReveal()) {
                            //region: 画标记单位
                            if (cell.isFlagged()) {
                                if (Mines.status == GameStatus.Exploded) {
                                    if (cell.isMine()) {
                                        //region: 画找到的地雷
                                        drawRect(
                                            size = cellSize,
                                            topLeft = cellOffset,
                                            color = COLOR_MINES_FLAGGED[x, flag],
                                        )
                                        //endregion
                                    } else {
                                        //region: 画找错的单位
                                        drawRect(
                                            size = cellSize,
                                            topLeft = cellOffset,
                                            color = COLOR_CELL_WRONG[x, flag],
                                        )
                                        drawText(
                                            textLayoutResult = wrongTxtMeasurer.measure(
                                                text = cell.getWarn().toString(),
                                                style = STYLE_WRONG,
                                            ),
                                            topLeft = cellOffset + txtOffset,
                                        )
                                        //endregion
                                    }
                                    continue
                                }
                                drawRect(
                                    size = cellSize,
                                    topLeft = cellOffset,
                                    color = COLOR_CELL_FLAG[x, flag],
                                )
                            }
                            //endregion
                            //region: 画未找到的地雷
                            else if (cell.isMine() && Mines.status == GameStatus.Exploded) {
                                drawRect(
                                    size = cellSize,
                                    topLeft = cellOffset,
                                    color = COLOR_MINES_HIDDEN[x, flag],
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
                        //endregion
                        //region: 画已打开单位的颜色
                        drawRect(
                            color = COLOR_CELL_REVEAL[x, flag],
                            topLeft = cellOffset,
                            size = cellSize,
                        )
                        //endregion
                        //region: 写地雷值
                        val v = cell.getWarn().toInt()
                        if (v == 0) continue
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
            }// for (y in 0..<row)
            //endregion
        }// translate(boxOffset.x, boxOffset.y)
        //endregion
        //region: 计算点击命中单元、绘制点击栅格
        if (!downColRow.isValid() || motionEvent != MotionEvent.Up) return@Canvas
        val (x, y) = downColRow
        when (Mines.status) {
            GameStatus.Playing -> {
                val c = Mines[x, y]
                if (c.isReveal()) Mines.revealAround(x, y)
                else if (pressOn) Mines.reveal(x, y)
                else Mines.switchFlag(x, y)
            }

            GameStatus.ReadyRetry -> {
                // TODO implement retry
            }

            GameStatus.ReadyNew -> {
                Log.i("MinesCanvas", "new game at(${x},${y})")
                Mines.newGame(x, y)
            }

            GameStatus.Swept, GameStatus.Exploded -> Mines.readyNew()
        }
        motionEvent = MotionEvent.Idle
        downColRow = INVALID_OFFSET
        downTime = Long.MAX_VALUE
        pressOn = false
        //endregion
    }
}
