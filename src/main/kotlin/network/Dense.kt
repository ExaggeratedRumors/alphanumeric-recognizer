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
    private var rememberDataVector: Matrix? = null
    private var rememberDataActivated: Array<Double>? = null

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
        rememberDataVector = input
        val resultVector = weights!!.dot(input.transpose())
            .asVector().let { vector ->
                rememberDataActivated = activationFunction.invoke(vector, derivative = true)
                activationFunction.invoke(vector)
            }
        return activationFunction.invoke(resultVector).toMatrix()
    }

    /**
     * Rows: 1
     * Columns: Neurons
     */
    override fun error(input: Matrix): Matrix {
        val error = weights!!
            .transpose()
            .applyForEachRow { _, row ->
                row.zip(rememberDataActivated!!).map{ it.first * it.second }.toTypedArray()
            }
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
        require (rememberDataVector != null) { "E: Response must be called before updateWeights." }
        val error = input.transpose().dot(rememberDataVector!!)
        require(weights!!.rows == error.rows && weights!!.columns == error.columns) {
            "E: Weights and error matrix must have the same dimensions."
        }
        weights = weights!!.minus(error.mul(learningRate))
    }
}