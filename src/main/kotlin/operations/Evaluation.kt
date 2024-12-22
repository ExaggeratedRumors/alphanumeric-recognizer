package com.ertools.operations

import com.ertools.io.DataLoader

object Evaluation {
    fun confusionMatrix(trueLabels: List<Array<Double>>, predictedLabels: List<Array<Double>>): String {
        val log = sequence {
            val classes = DataLoader.loadLabels ((0 until trueLabels[0].size).toList())
            val confusionMatrix = Array(classes.size) { IntArray(classes.size) }
            for(i in trueLabels.indices) {
                val predictedLabel = predictedLabels[i].indexOf(predictedLabels[i].maxOf { it })
                val trueLabel = trueLabels[i].indexOf(trueLabels[i].maxOf { it })
                confusionMatrix[trueLabel][predictedLabel] += 1
            }
            confusionMatrix.forEachIndexed { i, row ->
                yield("${classes[i]}\t|\t${row.joinToString("\t")}")
            }
            val labels = (0 until confusionMatrix[0].size).map {
                classes[it]
            }.joinToString("\t")
            yield("\t\t$labels")
        }.toString()
        return log
    }

    fun accuracy(trueLabels: List<Array<Double>>, predictedLabels: List<Array<Double>>): Double {
        val correct = predictedLabels.zip(trueLabels).count { (predicted, real) ->
            predicted.indexOf(predicted.maxOf { it }) == real.indexOf(real.maxOf { it })
        }
        return correct.toDouble() / trueLabels.size
    }

    fun valuesToLabels(values: Array<Double>): Array<Pair<Char, Double>> {
        val labels = DataLoader.loadLabels(values.indices.toList())
        return labels.zip(values).toTypedArray()
    }
}