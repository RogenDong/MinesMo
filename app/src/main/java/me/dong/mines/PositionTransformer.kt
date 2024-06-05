package me.dong.mines

import android.util.Log
import androidx.compose.ui.geometry.Offset
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
fun Offset.isValid(): Boolean {
    return this.x >= 0 && this.y >= 0
}

/**
 * （整数）坐标是否有效
 */
fun IntOffset.isValid(): Boolean {
    return this.x >= 0 && this.y >= 0
}

infix fun IntOffset.ne(other: IntOffset): Boolean {
    return x != other.x || y != other.y
}

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
        val tmp = Offset(
            round((pos.x - offset.x) / cellSize),
            round((pos.y - offset.y) / cellSize),
        )
        if (tmp.x > width || tmp.y > height) return INVALID_OFFSET
        return IntOffset(tmp.x.toInt(), tmp.y.toInt())
    }

    /**
     * 获取目标位置所在单元格的起始坐标
     */
    fun cellPosition(pos: Offset): Offset {
        val cr = colRow(pos)
        if (!cr.isValid()) return Offset.Unspecified
//        Log.d("pos-trans", "col,row = $cr")
//        val mod = Offset(
//            round((pos.x - offset.x) % cellSize),
//            round((pos.y - offset.y) % cellSize),
//        )
//        return pos - mod
        return Offset(
            pos.x - round((pos.x - offset.x) % cellSize),
            pos.y - round((pos.y - offset.y) % cellSize),
        )
    }
}
