package com.ertools.model

import com.ertools.common.Matrix


class Flatten : Layer() {
    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        val prevDimensions = previousLayer!!.dimensions
        dimensions = Dimensions(
            height = 1,
            width = prevDimensions.width * prevDimensions.height * prevDimensions.channels * prevDimensions.batch,
            channels = 1
        )
    }

    override fun response(input: Matrix): Matrix {
        return input.matrixFlatten()
    }

    override fun error(input: Matrix): Matrix {
        return input.reconstructMatrix(previousLayer!!.dimensions.height * previousLayer!!.dimensions.width)
    }
}