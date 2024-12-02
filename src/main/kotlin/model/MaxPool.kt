package com.ertools.model

import com.ertools.common.Matrix

class MaxPool(
    poolSize: Int = 2,
    stride: Int = 2,
    padding: Int = 0
): Layer<Matrix> {

    override fun response(input: Matrix): Matrix {
        TODO("Not yet implemented")
    }
}