package com.ertools.model

import com.ertools.common.Matrix

class Input(
    private val inputHeight: Int,
    private val inputWidth: Int
): Layer() {
    /** API **/
    override fun initialize() {
        require(nextLayer != null) { "E: Layer has not been bound correctly." }
        outputHeight = inputHeight
        outputWidth = inputWidth
    }

    override fun response(input: Matrix): Matrix {
        return nextLayer!!.response(input)
    }

    override fun error(input: Matrix): Matrix {
        return input
    }
}