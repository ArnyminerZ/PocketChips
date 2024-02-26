package utils

import kotlin.math.PI

/**
 * Converts a number of radians to degrees.
 */
fun Float.toDegrees() = this * 180f / PI.toFloat()

/**
 * Converts a number of degrees to radians.
 */
fun Float.toRadians() = this * PI.toFloat() / 180f
