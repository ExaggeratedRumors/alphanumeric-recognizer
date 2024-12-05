package com.ertools

import com.ertools.common.Initializer
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.model.*

fun main() {
    println("I: Start program.")
    println("I: Loading data from ${Utils.DATA_PATH}.")
    val xTrain = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TRAIN_IMAGE_DATA_FILENAME}")
    println("R: Load ${xTrain.data.size} train images.")
    val yTrain = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TRAIN_LABEL_DATA_FILENAME}")
    println("R: Load ${yTrain.size} train labels.")
    val xTest = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TEST_IMAGE_DATA_FILENAME}")
    println("R: Load ${xTest.data.size} test images.")
    val yTest = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TEST_LABEL_DATA_FILENAME}")
    println("R: Load ${yTest.size} test labels.")

    val cnn = CNN(
        listOf(
            Input(28, 28),
            Conv(
                filtersAmount = 16,
                kernel = 3,
                activationFunction = ActivationFunction.Relu,
                learningRate = 0.001,
                filtersInitializer = { Initializer.random(0.01) }
            ),
            MaxPool(
                poolSize = 2,
                stride = 2
            ),
            Flatten(),
            Dense(
                neurons = 10,
                learningRate = 0.001,
                weightsInitializer = { Initializer.random(0.1) }
            ),
        )
    )
    println("I: Building CNN.")
    val log = cnn.build()
    log.forEach { println(it) }

    println("I: Start training.")
    for (i in 0 until 100) {
        val x = xTrain.data[i].toTypedArray().toMatrix().reconstructMatrix(xTrain.rows)
        val y = yTrain[i]
        val predictedLabels = cnn.fit(listOf(x), listOf(y))

        /*
        val accuracy = Utils.accuracy(predictedLabels, yTrain)
        println("I: Epoch $i, Accuracy: $accuracy")
         */
    }
}