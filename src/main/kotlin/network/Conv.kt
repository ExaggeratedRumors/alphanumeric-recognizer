package com.ertools.network

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Layer
import com.ertools.operations.ActivationFunction
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
            .applyForEachRow { activationFunction.invoke(it) }
        return result
    }

    /**
     * Rows: Image data
     * Columns: Filters
     */
    override fun error(input: Matrix): Matrix {
        val error = input.transpose()
            //.applyForEachRow { activationFunction.invoke(it, derivative = true) }

        val kernelError = error.dot(stack!!)
        updateFilters(kernelError)

        val inputError = error.data.forEach { flatError ->
            flatError.toMatrix()
                .reconstructMatrix(dimensions.height)
                .convolutionError()
        }
        return error.convolutionError()
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
        val vectorizedImage = (0..this.rows - kernel step stride).map { row ->
                (0..this.columns - kernel step stride).map { column ->
                    this.vectorize(row, column)
                }
            }
            .flatten()
            .toTypedArray()
            .toMatrix()
        stack = vectorizedImage
        return vectorizedImage.dot(filters!!.transpose())
    }

    private fun Matrix.convolutionError(): Matrix {
        val paddedMatrix = this.applyPadding(1)

        val rotatedKernels = filters!!.data.map {
            it.toMatrix().rotate180degree().matrixFlatten().asVector()
        }.toTypedArray().toMatrix()

        val vectorizedErrorImage = (0..paddedMatrix.rows - kernel step stride).map { row ->
                (0..paddedMatrix.columns - kernel step stride).map { column ->
                    paddedMatrix.vectorize(row, column)
                }
            }
            .flatten()
            .toTypedArray()
            .toMatrix()

        return vectorizedErrorImage.dot(rotatedKernels.transpose())
    }

    private fun Matrix.vectorize(rowIndex: Int, columnIndex: Int): Array<Double> =
        this.slice(
            IntRange(rowIndex, rowIndex + kernel - 1),
            IntRange(columnIndex, columnIndex + kernel - 1)
        ).matrixFlatten(orientation = Matrix.FlattenOrientation.Vertical).asVector()
}