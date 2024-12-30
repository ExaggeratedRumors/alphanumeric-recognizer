package com.ertools.network

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Layer

class Dropout(
    private val factor: Double
): Layer() {
    private var indices: List<Int>? = null

    init {
        require(factor in 0.0..1.0) { "E: Factor must be between 0 and 1." }
    }

    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        dimensions = previousLayer!!.dimensions
    }

    /**
     * Rows: 1
     * Columns: Image data
     */
    override fun response(input: Matrix): Matrix {
        val flatten = input.matrixFlatten().asVector()
        val expiredNeuronsAmount = (flatten.size * factor).toInt()
        indices = flatten.indices
            .toList()
            .shuffled()
            .take(expiredNeuronsAmount)

        indices!!.forEach {
            flatten[it] = 0.0
        }
        return flatten.toMatrix().reconstructMatrix(input.rows)
    }

    override fun info(): String {
        val info = sequence {
            yield("Dropout (${dimensions.batch},${dimensions.height},${dimensions.width},${dimensions.channels})")
            yield("\tfactor: $factor")
        }
        return info.toList().joinToString("\n")
    }
    /**
     * Rows: 1
     * Columns: Image data
     */
    override fun error(input: Matrix): Matrix {
        require(indices != null) { "E: Response must be called before error." }
        val flatten = input.matrixFlatten().asVector()
        indices!!.forEach {
            flatten[it] = 0.0
        }
        return flatten.toMatrix().reconstructMatrix(input.rows)
    }
}