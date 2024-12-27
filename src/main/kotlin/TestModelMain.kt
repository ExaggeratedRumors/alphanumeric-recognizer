package com.ertools

import com.ertools.operations.Evaluation
import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.io.ModelSerialization

fun main(args: Array<String>) {
    val modelName = if(args.isEmpty()) "balanced_50e_1c_2d" else args[0]
    val testSizeException = if(args.size > 1) args[1].toInt() else 18800

    val model = ModelSerialization.load(modelName)
    model.build()
    val xTest = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TEST_IMAGE_BALANCED_DATA_FILENAME}", testSizeException)
    val yTest = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TEST_LABEL_BALANCED_DATA_FILENAME}", testSizeException)
    val (x, y) = DataLoader.shuffle(xTest, yTest, testSizeException)

    val predictedLabels = x.map {
        model.predict(it)
    }

    val matrix = Evaluation.confusionMatrix(y, predictedLabels)
    println("R: Confusion matrix:\n$matrix")

    val accuracy = Evaluation.accuracy(y, predictedLabels)
    println("R: Accuracy: $accuracy")
}