package me.dong.mines.mines

enum class GameStatus {
    /** 新开局 */ ReadyNew,
    /** 重新玩 */ ReadyRetry,
    /** 正在玩 */ Playing,
    /** 已爆炸 */ Exploded,
    /** 已扫清 */ Swept,
}