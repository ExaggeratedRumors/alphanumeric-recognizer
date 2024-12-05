package com.ertools.model

import com.ertools.common.Matrix


class Flatten : Layer() {
    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        outputHeight = 1
        outputWidth = previousLayer!!.outputWidth * previousLayer!!.outputHeight
    }

    override fun response(input: Matrix): Matrix {
        return input.matrixFlatten()
    }

    override fun error(input: Matrix): Matrix {
        return input.reconstructMatrix(previousLayer!!.outputHeight)
    }
}