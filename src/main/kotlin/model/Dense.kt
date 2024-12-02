package com.ertools.model

import com.ertools.common.Vector

class Dense(
    neurons: Int,
    activationFunction: (DoubleArray) -> (DoubleArray)
): Layer<Vector> {
    private var weights: Array<DoubleArray> = Array(neurons) {
        DoubleArray(0)
    }

    fun loadWeights(weights: Array<DoubleArray>) {
        this.weights = weights
    }

    override fun response(input: Vector): Vector {
        TODO("Not yet implemented")
    }
}