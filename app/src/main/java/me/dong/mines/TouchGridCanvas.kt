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
import kotlin.math.round

/**
 * 绘制栅格
 */
@Composable
fun TouchGridCanvas(col: Int, row: Int, modifier: Modifier = Modifier) {
    val textStyle = TextStyle(fontSize = 18.sp)
    // 未开单位颜色
//    val COLOR_CELL_HIDDEN = Pair(
//        Color(0xFFAE92FA),
//        Color(0xFF9E82F0)
//    )
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
                    if (!it.position.isUnspecified) {
                        downPosition = posTrans.cellPosition(it.position)
                        val tmp = posTrans.colRow(downPosition)
                        if (tmp != downColRow) {
                            log = "${tmp.x},${tmp.y}"
                            downColRow = tmp
                        }
                    }
                    it.consume()
                },
                onUp = {
                    motionEvent = MotionEvent.Idle
                    downColRow = INVALID_OFFSET
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
                        //region: 画栅格
                        if ((x and 1) != flag) {
                            drawRect(
                                size = cellSize,
                                topLeft = cellOffset,
                                color = COLOR_CELL_HIDDEN.second,
                            )
                        }
                        if (x == downColRow.x || y == downColRow.y) {
                            val (a, b) = COLOR_CELL_REVEAL
                            drawRect(
                                size = cellSize,
                                topLeft = cellOffset,
                                color = if ((x and 1) != flag) b else a
                            )
                        }
                        //endregion
                        //region: 画行列数
                        if (x < 1) {
                            drawText(
                                textLayoutResult = txtMeasurer.measure(
                                    text = (y % 10).toString(),
                                    style = textStyle,
                                ),
                                topLeft = cellOffset + txtOffset,
                            )
                        } else if (y < 1) {
                            drawText(
                                textLayoutResult = txtMeasurer.measure(
                                    text = (x % 10).toString(),
                                    style = textStyle,
                                ),
                                topLeft = cellOffset + txtOffset,
                            )
                        }
                        //endregion
                    }// for (x in 0..<Mines.col)
                }// translate(top = cs * y)
                flag = if (flag == 0) 1 else 0
            }// for (y in 0..<row)
            //endregion
        }// translate(boxOffset.x, boxOffset.y)
        //endregion
    }// canvas draw scope
    Text(
        text = log,
        fontSize = 24.sp,
        color = Color.White,
        modifier = Modifier.padding(top = 30.dp, start = 20.dp)
    )
}
