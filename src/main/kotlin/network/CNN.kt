package com.ertools.network

import com.ertools.operations.Error.dmse
import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Layer

class CNN(
    private val layers: List<Layer>
) {
    fun build(): List<String> {
        val log = sequence {
            layers.forEachIndexed { index, layer ->
                layer.bind(layers.getOrNull(index - 1), layers.getOrNull(index + 1))
                yield("R: Layer ${layer.javaClass.simpleName} " +
                        "(${layer.dimensions.height},${layer.dimensions.width},${layer.dimensions.channels}," +
                        "${layer.dimensions.batch}) initialized.")
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
}