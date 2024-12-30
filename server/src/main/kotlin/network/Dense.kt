package com.ertools.network

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Layer
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
    private var rememberInputVector: Matrix? = null
    private var rememberDerivativeActivation: Array<Double>? = null

    /** API **/
    override fun initialize() {
        require(previousLayer != null) { "E: Layer has not been bound." }
        dimensions = Dimensions(
            width = neurons
        )
        if(weights != null) return
        weights = Array(neurons) { Array(previousLayer!!.dimensions.width) { weightsInitializer.invoke() } }.toMatrix()
    }

    override fun info(): String {
        val info = sequence {
            yield("Dense (${dimensions.batch},${dimensions.height},${dimensions.width},${dimensions.channels})")
            yield("\tneurons: $neurons")
            yield("\tlearningRate: $learningRate")
            yield("\tactivationFunction: $activationFunction")
        }
        return info.toList().joinToString("\n")
    }

    /**
     * Rows: 1
     * Columns: Neurons
     */
    override fun response(input: Matrix): Matrix {
        rememberInputVector = input
        val resultVector = weights!!
            .dot(input.transpose())
            .asVector().let { vector ->
                rememberDerivativeActivation = activationFunction.invoke(vector, derivative = true)
                activationFunction.invoke(vector)
            }
            .toMatrix()
        return resultVector
    }

    /**
     * Rows: 1
     * Columns: Neurons
     */
    override fun error(input: Matrix): Matrix {
        val activatedInput = input.applyForEachRow { _, row ->
            row.zip(rememberDerivativeActivation!!).map{ it.first * it.second }.toTypedArray()
        }
        val error = weights!!
            .transpose()
            .dot(activatedInput.transpose())
            .transpose()

        updateWeights(activatedInput)
        return error
    }

    fun loadWeights(weights: Matrix) {
        this.weights = weights
    }

    /*************/
    /** Private **/
    /*************/

    private fun updateWeights(input: Matrix) {
        require (rememberInputVector != null) { "E: Response must be called before updateWeights." }
        val error = input.transpose().dot(rememberInputVector!!)
        require(weights!!.rows == error.rows && weights!!.columns == error.columns) {
            "E: Weights and error matrix must have the same dimensions."
        }
        weights = weights!!.minus(error.mul(learningRate))
    }
}