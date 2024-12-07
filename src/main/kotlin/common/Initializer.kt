package com.ertools.common

import kotlin.random.Random

object Initializer {
    fun random(maxAbs: Double = 0.01): Double {

        return (2 * Random.nextDouble() - 1) * maxAbs
    }
}