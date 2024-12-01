package com.ertools.model

class Dense(
    neurons: Int,
    activationFunction: (DoubleArray) -> (DoubleArray)
): Layer {
    private var weights: Array<DoubleArray> = Array(neurons) {
        DoubleArray(0)
    }

    fun loadWeights(weights: Array<DoubleArray>) {
        this.weights = weights
    }

    override fun response(input: List<Double>): List<Double> {
        TODO("Not yet implemented")
    }
}