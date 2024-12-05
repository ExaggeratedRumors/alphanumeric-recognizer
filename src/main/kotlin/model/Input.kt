package com.ertools.model

import com.ertools.common.Matrix

class Input(
    private val inputHeight: Int,
    private val inputWidth: Int
): Layer(inputHeight * inputWidth) {
    /** API **/
    override fun initialize() {
        require(nextLayer != null) { "E: Layer has not been bound correctly." }
    }

    override fun response(input: Matrix): Matrix {
        return nextLayer!!.response(input) as Matrix

    }

    override fun error(input: Matrix): Matrix {
        return input
    }
}