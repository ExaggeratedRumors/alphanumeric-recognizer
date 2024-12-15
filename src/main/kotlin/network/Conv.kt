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
    private var vectorizedImagesStack: Matrix? = null

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
        this.vectorizedImagesStack = null
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

        val kernelError = error.dot(vectorizedImagesStack!!)
        updateFilters(kernelError)

        val inputError = error.data.map { flatError ->
            flatError.toMatrix()
                .reconstructMatrix(dimensions.height)
                .convolutionError()
        }
        val resultError = inputError
            .reduce(Matrix::plus)
        return resultError
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
        vectorizedImagesStack = if(vectorizedImagesStack == null) vectorizedImage
                else vectorizedImagesStack!!.plus(vectorizedImage)
        return vectorizedImage.dot(filters!!.transpose())
    }

    private fun Matrix.convolutionError(): Matrix {
        val padding = (this.rows - 1) * stride + kernel - this.rows
        val paddedMatrix = this.applyPadding(padding)

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