package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix

class Conv(
    private val filtersAmount: Int,
    private val kernel: Int = 3,
    private val stride: Int = 1,
    private val padding: Int = 0,
    private val learningRate: Double = 0.001,
    private val activationFunction: (Array<Double>) -> (Array<Double>) = { it }
): Layer() {
    private lateinit var filters: Matrix
    private var stack: Matrix? = null

    override fun initialize() {
        filters = Matrix(filtersAmount, kernel * kernel) { 1.0 }
        outputHeight = filtersAmount
        outputWidth = kernel * kernel
    }

    override fun response(input: Matrix): Matrix {
        return input
            .applyPadding(padding)
            .convolution()
            .applyActivationFunction()
    }

    override fun error(input: Matrix): Matrix {
        val error = input.transpose().dot(stack!!)
        updateFilters(error)
        return error
    }


    fun loadFilters(filters: Matrix) {
        this.filters = filters
    }

    /*************/
    /** Private **/
    /*************/

    private fun updateFilters(error: Matrix) {
        require(filters.rows == error.rows && filters.columns == error.columns) {
            "E: Weights and error matrix must have the same dimensions."
        }
        filters = filters.minus(error.mul(learningRate))
    }

    private fun Matrix.convolution(): Matrix {
        val vectorizedFilters = (0..this.rows - kernel step stride).map { row ->
                (0..this.columns - kernel step stride).map { column ->
                    this.vectorize(row, column)
                }
            }
            .flatten()
            .toTypedArray()
            .toMatrix()
        stack = vectorizedFilters
        return vectorizedFilters.dot(filters.transpose())
    }

    private fun Matrix.vectorize(rowIndex: Int, columnIndex: Int): Array<Double> =
        this.slice(
            IntRange(rowIndex, rowIndex + kernel - 1),
            IntRange(columnIndex, columnIndex + kernel - 1)
        ).matrixFlatten(orientation = Matrix.FlattenOrientation.Vertical).asVector()

    private fun Matrix.applyActivationFunction(): Matrix =
        this.data.map {
            activationFunction.invoke(it)
        }.toTypedArray().toMatrix()

}