package com.ertools

import com.ertools.operations.Evaluation
import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.io.ModelSerialization

fun main() {
    val model = ModelSerialization.load("balanced_50e_1c_1d")
    model.build()
    val xTest = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TEST_IMAGE_BALANCED_DATA_FILENAME}", 10000)
    val yTest = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TEST_LABEL_BALANCED_DATA_FILENAME}", 10000)
    val (x, y) = DataLoader.shuffle(xTest, yTest, 10000)

    val predictedLabels = x.map {
        model.predict(it)
    }

    val matrix = Evaluation.confusionMatrix(y, predictedLabels)
    println("R: Confusion matrix:\n$matrix")

    val accuracy = Evaluation.accuracy(y, predictedLabels)
    println("R: Accuracy: $accuracy")
}