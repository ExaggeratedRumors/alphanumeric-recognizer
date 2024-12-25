package com.ertools.operations

object Error {
    fun dmse(actual: Array<Double>, expected: Array<Double>): Array<Double> {
        return actual.mapIndexed { index, value ->
            val expectedValue = if(index >= expected.size) 0.0 else expected[index]
            2.0 * (value - expectedValue) / actual.size
        }.toTypedArray()
    }
}