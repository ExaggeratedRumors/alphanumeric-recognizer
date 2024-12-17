package com.ertools.network

import com.ertools.common.Matrix
import com.ertools.model.Layer

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