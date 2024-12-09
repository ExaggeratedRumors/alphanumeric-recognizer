package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.fasterxml.jackson.annotation.JsonIgnore

class Conv(
    private val filtersAmount: Int,
    private val kernel: Int = 3,
    private val stride: Int = 1,
    private val padding: Int = 0,
    private val learningRate: Double = 0.001,
    @JsonIgnore private val filtersInitializer: () -> (Double) = { 0.0 },
    private val activationFunction: ActivationFunction = ActivationFunction.Linear
): Layer() {
    private var filters: Matrix? = null
    private var stack: Matrix? = null

    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        val prevDimensions = previousLayer!!.dimensions
        dimensions = Dimensions(
            height = (prevDimensions.height - kernel) / stride + 1,
            width = (prevDimensions.width - kernel) / stride + 1,
            channels = filtersAmount
        )
        if(filters != null) return
        filters = Matrix(filtersAmount, kernel * kernel) { filtersInitializer.invoke() }
    }

    /**
     * Rows: Image data
     * Columns: Filters
     */
    override fun response(input: Matrix): Matrix {
        val filteredImages = input.transpose().data.map { flatImage ->
            flatImage.toMatrix()
                .reconstructMatrix(previousLayer!!.dimensions.height)
                .applyPadding(padding)
                .convolution()
        }
        val result = filteredImages
            .reduce(Matrix::plus)
            .applyActivationFunction()
        return result
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
        require(filters!!.rows == error.rows && filters!!.columns == error.columns) {
            "E: Weights and error matrix must have the same dimensions." +
                    "\nGot: ${filters!!.rows}x${filters!!.columns} and ${error.rows}x${error.columns}."
        }
        filters = filters!!.minus(error.mul(learningRate))
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
        return vectorizedFilters.dot(filters!!.transpose())
    }

    fun Matrix.deconvolution(inputKernel: Array<Double>): Array<Array<Double>> {
        val filter = inputKernel.toMatrix().reconstructMatrix(kernel)
        val input = Array(previousLayer!!.dimensions.height) { Array(previousLayer!!.dimensions.width) { 0.0 } }
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                val value = this.data[i][j]
                for (ki in 0 until kernel) {
                    for (kj in 0 until kernel) {
                        input[i + ki][j + kj] += value * filter.data[0][ki * kernel + kj]
                    }
                }
            }
        }
        return input
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