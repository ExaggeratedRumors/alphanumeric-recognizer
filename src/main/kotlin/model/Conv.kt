package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix

class Conv(
    private val filtersAmount: Int,
    private val kernel: Int = 3,
    private val stride: Int = 1,
    private val padding: Int = 0,
    private val activationFunction: (Array<Double>) -> (Array<Double>)
): Layer<Matrix, Matrix>(filtersAmount) {
    private lateinit var filters: Matrix

    override fun initialize() {
        filters = Matrix(filtersAmount, kernel * kernel) { 1.0 }
    }

    override fun response(input: Matrix): Matrix {
        return input
            .applyPadding(padding)
            .convolution()
            .applyActivationFunction()
    }

    override fun error(input: Matrix): Matrix {
        TODO()
    }


    fun loadFilters(filters: Matrix) {
        this.filters = filters
    }

    /*************/
    /** Private **/
    /*************/

    /**
     * Vectorize image by filter through kernel including stride and flatten it.
     */
    private fun Matrix.convolution(): Matrix =
        (0 until this.rows step stride).map { row ->
            (0 until this.columns step stride).map { column ->
                this.vectorize(row, column)
            }
        }
        .flatten()
        .toTypedArray()
        .toMatrix()

    private fun Matrix.vectorize(rowIndex: Int, columnIndex: Int): Array<Double> =
        this.slice(
            IntRange(rowIndex, rowIndex + kernel),
            IntRange(columnIndex, columnIndex + kernel)
        ).flatten()

    private fun Matrix.applyActivationFunction(): Matrix =
        this.data.map {
            activationFunction.invoke(it)
        }.toTypedArray().toMatrix()

}