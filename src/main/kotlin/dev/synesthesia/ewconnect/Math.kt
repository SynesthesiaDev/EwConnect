package dev.synesthesia.ewconnect

import kotlin.time.Duration.Companion.milliseconds

private const val ticksPerSecond = 20
private const val secondsPerMinute = 60
private const val secondsPerHour = 3600
private const val secondsPerDay = 86400

fun Int.ticksToReadable(): String {

    var remainingSeconds = this / ticksPerSecond

    val days = remainingSeconds / secondsPerDay
    remainingSeconds %= secondsPerDay

    val hours = remainingSeconds / secondsPerHour
    remainingSeconds %= secondsPerHour

    val minutes = remainingSeconds / secondsPerMinute
    val seconds = remainingSeconds % secondsPerMinute

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (seconds > 0 || this.isEmpty()) append("${seconds}s")
    }.trim()
}

fun Long.msToReadable(): String {
    if (this <= 0) return "0s"

    val duration = this.milliseconds

    return duration.toComponents { days, hours, minutes, seconds, _ ->
        buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (seconds > 0 || isEmpty()) append("${seconds}s")
        }.trim()
    }
}

fun Double.format(): String = "%.2f".format(this)