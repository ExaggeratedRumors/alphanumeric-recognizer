package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Vector

class Conv(
    val filtersAmount: Int,
    val kernel: Int = 3,
    val stride: Int = 1,
    val padding: Int = 0,
    val activationFunction: (DoubleArray) -> (DoubleArray)
): Layer<Matrix> {
    private var filters: Matrix = Matrix(filtersAmount, kernel * kernel) { 1.0 }

    override fun response(input: Matrix): Matrix {
        return input.applyPadding(padding).convolution()
    }

    fun loadFilters(filters: Matrix) {
        this.filters = filters
    }

    /*************/
    /** Private **/
    /*************/
    private fun Matrix.convolution(): Matrix {
        val vectorizedImage = mutableListOf<Vector>()
        (0..this.rows step stride).forEach { row ->
            (0..this.columns step stride).forEach { column ->
                val vector = this.slice(IntRange(row, row + kernel), IntRange(column, column + kernel)).flatten()
                vectorizedImage.add(vector)
            }
        }

        val matrix = Matrix(
            rows = vectorizedImage.size,
            columns = vectorizedImage[0].size,
        ).apply {
            this.data = Array(rows) { row ->
                vectorizedImage[row].data
            }
        }

        return matrix.dot(filters.transpose())
    }
}