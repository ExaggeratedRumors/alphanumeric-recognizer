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
    private var filters: Array<Matrix> = emptyArray()
    private var vectorizedImages: Array<Matrix> = emptyArray()

    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        val prevDimensions = previousLayer!!.dimensions
        dimensions = Dimensions(
            height = (prevDimensions.height - kernel) / stride + 1,
            width = (prevDimensions.width - kernel) / stride + 1,
            channels = filtersAmount
        )
        if(filters.isNotEmpty()) return

        /**
         * Depth: Input channels
         * Rows: Filters
         * Columns: Kernel weights
         */
        filters = Array(prevDimensions.channels) {
            Matrix(filtersAmount, kernel * kernel) {
                filtersInitializer.invoke()
            }
        }
    }

    /**
     * Rows: Image data
     * Columns: Filters
     */
    override fun response(input: Matrix): Matrix {
        this.vectorizedImages = Array(filters.size) { Matrix() }
        val filteredImages = input.transpose().data.mapIndexed { channelIndex, flatImage ->
            flatImage.toMatrix()
                .reconstructMatrix(previousLayer!!.dimensions.height)
                .applyPadding(padding)
                .convolution(channelIndex)
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

        updateFilters(error)

        val inputError = error.data.mapIndexed { channelIndex, flatError ->
            flatError.toMatrix()
                .reconstructMatrix(dimensions.height)
                .convolutionError(channelIndex)
        }
        val resultError = inputError
            .reduce(Matrix::plus)
        return resultError
    }


    fun loadFilters(filters: Array<Matrix>) {
        this.filters = filters
    }

    /*************/
    /** Private **/
    /*************/

    private fun updateFilters(error: Matrix) {
        filters = filters.mapIndexed { i, channelFilters ->
            val filterError = error.dot(vectorizedImages[i])
            require(channelFilters.rows == filterError.rows && channelFilters.columns == filterError.columns) {
                "E: Weights and error matrix must have the same dimensions." +
                        "\nGot: ${channelFilters.rows}x${channelFilters.columns}" +
                        " and ${filterError.rows}x${filterError.columns}."
            }
            channelFilters.minus(filterError.mul(learningRate))
        }.toTypedArray()
    }

    private fun Matrix.convolution(channelIndex: Int): Matrix {
        val vectorizedImage = (0..this.rows - kernel step stride).map { row ->
                (0..this.columns - kernel step stride).map { column ->
                    this.vectorize(row, column)
                }
            }
            .flatten()
            .toTypedArray()
            .toMatrix()
        vectorizedImages[channelIndex] = vectorizedImages[channelIndex].plus(vectorizedImage)
        return vectorizedImage.dot(filters[channelIndex].transpose())
    }

    private fun Matrix.convolutionError(filterIndex: Int): Matrix {
        val padding = (this.rows - 1) * stride + kernel - this.rows
        val paddedMatrix = this.applyPadding(padding)

        /*
         * Map INPUT_CHANNELS x DATA x FILTER_CHANNELS
         * to
         * FILTER_CHANNELS x DATA
         */
        val filterChannels = filters.map {
            it.data[filterIndex]
        }.toTypedArray().toMatrix()

        val rotatedKernels = filterChannels.data.map {
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