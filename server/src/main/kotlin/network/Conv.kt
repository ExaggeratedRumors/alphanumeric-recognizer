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
    private var rememberVectorizedImages: Array<Matrix?> = emptyArray()
    private var rememberDerivativeActivation: Array<Array<Double>?> = emptyArray()

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

    override fun info(): String {
        val info = sequence {
            yield("Conv (${dimensions.batch},${dimensions.height},${dimensions.width},${dimensions.channels})")
            yield("\tfiltersAmount: $filtersAmount")
            yield("\tkernel: $kernel")
            yield("\tstride: $stride")
            yield("\tpadding: $padding")
            yield("\tlearningRate: $learningRate")
            yield("\tactivationFunction: $activationFunction")
        }
        return info.toList().joinToString("\n")
    }

    /**
     * Rows: Image data
     * Columns: Channels
     */
    override fun response(input: Matrix): Matrix {
        /** Reset remembered data **/
        this.rememberVectorizedImages = Array(filters.size) { null }
        this.rememberDerivativeActivation = Array(dimensions.channels) { null }

        /** Make convolution for each image **/
        val filteredImagesByChannel = input.transpose().data.mapIndexed { channelIndex, flatImage ->
            flatImage.toMatrix()
                .reconstructMatrix(previousLayer!!.dimensions.height)
                .applyPadding(padding)
                .convolution(channelIndex)
        }

        /** Sum filtered images for each channel **/
        val result = filteredImagesByChannel
            .reduce(Matrix::plus)
            .transpose()
            .applyForEachRow { i, row ->
                rememberDerivativeActivation[i] = activationFunction.invoke(row, true)
                activationFunction.invoke(row)
            }
            .transpose()
        return result
    }

    /**
     * Rows: Image data
     * Columns: Filters
     */
    override fun error(input: Matrix): Matrix {
        val error = input.transpose()
            .applyForEachRow { i, row ->
                row.zip(rememberDerivativeActivation[i]!!).map { it.first * it.second }.toTypedArray()
            }

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
            val filterError = error.dot(rememberVectorizedImages[i]!!)
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
        if(rememberVectorizedImages[channelIndex] == null) rememberVectorizedImages[channelIndex] = vectorizedImage
        else rememberVectorizedImages[channelIndex] = rememberVectorizedImages[channelIndex]!!.plus(vectorizedImage)
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