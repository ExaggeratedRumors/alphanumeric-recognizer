package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import kotlin.math.sqrt

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
        val filteredMatrixWidth = (sqrt(1.0 * previousLayer!!.outputWidth).toInt() - poolSize) / stride + 1
        outputWidth = filteredMatrixWidth * filteredMatrixWidth
        outputHeight = previousLayer!!.outputHeight
    }

    /** Choose max value per each pooled minor and then remember index of this value **/
    override fun response(input: Matrix): Matrix {
        maxValuesIndices.clear()
        val kernel = sqrt(1.0 * input.columns).toInt()

        val result = (0 until input.rows).map { filter ->
            val matrix = input.data[filter].toMatrix().reconstructMatrix(kernel).applyPadding(padding)
            val maxValuesVector = ArrayList<Pair<Int, Int>>()
            val pooledFilter = (0 until kernel step stride).map { row ->
                (0 until kernel step stride).map { column ->
                    val minor = matrix.slice(
                        IntRange(row, (row + poolSize) - 1),
                        IntRange(column, (column + poolSize) - 1)
                    )
                    var maxIndex: Pair<Int, Int> = Pair(0, 0)
                    var maxValue = minor.data[0][0]
                    minor.data.forEachIndexed { rowIndex, rowValue ->
                        rowValue.forEachIndexed { columnIndex, columnValue ->
                            if (columnValue > maxValue) {
                                maxValue = columnValue
                                maxIndex = Pair(row + rowIndex, column + columnIndex)
                            }
                        }
                    }
                    maxValuesVector.add(maxIndex)
                    maxValue
                }.toTypedArray()
            }.toTypedArray().toMatrix().matrixFlatten().asVector()
            maxValuesIndices.add(maxValuesVector)
            pooledFilter
        }.toTypedArray().toMatrix()
        return result
    }

    override fun error(input: Matrix): Matrix {
        val kernelSize = sqrt(1.0 * previousLayer!!.outputWidth).toInt()
        val poolingOutputSize = sqrt(1.0 * outputWidth).toInt()
        val error: ArrayList<Array<Double>> = ArrayList()

        input.data.forEachIndexed{ i, flatten ->
            /** Non-indexed values are zeros **/
            val filter = Array(kernelSize) { Array(kernelSize) { 0.0 } }
            val pool = flatten.toMatrix().reconstructMatrix(poolingOutputSize).matrixFlatten().asVector()
            maxValuesIndices[i].forEachIndexed { j, pair ->
                filter[pair.first][pair.second] = pool[j]
            }
            error.add(filter.toMatrix().matrixFlatten().asVector())
        }
        return error.toTypedArray().toMatrix()
    }
}