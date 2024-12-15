package com.ertools.network

import com.ertools.common.Matrix
import com.ertools.model.Layer


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

    /**
     * Input: rows (image data), columns (filters)
     * Output: rows (1), columns (image data)
     */
    override fun response(input: Matrix): Matrix {
        return input.matrixFlatten()
    }

    /**
     * Input: rows (1), columns (image data)
     * Output: rows (image data), columns (filters)
     */
    override fun error(input: Matrix): Matrix {
        return input.reconstructMatrix(previousLayer!!.dimensions.height * previousLayer!!.dimensions.width)
    }
}