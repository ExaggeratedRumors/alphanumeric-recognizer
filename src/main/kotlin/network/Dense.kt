package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.operations.ActivationFunction
import com.fasterxml.jackson.annotation.JsonIgnore

class Dense(
    private val neurons: Int,
    private val learningRate: Double = 0.001,
    @JsonIgnore private val weightsInitializer: () -> (Double) = { 0.0 },
    private val activationFunction: ActivationFunction = ActivationFunction.Linear
): Layer() {

    /** Variables **/
    private var weights: Matrix? = null
    private var stack: Matrix? = null

    /** API **/
    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        dimensions = Dimensions(
            width = neurons
        )
        if(weights != null) return
        weights = Array(neurons) { Array(previousLayer!!.dimensions.width) { weightsInitializer.invoke() } }.toMatrix()
    }

    /**
     * Rows: 1
     * Columns: Neurons
     */
    override fun response(input: Matrix): Matrix {
        stack = input
        val resultVector = weights!!.dot(input.transpose()).asVector()
        return activationFunction.invoke(resultVector).toMatrix()
    }

    /**
     * Rows: 1
     * Columns: Neurons
     */
    override fun error(input: Matrix): Matrix {
        val error = weights!!
            .transpose()
            .applyForEachRow { activationFunction.invoke(it, derivative = true) }
            .dot(input.transpose())
            .transpose()

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
        require(weights!!.rows == error.rows && weights!!.columns == error.columns) {
            "E: Weights and error matrix must have the same dimensions."
        }
        weights = weights!!.minus(error.mul(learningRate))
    }
}