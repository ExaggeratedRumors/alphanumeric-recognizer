package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.ActivationFunction.linear

class Dense(
    private val neurons: Int,
    private val activationFunction: (Array<Double>) -> (Array<Double>) = { it },
    private val weightsInitializer: () -> (Double) = { 0.0 }
): Layer<Array<Double>, Array<Double>>(neurons) {
    private lateinit var weights: Matrix

    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        weights = Array(previousLayer!!.size) { Array(neurons) { weightsInitializer.invoke() } }.toMatrix()
    }

    override fun response(input: Array<Double>): Array<Double> {
        val resultVector = weights.dot(input)
        return activationFunction(resultVector)
    }

    override fun error(input: Array<Double>): Array<Double> {
        return input.map { 2 * it / neurons }.toTypedArray()
    }

    fun loadWeights(weights: Matrix) {
        this.weights = weights
    }
}