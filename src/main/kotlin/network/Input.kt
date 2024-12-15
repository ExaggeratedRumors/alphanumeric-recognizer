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

    /**
     * Rows: Image data
     * Columns: Channels
     */
    override fun response(input: Matrix): Matrix {
        return input.matrixFlatten().transpose()
    }

    /**
     * Rows: Image data
     * Columns: Channels
     */
    override fun error(input: Matrix): Matrix {
        return input
    }
}