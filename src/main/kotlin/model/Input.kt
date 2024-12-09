package com.ertools.model

import com.ertools.common.Matrix

class Input(
    private val inputHeight: Int,
    private val inputWidth: Int
): Layer() {
    /** API **/
    override fun initialize() {
        dimensions = Dimensions(
            height = inputHeight,
            width = inputWidth
        )
    }

    override fun response(input: Matrix): Matrix {
        return input.matrixFlatten().transpose()
    }

    override fun error(input: Matrix): Matrix {
        return input
    }
}