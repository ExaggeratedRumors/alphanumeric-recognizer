package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix

class Dropout(
    private val factor: Double
): Layer(1) {
    var indices: List<Int>? = null

    init {
        require(factor in 0.0..1.0) { "E: Factor must be between 0 and 1." }
    }

    override fun initialize() {
        size = previousLayer!!.size
    }

    override fun response(input: Matrix): Matrix {
        val flatten = input.flatten().asVector()
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

    override fun error(input: Matrix): Matrix {
        require(indices != null) { "E: Response must be called before error." }
        val flatten = input.flatten().asVector()
        indices!!.forEach {
            flatten[it] = 0.0
        }
        return flatten.toMatrix().reconstructMatrix(input.rows)
    }
}