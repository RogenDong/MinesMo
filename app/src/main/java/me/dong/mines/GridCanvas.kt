package me.dong.mines

import android.util.Log
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

val LIST_COLOR = arrayOf(
    Color(0xFF43B244),
    Color(0xFF20A162),
    Color(0xFFAE92FA),
    Color(0xFF9E82F0),
    Color(0xFFF7E8AA),
    Color(0xFFF9D27D),
    Color(0xFF42A5F5),
)

val COLOR_CELL_FLAG = LIST_COLOR[0]
/**
 * 未开单位颜色
 */
val COLOR_CELL_HIDDEN = Pair(LIST_COLOR[2], LIST_COLOR[3])

/**
 * 已开单位颜色
 */
val COLOR_CELL_REVEAL = Pair(LIST_COLOR[4], LIST_COLOR[5])

val STYLE_TXT = TextStyle(fontSize = 18.sp)
/**
 * 绘制栅格
 */
@Composable
fun GridCanvas(modifier: Modifier = Modifier) {
    var log by remember { mutableStateOf("???") }
    var posTrans by remember { mutableStateOf(PositionTransformer(Mines.col, Mines.row, Offset.Zero, 0f)) }
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
                        log = "down " + it.position
                    }
                    it.consume()
                },
                onMove = {
                    motionEvent = MotionEvent.Move
                    log = "move " + it.position
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
                        log = "up " + it.position
                    }
                    it.consume()
                },
                delayAfterDownInMillis = 20L
            )
        }

    Canvas(modifier = drawModifier) {
        try {
            //region 计算宽高
            val boxWidth = size.width * 0.9f // 盒子宽度
            val cs = boxWidth / Mines.col
            val cellSize = Size(cs, cs) // 格子宽度
            val boxSize = Size(boxWidth, cs * Mines.row)// 盒子大小
            val txtOffset = Offset(cs / 3, cs / 10)// 文本偏移量
            val boxOffset = Offset(size.width * 0.05f, 20 + cs * 2f)
            if (boxOffset != posTrans.offset || cs != posTrans.cellSize) {
                posTrans = PositionTransformer(Mines.row, Mines.col, boxOffset, cs)
            }
            //endregion
            //region 绘制栅格
            translate(boxOffset.x, boxOffset.y) {
                try {
                    // 大色块垫底
                    drawRect(color = COLOR_CELL_HIDDEN.first, size = boxSize)
                    // 小色块画出栅格
                    var flag = 0
                    for (y in 0..<Mines.row) {
                        translate(top = cs * y) {
                            for (x in 0..<Mines.col) {
                                try {
                                    val cellOffset = Offset(cs * x, 0f)
                                    val cell = Mines.get(x, y)
                                    //region: draw hidden tile
                                    if (!cell.isReveal()) {
                                        //region: draw flagged tile
                                        if (cell.isFlagged()) {
                                            drawRect(
                                                size = cellSize,
                                                topLeft = cellOffset,
                                                color = COLOR_CELL_FLAG,
                                            )
                                        }
                                        //endregion
                                        else if ((x and 1) != flag) {
                                            drawRect(
                                                size = cellSize,
                                                topLeft = cellOffset,
                                                color = COLOR_CELL_HIDDEN.second,
                                            )
                                        }
                                        continue
                                    }// if (!cell.isReveal())
                                    //endregion
                                    //region: draw reveal tile
                                    val color = if ((x and 1) != flag)
                                        COLOR_CELL_REVEAL.first
                                    else COLOR_CELL_REVEAL.second
                                    drawRect(
                                        topLeft = cellOffset,
                                        size = cellSize,
                                        color = color,
                                    )
                                    //endregion
                                    //region: draw text
                                    val v = cell.getWarn().toInt()
                                    if (v < 1) continue
                                    val measured = txtMeasurer.measure(
                                        text = v.toString(),
                                        style = STYLE_TXT,
                                    )
                                    drawText(
                                        textLayoutResult = measured,
                                        topLeft = cellOffset + txtOffset,
                                    )
                                    //endregion
                                } catch (e: Exception) {
                                    Log.e("GridCanvas", "draw cell($x,$y): ${e.localizedMessage}", e)
                                }
                            }// for (x in 0..<Mines.col)
                        }// translate(top = cs * y)
                        flag = if (flag == 0) 1 else 0
                    }// for (y in 0..<Mines.row)
                } catch (e: Exception) {
                    Log.e("GridCanvas", "draw grid: ${e.localizedMessage}", e)
                }
            }// translate(boxOffset.x, boxOffset.y)
            //endregion
            //region: 绘制点击命中单元
            if (!downPosition.isUnspecified && downPosition.isValid()) {
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
                            if (Mines.playing) {
                                Log.i("GridCanvas", "reveal(${downColRow.x},${downColRow.y})")
                                Mines.reveal(downColRow.x, downColRow.y)
                            } else {
                                Log.i("GridCanvas", "new game at(${downColRow.x},${downColRow.y})")
                                Mines.newGame(downColRow.x, downColRow.y)
                            }
                            downColRow = INVALID_OFFSET
                        }
                    }

                    else -> Unit
                }
            }
            //endregion
        } catch (e: Exception) {
            Log.e("GridCanvas", "Canvas root: ${e.localizedMessage}", e)
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
    }
    Text(
        text = log,
        fontSize = 24.sp,
        color = Color.White,
        modifier = Modifier.padding(top = 30.dp, start = 20.dp)
    )
}
