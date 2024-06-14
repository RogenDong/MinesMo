package me.dong.mines

import android.util.Log

private const val TAG = "Watch"

class Watch {
    private var stop = true
    private var elapsed = 0
    private var start: Long = 0
//    private var tt = Timer

    fun start() {
        Log.d(TAG, "start: $start")
        start = System.currentTimeMillis()
        stop = false
    }

    fun reset() {
        start = 0
        elapsed = 0
        stop = true
    }

    fun stop() {
        Log.d(TAG, "stop")
        start = 0
        stop = true
    }

    fun elapsed(): Int {
//        Log.d(TAG, "elapsed")
        if (stop) return elapsed
        if (start == 0L) return 0
        if (elapsed > 999000) return 999000
        val now = System.currentTimeMillis()
        if (now < start) return 0
        elapsed = (now - start).toInt()
        return if (elapsed > 999000) 999000 else elapsed
    }
}

