package me.dong.mines.mines

/** 位标识：已打开 */
const val BIT_REVEAL: UByte = 0x80u
/** 位标识：已标记 */
const val BIT_FLAG: UByte = 0x40u
/** 位标识：周围地雷数 */
const val BIT_WARN: UByte = 0x1Fu

val EMPTY_CELL = Cell(0u)

/** 扫雷单元状态 */
class Cell(var v: UByte = 0u) {

    /** 周围地雷数量 */
    fun getWarn(): UByte {
        return v and BIT_WARN
    }

    fun isReveal(): Boolean {
        return v >= BIT_REVEAL
    }

    fun isFlagged(): Boolean {
        return v and BIT_FLAG > 0u
    }

    fun isMine(): Boolean {
        return getWarn() > 8u
    }

    fun isEmpty(): Boolean {
        return getWarn() < 1u
    }

    /** 揭露该单元 */
    fun reveal() {
        v = v or BIT_REVEAL
    }

    /** 反转标记状态 */
    fun switchFlag() {
        v = v xor BIT_FLAG
    }

    override fun toString(): String {
        return "Cell{ f:${isFlagged()}, r:${isReveal()}, w:${getWarn()} }"
    }
}