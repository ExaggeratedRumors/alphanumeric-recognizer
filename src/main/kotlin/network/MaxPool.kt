package com.ertools.network

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Layer

class MaxPool(
    private val poolSize: Int = 2,
    private val stride: Int = 2,
    private val padding: Int = 0
): Layer() {
    private val maxValuesIndices: ArrayList<List<Pair<Int, Int>>> = ArrayList()

    init {
        require(poolSize > 1 && stride > 0 && padding >= 0) {
            "E: Size must be higher than 1 and stride higher than 0."
        }
    }

    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        val prevDimensions = previousLayer!!.dimensions
        dimensions = Dimensions(
            height = (prevDimensions.height - poolSize) / stride + 1,
            width = (prevDimensions.width - poolSize) / stride + 1,
            channels = prevDimensions.channels,
            batch = prevDimensions.batch
        )
    }

    /**
     * Rows: Pooled image data
     * Columns: Filters
     */
    override fun response(input: Matrix): Matrix {
        maxValuesIndices.clear()
        val transposedInput = input.transpose()
        val result = pooling(transposedInput)
        return result.transpose()
    }

    /**
     * Rows: Pooled image data
     * Columns: Filters
     */
    override fun error(input: Matrix): Matrix {
        val error = reversePooling(input.transpose())
        return error.transpose()
    }

    /*************/
    /** Private **/
    /*************/

    /** Choose max value per each pooled minor and then remember index of this value **/
    private fun pooling(input: Matrix): Matrix =
        (0 until input.rows).map { filter ->
            val image = input.data[filter]
                .toMatrix()
                .reconstructMatrix(previousLayer!!.dimensions.height)
                .applyPadding(padding)
            val maxValuesVector = ArrayList<Pair<Int, Int>>()
            val pooledImage = (0 until dimensions.height).map { row ->
                (0 until dimensions.width).map { column ->
                    val minor = image.slice(
                        IntRange(row * stride, row * stride + poolSize - 1),
                        IntRange(column * stride, column * stride + poolSize - 1)
                    )
                    var maxIndex: Pair<Int, Int> = Pair(0, 0)
                    var maxValue = minor.data[0][0]
                    minor.data.forEachIndexed { rowIndex, rowValue ->
                        rowValue.forEachIndexed { columnIndex, columnValue ->
                            if (columnValue > maxValue) {
                                maxValue = columnValue
                                maxIndex = Pair(row * poolSize + rowIndex, column * poolSize + columnIndex)
                            }
                        }
                    }
                    maxValuesVector.add(maxIndex)
                    maxValue
                }.toTypedArray()
            }.toTypedArray().toMatrix().matrixFlatten().asVector()
            maxValuesIndices.add(maxValuesVector)
            pooledImage
        }.toTypedArray().toMatrix()


    private fun reversePooling(input: Matrix): Matrix {
        require(maxValuesIndices.isNotEmpty()) { "E: No max values indices found." }
        val error: ArrayList<Array<Double>> = ArrayList()
        input.data.forEachIndexed{ i, flatten ->
            /** Non-indexed values are zeros **/
            val image = Array(previousLayer!!.dimensions.height) { Array(previousLayer!!.dimensions.width) { 0.0 } }
            val pool = flatten.toMatrix().reconstructMatrix(dimensions.height).matrixFlatten().asVector()
            maxValuesIndices[i].forEachIndexed { j, pair ->
                image[pair.first][pair.second] = pool[j]
            }
            error.add(image.toMatrix().matrixFlatten().asVector())
        }
        return error.toTypedArray().toMatrix()
    }
}