package com.ertools.model

import com.ertools.common.Error.dmse
import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix

class CNN(
    private val layers: List<Layer>
) {
    fun build(): List<String> {
        val log = sequence {
            layers.forEachIndexed { index, layer ->
                layer.bind(layers.getOrNull(index - 1), layers.getOrNull(index + 1))
                yield("R: Layer ${layer.javaClass.simpleName} (${layer.outputHeight},${layer.outputWidth}) initialized.")
            }
        }.toList()
        return log
    }

    fun fit(x: List<Matrix>, y: List<Array<Double>>): List<Array<Double>> {
        val predictedLabels = mutableListOf<Array<Double>>()
        x.zip(y).forEach { (image, label) ->
            /** 1. Calculate response **/
            var response: Matrix = image
            layers.forEach {
                response = it.response(response)
            }

            /** 2. Calculate error **/
            var error = dmse(response.asVector(), label).toMatrix()

            /** 3. Backpropagation **/
            layers.reversed().forEach { error = it.error(error) }

            predictedLabels.add(response.asVector())
        }
        return predictedLabels
    }

    fun predict(x: Matrix): Array<Double> {
        var response: Matrix = x
        layers.forEach { response = it.response(response) }
        return response.asVector()
    }

    fun accuracy(predictedLabels: List<Array<Double>>, y: List<Array<Double>>): Double {
        val correct = predictedLabels.zip(y).count { (predicted, real) ->
            predicted.indexOf(predicted.maxOf { it }) == real.indexOf(real.maxOf { it })
        }
        return correct.toDouble() / y.size
    }
}