package com.ertools.model

class Conv(
    filters: Int,
    kernelSize: Int = 3,
    stride: Int = 1,
    padding: Int = 0,
    activationFunction: (DoubleArray) -> (DoubleArray)
): Layer {
    private var weights: Array<DoubleArray> = Array(filters) {
        DoubleArray(0)
    }

    fun loadWeights(weights: Array<DoubleArray>) {
        this.weights = weights
    }

    override fun response(input: List<Double>): List<Double> {
        TODO("Not yet implemented")
    }
}