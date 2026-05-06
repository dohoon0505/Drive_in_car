import Foundation
import QuartzCore

/// 모노토닉 클럭 — NTP 보정/타임존 변경에 영향을 받지 않는다.
enum MonotonicClock {
    static func nowMs() -> Int64 {
        Int64(CACurrentMediaTime() * 1000)
    }
}

enum TimeFormat {
    static func raceTime(ms: Int64) -> String {
        let totalCs = ms / 10
        let cs = Int(totalCs % 100)
        let totalSec = totalCs / 100
        let sec = Int(totalSec % 60)
        let min = Int(totalSec / 60)
        return String(format: "%02d:%02d.%02d", min, sec, cs)
    }
}
