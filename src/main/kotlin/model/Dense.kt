package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix

class Dense(
    private val neurons: Int,
    private val activationFunction: (Array<Double>) -> (Array<Double>) = { it },
    private val weightsInitializer: () -> (Double) = { 0.0 },
    private val learningRate: Double = 0.001
): Layer(neurons) {

    /** Variables **/
    private lateinit var weights: Matrix
    private var stack: Matrix? = null

    /** API **/
    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        weights = Array(previousLayer!!.size) { Array(neurons) { weightsInitializer.invoke() } }.toMatrix()
    }

    override fun response(input: Matrix): Matrix {
        stack = input
        val resultVector = weights.dot(input.transpose()).asVector()
        return activationFunction(resultVector).toMatrix()
    }

    override fun error(input: Matrix): Matrix {
        val error = weights.transpose().dot(input.transpose()).transpose()
        updateWeights(input)
        return error
    }

    fun loadWeights(weights: Matrix) {
        this.weights = weights
    }

    /*************/
    /** Private **/
    /*************/

    private fun updateWeights(input: Matrix) {
        require (stack != null) { "E: Response must be called before updateWeights." }
        val error = input.transpose().dot(stack!!)
        require(weights.rows == error.rows && weights.columns == error.columns) {
            "E: Weights and error matrix must have the same dimensions."
        }
        weights = weights.minus(error.mul(learningRate))
    }
}