package com.ertools.model

import com.ertools.common.Matrix

class Dense(
    neurons: Int,
    activationFunction: (DoubleArray) -> (DoubleArray)
): Layer<Array<Double>> {
    private var weights: Array<DoubleArray> = Array(neurons) {
        DoubleArray(0)
    }

    fun loadWeights(weights: Array<DoubleArray>) {
        this.weights = weights
    }

    override fun response(input: Array<Double>): Array<Double> {
        TODO("Not yet implemented")
    }

    override fun error(input: Array<Double>): Array<Double> {
        TODO()
    }
}