package me.dong.mines.mines

import android.util.Log
import androidx.annotation.Keep

object Mines {
    val count get() = _count
    val col get() = _width
    val row get() = _height
    val status get() = _status
    private var initLogger = false
    private var _count = 10
    private var _width = 10
    private var _height = 10
    private val rs = MinesJNI()
    private val burstSet = HashSet<Int>()
    private var _status = GameStatus.ReadyNew
    private val rawMap = ArrayList<UByte>(255 * 255)

    /**
     * @param x 列
     * @param y 行
     * @return 下标
     */
    private fun index(x: Int, y: Int) = y * col + x

    /**
     * @param x 列
     * @param y 行
     * @return 单元数据
     */
    private operator fun List<UByte>.get(x: Int, y: Int) = Cell(this[y * col + x])

    /**
     * @param x  列
     * @param y  行
     * @param fn 更新操作
     */
    private fun update(x: Int, y: Int, fn: (Cell) -> Unit) {
        val i = index(x, y)
        val c = Cell(rawMap[i])
        fn(c)
        rawMap[i] = c.v
    }

    fun isBurst(x: Int, y: Int) = burstSet.contains(index(x, y))

    /**
     * 初始化地图
     *
     * @param count  地雷数
     * @param width  宽度/列数
     * @param height 高度/行数
     */
    fun newMap(count: Int, width: Int, height: Int) {
        if (!initLogger) {
            initLogger = true
            rs.initLogger()
        }
        _status = GameStatus.ReadyNew
        burstSet.clear()
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
        if (rawMap.isEmpty() || data.size != rawMap.size) {
            rawMap.clear()
            rawMap.addAll(data)
            return
        }
        for (i in data.indices) {
            val c = Cell(data[i])
            if (c.isMine() && c.isReveal() && _status == GameStatus.Playing) {
                _status = GameStatus.Exploded
                burstSet.add(i)
            }
            rawMap[i] = data[i]
        }
        isAllReveal()
    }

    /**
     * 获取单元格实例
     */
    operator fun get(x: Int, y: Int): Cell {
        return when (_status) {
            GameStatus.ReadyNew, GameStatus.ReadyRetry -> EMPTY_CELL
            else -> rawMap[x, y]
        }
    }

    fun readyNew() {
        _status = GameStatus.ReadyNew
        burstSet.clear()
    }

    /**
     * 洗牌，开始新回合
     */
    fun newGame(x: Int, y: Int) {
        _status = GameStatus.Playing
        burstSet.clear()
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
        _status = GameStatus.ReadyRetry
        burstSet.clear()
        rs.resetProgress()
        fetch()
    }

    /**
     * 切换标记
     */
    fun switchFlag(x: Int, y: Int) {
        if (_status != GameStatus.Playing) return
        rs.switchFlag(x, y)
        update(x, y, Cell::switchFlag)
    }

    /**
     * 揭开隐藏单元
     */
    fun reveal(x: Int, y: Int) {
        val c = rawMap[x, y]
        if (c.isMine()) {
            Log.d("app-jni", "reveal: burst at ($x,$y)")
            _status = GameStatus.Exploded
            burstSet.add(index(x, y))
//            rs.revealAllMines()
//            fetch()
            return
        }
        val count = rs.reveal(x, y)
        if (count == 0) return
        if (count > 1) {
            fetch()
            return
        }
        update(x, y, Cell::reveal)
        isAllReveal()
    }

    /**
     * 揭开周围一圈
     */
    fun revealAround(x: Int, y: Int) {
        val cfa = rs.countFlaggedAround(x, y)
        Log.d("app-jni", "count flagged around: $cfa")
        if (cfa == 0) return
        val cell = rawMap[x, y]
        Log.d("app-jni", "cell warn: ${cell.getWarn()}")
        if (cfa < cell.getWarn().toInt()) return

        val count = rs.revealAround(x, y)
        Log.d("app-jni", "count reveal: $count")
        if (count > 0) fetch()
        // TODO find burst (x,y)
        isAllReveal()
    }

    /**
     * 揭露所有地雷
     */
    fun revealAllMines() {
        _status = GameStatus.Exploded
//        _burst = INVALID_OFFSET
        rs.revealAllMines()
        fetch()
    }

    /**
     * 是否已揭露全部非地雷单位？
     */
    fun isAllReveal(): Boolean {
        val all = rs.isAllReveal()
        if (all) {
            _status = GameStatus.Swept
            burstSet.clear()
        }
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

    external fun initLogger()

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

    /** 统计周围标记数 */
    external fun countFlaggedAround(x: Int, y: Int): Int

    /**
     * 揭露所有地雷
     */
    external fun revealAllMines()

    /**
     * 是否已揭露全部非地雷单位？
     */
    external fun isAllReveal(): Boolean

    /** 统计全局标记数 */
    external fun countFlagged(): Int

    /**
     * 获取地图的格式化字符串
     */
    external fun formatString(): String?

    /** 获取状态数据格式化字符串 */
    external fun formatStatusString(): String?

    /**
     * 同步数据
     *
     * @param keepStatus 是否包含状态信息（是否揭露和标记）
     */
    external fun fetch(keepStatus: Boolean): ByteArray
}