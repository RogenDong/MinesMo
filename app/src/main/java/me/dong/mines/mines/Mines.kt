@file:OptIn(ExperimentalUnsignedTypes::class)

package me.dong.mines.mines

import android.util.Log
import androidx.annotation.Keep

object Mines {
    val count get() = _count
    val col get() = _width
    val row get() = _height
    val playing get() = _playing
    private var _playing = false
    private var _count: Int = 10
    private var _width: Int = 10
    private var _height: Int = 10
    private val rs = MinesJNI()
    private val rawMap = ArrayList<UByte>(255 * 255)

    /**
     * @param x 列
     * @param y 行
     * @return 下标
     */
    private fun index(x: Int, y: Int): Int {
        return y * col + x
    }

    private operator fun List<UByte>.get(x: Int, y: Int): Cell {
        return Cell(this[y * col + x])
    }

    private fun update(x: Int, y: Int, fn: (Cell) -> Unit) {
        val i = index(x, y)
        val c = Cell(rawMap[i])
        fn(c)
        rawMap[i] = c.v
    }

    /**
     * 初始化地图
     *
     * @param count  地雷数
     * @param width  宽度/列数
     * @param height 高度/行数
     */
    fun newMap(count: Int, width: Int, height: Int) {
        _playing = false
        this._count = count
        this._width = width
        this._height = height
        rs.init(count, width, height)
    }

    /**
     * 同步数据
     *
     * @param keepStatus 是否包含状态信息（是否揭露、标记）
     */
    private fun fetch(keepStatus: Boolean = true) {
        val data = rs.fetch(keepStatus).drop(2).map(Byte::toUByte)
        if (rawMap.isNotEmpty() && data.size == rawMap.size) {
            for (i in data.indices) rawMap[i] = data[i]
            return
        }
        rawMap.clear()
        rawMap.addAll(data)
    }

    /**
     * 获取单元格实例
     */
    fun get(x: Int, y: Int): Cell {
        if (!_playing) return EMPTY_CELL
        return rawMap[x, y]
    }

    /**
     * 洗牌，开始新回合
     */
    fun newGame(x: Int, y: Int) {
        _playing = true
        rs.newGame(x, y)
        val count = rs.revealAround(x, y)
        fetch()
        Log.i("app-jni", "newGame: count reveal cells: $count")
        Log.d("app-jni", formatString())
    }

    /**
     * 重置进度：清除开关、标记状态
     */
    fun resetProgress() {
        rs.resetProgress()
        fetch()
    }

    /**
     * 切换标记
     */
    fun switchFlag(x: Int, y: Int) {
        if (!_playing) return
        rs.switchFlag(x, y)
        update(x, y, Cell::switchFlag)
    }

    /**
     * 揭开隐藏单元
     */
    fun reveal(x: Int, y: Int) {
        val count = rs.reveal(x, y)
        if (count > 1) {
            fetch()
            return
        }
        update(x, y, Cell::reveal)
//        isAllReveal()
    }

    /**
     * 揭开周围一圈
     */
    fun revealAround(x: Int, y: Int) {
        val count = rs.revealAround(x, y)
        if (count > 0) fetch()
        isAllReveal()
    }

    /**
     * 揭露所有地雷
     */
    fun revealAllMines() {
        _playing = false
        rs.revealAllMines()
        fetch()
    }

    /**
     * 是否已揭露全部非地雷单位？
     */
    fun isAllReveal(): Boolean {
        val all = rs.isAllReveal()
        _playing = !all
        return all
    }

    /**
     * 获取地图的格式化字符串
     */
    fun formatString(): String {
        return rs.formatString() ?: ""
    }
}

@Keep
class MinesJNI {
    companion object {
        init {
            System.loadLibrary("mmrs")
        }
    }

    /**
     * 初始化地图大小
     *
     * @param count  地雷数
     * @param width  宽度/列数
     * @param height 高度/行数
     */
    external fun init(count: Int, width: Int, height: Int)

    /**
     * 洗牌，开始新回合
     * @param x 开始位置
     * @param y 开始位置
     */
    external fun newGame(x: Int, y: Int)

    /**
     * 重置进度：清除开关、标记状态
     */
    external fun resetProgress()

    /**
     * 切换标记
     */
    external fun switchFlag(x: Int, y: Int)

    /**
     * 揭开隐藏单元
     */
    external fun reveal(x: Int, y: Int): Int

    /**
     * 揭开周围一圈
     */
    external fun revealAround(x: Int, y: Int): Int

    /**
     * 揭露所有地雷
     */
    external fun revealAllMines()

    /**
     * 是否已揭露全部非地雷单位？
     */
    external fun isAllReveal(): Boolean

    /**
     * 获取地图的格式化字符串
     */
    external fun formatString(): String?

    /**
     * 同步数据
     *
     * @param keepStatus 是否包含状态信息（是否揭露和标记）
     */
    external fun fetch(keepStatus: Boolean): ByteArray
}