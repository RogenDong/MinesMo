package me.dong.mines

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.round

private val LS_COLOR = listOf(
    COLOR_CELL_REVEAL,
    COLOR_MINES_FLAGGED,
    COLOR_MINES_HIDDEN,
    COLOR_CELL_WRONG,
    COLOR_GUESS,
    COLOR_FORCE,
)

/** 绘制栅格 */
@Composable
fun GridCanvas(col: Int = 13, row: Int = 28) {
    val wrongTxtMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 18.sp)
    val txtMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        //region 计算宽高
        val cs = round(size.width / (col + 1))
        val boxWidth = round(cs * col) // 盒子宽度
        val cellSize = Size(cs, cs) // 格子宽度
        val boxSize = Size(boxWidth, round(cs * row))// 盒子大小
        val boxOffset = Offset(round(cs / 2), round(cs * 2f))// 盒子偏移量
        val txtOffset = Offset(round(cs / 3.5f), round(cs / 30))// 文本偏移量
        val flagOffset = Offset(round(cs / 9f), round(cs / 30))// 文本偏移量
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
                        val c = (if (y in 0..5) LS_COLOR[y] else COLOR_CELL_HIDDEN)[x, flag]
                        drawRect(
                            topLeft = cellOffset,
                            size = cellSize,
                            color = c,
                        )
                        //region base: 画栅格
//                        if ((x and 1) != flag) {
//                            drawRect(
//                                size = cellSize,
//                                topLeft = cellOffset,
//                                color = c,
//                            )
//                        }
                        //endregion
                        //region flagged: 标记符号
                        if (y == 1 && x > 0) {
                            drawText(
                                textLayoutResult = wrongTxtMeasurer.measure(
                                    style = STYLE_TXT,
                                    text = CHAR_FLAG,
                                ),
                                topLeft = cellOffset + flagOffset,
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
            }//endregion for (y in 0..<Mines.row)
        }//endregion translate(boxOffset.x, boxOffset.y)
    }
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
