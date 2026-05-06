package com.driveincar.core.time

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

interface MonotonicClock {
    /** 모노토닉 시각 (밀리초). NTP 보정/타임존에 영향을 받지 않는다. */
    fun nowMs(): Long
}

@Singleton
class SystemMonotonicClock @Inject constructor() : MonotonicClock {
    override fun nowMs(): Long = SystemClock.elapsedRealtime()
}

object TimeFormat {
    fun raceTime(ms: Long): String {
        val totalCs = ms / 10                              // 1/100초
        val cs = (totalCs % 100).toInt()
        val totalSec = totalCs / 100
        val sec = (totalSec % 60).toInt()
        val min = (totalSec / 60).toInt()
        return "%02d:%02d.%02d".format(min, sec, cs)
    }
}
