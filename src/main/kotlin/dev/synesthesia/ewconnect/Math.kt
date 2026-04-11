package dev.synesthesia.ewconnect

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
    }
}