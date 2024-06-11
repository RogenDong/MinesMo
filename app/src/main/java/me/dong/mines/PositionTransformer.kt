package me.dong.mines

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.unit.IntOffset
import kotlin.math.floor
import kotlin.math.round

/**
 * 无效坐标
 */
val INVALID_OFFSET = IntOffset(-1, -1)

/**
 * （浮点数）坐标是否有效
 */
fun Offset.invalid() = isUnspecified
        || !isValid()
        || !isFinite
        || x < 0
        || y < 0

/**
 * （整数）坐标是否有效
 */
fun IntOffset.isValid() = x >= 0 && y >= 0

infix fun IntOffset.ne(other: IntOffset) = x != other.x || y != other.y

/**
 * 坐标转换工具
 */
class PositionTransformer(
    /** 行数 */
    row: Int,
    /** 列数 */
    col: Int,
    /** 偏移量 */
    val offset: Offset,
    /** 单元格宽度 */
    val cellSize: Float,
) {
    private val width = cellSize * col
    private val height = cellSize * row

    init {
        Log.d("pos-trans", "width: $width, height: $height")
    }

    /**
     * 获取单元格行列坐标（0开始）
     */
    fun colRow(pos: Offset): IntOffset {
        if (pos.isUnspecified || !pos.isValid()) return INVALID_OFFSET
        var (x, y) = pos - offset
        if (x < 0 || y < 0) return INVALID_OFFSET
        x -= round(x % cellSize)
        y -= round(y % cellSize)
        if (x > width || y > height) return INVALID_OFFSET
        x = round(x / cellSize)
        y = round(y / cellSize)
        if (x > width || y > height) return INVALID_OFFSET
        return IntOffset(x.toInt(), y.toInt())
    }

    /**
     * 获取目标位置所在单元格的起始坐标
     */
    fun cellPosition(pos: Offset): Offset {
        if (pos.isUnspecified || !pos.isValid()) return Offset.Unspecified
        val (x, y) = pos - offset
        if (x < 0 || x > width || y < 0 || y > height) return Offset.Unspecified
//        Log.d("pos-trans", "col,row = $cr")
//        val mod = Offset(
//            round(x % cellSize),
//            round(y % cellSize),
//        )
//        return pos - mod
        return Offset(
            pos.x - round(x % cellSize),
            pos.y - round(y % cellSize),
        )
    }
}
