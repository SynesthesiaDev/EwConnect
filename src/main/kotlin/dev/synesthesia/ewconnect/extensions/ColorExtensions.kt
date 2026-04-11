package dev.synesthesia.ewconnect.extensions

import java.awt.Color

private val hexRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex()

val Color.hex: String
    get() {
        return String.format("#%02x%02x%02x", this.red, this.green, this.blue)
    }

val String.isValidHexColor: Boolean
    get() = this.matches(hexRegex)

fun Color.tintWhite(factor: Float): Color {
    val r = (this.red + (255 - this.red) * factor).toInt()
    val g = (this.green + (255 - this.green) * factor).toInt()
    val b = (this.blue + (255 - this.blue) * factor).toInt()
    return Color(r, g, b, this.alpha)
}