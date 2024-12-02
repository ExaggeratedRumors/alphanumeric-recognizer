package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix

class MaxPool(
    val poolSize: Int = 2,
    val stride: Int = 2,
    val padding: Int = 0
): Layer<Matrix> {
    var mask: Matrix? = null
    var indexes: ArrayList<Pair<Int, Int>> = ArrayList()
    init {
        require(poolSize > 1 && stride > 0 && padding >= 0) {
            "E: Size must be higher than 1 and stride higher than 0."
        }
    }

    /** Choose max value per each pooled minor and then remember index of this value **/
    override fun response(input: Matrix): Matrix {
        indexes.clear()
        return (0 until input.rows step stride).map { row ->
            (0 until input.columns step stride).map { column ->
                val minor = input.slice(
                    IntRange(row * stride, (row + poolSize) * stride),
                    IntRange(column * stride, (column + poolSize) * stride)
                )
                var maxIndex: Pair<Int, Int> = Pair(0, 0)
                var maxValue = minor.data[0][0]
                minor.data.forEachIndexed { rowIndex, rowValue ->
                    rowValue.forEachIndexed { columnIndex, columnValue ->
                        if(columnValue > maxValue) {
                            maxValue = columnValue
                            maxIndex = Pair(rowIndex, columnIndex)
                        }
                    }
                }
                indexes.add(maxIndex)
                maxValue
            }.toTypedArray()
        }.toTypedArray().toMatrix()
    }
}