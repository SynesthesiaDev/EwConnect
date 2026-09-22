package dev.synesthesia.ewconnect.utils

import kotlin.random.Random

fun randomFloat(min: Float, max: Float): Float {
    return min + Random.nextFloat() * (max - min)
}