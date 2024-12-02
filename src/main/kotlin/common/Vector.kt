package com.ertools.common

class Vector(
    val size: Int,
    initializer: (Int) -> (Double) = { 0.0 }
) {
    var data: Array<Double> = Array<Double>(size) { 0.0 }
}