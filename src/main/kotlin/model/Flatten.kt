package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.reconstructMatrix


class Flatten : Layer<Matrix, Array<Double>>(1) {
    override fun initialize() {
        size = previousLayer!!.size
    }

    override fun response(input: Matrix): Array<Double> {
        return input.flatten()
    }

    override fun mseError(input: Array<Double>): Matrix {
        return reconstructMatrix(input, previousLayer!!.size)
    }
}