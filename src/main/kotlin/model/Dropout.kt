package com.ertools.model

import com.ertools.common.Matrix

class Dropout(
    factor: Double
): Layer<Array<Double>, Array<Double>>(1) {
    override fun initialize() {
        size = previousLayer!!.size
    }

    override fun response(input: Array<Double>): Array<Double> {
        TODO("Not yet implemented")
    }

    override fun mseError(input: Array<Double>): Array<Double> {
        TODO()
    }
}