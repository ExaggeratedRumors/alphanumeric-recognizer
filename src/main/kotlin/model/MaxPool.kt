package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import kotlin.math.sqrt

class MaxPool(
    val poolSize: Int = 2,
    val stride: Int = 2,
    val padding: Int = 0
): Layer<Matrix, Matrix>(poolSize * poolSize) {
    var mask: Matrix? = null
    private val maxIndexList: ArrayList<Pair<Int, Int>> = ArrayList()
    private var stack: Matrix? = null

    init {
        require(poolSize > 1 && stride > 0 && padding >= 0) {
            "E: Size must be higher than 1 and stride higher than 0."
        }
    }

    override fun initialize() {
        TODO("Not yet implemented")
    }

    /** Choose max value per each pooled minor and then remember index of this value **/
    override fun response(input: Matrix): Matrix {
        stack = input
        maxIndexList.clear()

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
                maxIndexList.add(maxIndex)
                maxValue
            }.toTypedArray()
        }.toTypedArray().toMatrix()
    }

    override fun mseError(input: Matrix): Matrix {
        require(stack != null && maxIndexList.isNotEmpty()) { "E: Layer has not been activated."}
        /*val filtersAmount = stack!!.rows
        val kernelSize = sqrt(1.0 * stack!!.columns).toInt()
        (0 until filtersAmount).map { filter ->
            val kernel = Array(kernelSize) { Array(kernelSize) { 0.0 } }
            val flatFilter = input.slice(
                IntRange(filter * poolSize, (filter + 1) * poolSize),
                IntRange
            )
        }*/
        TODO()
    }

}