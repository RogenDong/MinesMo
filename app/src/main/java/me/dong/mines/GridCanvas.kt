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

/**
 * 绘制栅格
 */
@Composable
fun GridCanvas(col: Int = 13, row: Int = 28, modifier: Modifier = Modifier) {
    val textStyle = TextStyle(fontSize = 18.sp)
    val txtMeasurer = rememberTextMeasurer()
    // 未开单位颜色
//    val COLOR_CELL_HIDDEN = Pair(
//        Color(0xFFAE92FA),
//        Color(0xFF9E82F0)
//    )

    Canvas(modifier = modifier.fillMaxSize()) {
        //region 计算宽高
        val cs = round(size.width / (col + 1))
        val boxWidth = round(cs * col) // 盒子宽度
        val cellSize = Size(cs, cs) // 格子宽度
        val boxSize = Size(boxWidth, round(cs * row))// 盒子大小
        val boxOffset = Offset(round(cs / 2), round(cs * 2f))// 盒子偏移量
        val txtOffset = Offset(round(cs / 3.5f), round(cs / 30))// 文本偏移量
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
        }// translate(boxOffset.x, boxOffset.y)
        //endregion
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
