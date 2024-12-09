package com.ertools.common

object Evaluation
{
    fun consussionMatrix(trueLabels: List<Array<Double>>, predictedLabels: List<Array<Double>>) {
        val classes = (0 until trueLabels[0].size).toList()
        val confusionMatrix = Array(classes.size) { IntArray(classes.size) }
        for(i in trueLabels.indices) {
            val predictedLabel = predictedLabels[i].indexOf(predictedLabels[i].maxOf { it })
            val trueLabel = trueLabels[i].indexOf(trueLabels[i].maxOf { it })
            confusionMatrix[trueLabel][predictedLabel] += 1
        }
        confusionMatrix.forEachIndexed { i, row ->
            println("${classes[i]}\t|\t${row.joinToString("\t")}")
        }
        val labels = (0 until confusionMatrix[0].size).map {
            classes[it]
        }.joinToString("\t")
        println("\t\t$labels")
    }
}