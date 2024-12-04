package com.ertools.common

object Error {
    fun dmse(actual: Array<Double>, expected: Array<Double>): Array<Double> {
        return actual.zip(expected).map { (a, e) -> 2.0 * (a - e) / actual.size  }.toTypedArray()
    }
}