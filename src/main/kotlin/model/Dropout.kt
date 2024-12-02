package com.ertools.model

import com.ertools.common.Matrix

class Dropout(
    factor: Double
): Layer<Array<Double>> {
    override fun response(input: Array<Double>): Array<Double> {
        TODO("Not yet implemented")
    }

    override fun error(input: Array<Double>): Array<Double> {
        TODO()
    }
}