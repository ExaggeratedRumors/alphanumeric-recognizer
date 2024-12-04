package com.ertools.model

import com.ertools.common.Matrix

class Input(
    private val inputHeight: Int,
    private val inputWidth: Int
): Layer<Matrix, Matrix>(inputHeight * inputWidth) {

    /** Variables **/
    private lateinit var image: Matrix

    /** API **/
    override fun initialize() {}

    override fun response(input: Matrix): Matrix {
        image = input
        return input
    }

    override fun error(input: Matrix): Matrix {
        return input
    }
}