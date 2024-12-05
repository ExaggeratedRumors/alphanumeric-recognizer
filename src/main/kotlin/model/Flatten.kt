package com.ertools.model

import com.ertools.common.Matrix


class Flatten : Layer(1) {
    override fun initialize() {
        size = previousLayer!!.size
    }

    override fun response(input: Matrix): Matrix {
        return input.flatten()
    }

    override fun error(input: Matrix): Matrix {
        return input.reconstructMatrix(previousLayer!!.size)
    }
}