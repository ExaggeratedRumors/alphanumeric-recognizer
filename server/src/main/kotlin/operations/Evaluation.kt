package com.ertools.operations

import com.ertools.io.DataLoader
import java.util.*

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
        }.toList().joinToString("\n")
        return log
    }

    fun accuracy(trueLabels: List<Array<Double>>, predictedLabels: List<Array<Double>>): Double {
        val correct = predictedLabels.zip(trueLabels).count { (predicted, real) ->
            predicted.indexOf(predicted.maxOf { it }) == real.indexOf(real.maxOf { it })
        }
        return correct.toDouble() / trueLabels.size
    }

    fun valuesToLabel(values: Array<Double>, topN: Int): List<Pair<String, String>> {
        val labels = DataLoader.loadLabels(values.indices.toList()).map {
            it.toString()
        }
        val maxLabels = labels.zip(values).sortedByDescending { it.second }
        return maxLabels.take(topN).map {
            Pair(it.first, "%.4f".format(Locale.ENGLISH, it.second))
        }
    }
}