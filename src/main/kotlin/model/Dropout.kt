package com.ertools.model

import com.ertools.common.Matrix

class Dropout(
    factor: Double
): Layer(1) {
    override fun initialize() {
        size = previousLayer!!.size
    }

    override fun response(input: Matrix): Matrix {
        TODO("Not yet implemented")
    }

    override fun error(input: Matrix): Matrix {
        TODO()
    }
}