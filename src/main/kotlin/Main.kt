package com.ertools

import com.ertools.common.Initializer
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.model.*
import java.util.*

fun main() {
    println("I: Start program.")
    println("I: Loading data from ${Utils.DATA_PATH}.")
    val xTrain = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TRAIN_IMAGE_DIGITS_DATA_FILENAME}", 10000)
    println("R: Load ${xTrain.data.size} train images.")
    val yTrain = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TRAIN_LABEL_DIGITS_DATA_FILENAME}", 10000)
    println("R: Load ${yTrain.labels.size} train labels.")
    val xTest = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TEST_IMAGE_DIGITS_DATA_FILENAME}", 10000)
    println("R: Load ${xTest.data.size} test images.")
    val yTest = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TEST_LABEL_DIGITS_DATA_FILENAME}", 10000)
    println("R: Load ${yTest.labels.size} test labels.")

    val cnn = CNN(
        listOf(
            Input(28, 28),
            Conv(
                filtersAmount = 16,
                kernel = 3,
                activationFunction = ActivationFunction.Relu,
                learningRate = 0.01,
                filtersInitializer = { Initializer.random(0.01) }
            ),
            MaxPool(
                poolSize = 2,
                stride = 2
            ),
            Flatten(),
            Dense(
                neurons = yTrain.labelsAmount,
                activationFunction = ActivationFunction.Linear,
                learningRate = 0.01,
                weightsInitializer = { Initializer.random(0.1) }
            ),
        )
    )
    println("I: Building CNN.")
    val log = cnn.build()
    log.forEach { println(it) }

    println("I: Start training.")

    val epochs = 50
    val dataSize = 1000
    val x = xTrain.data.take(dataSize)
    val y = yTrain.labels.take(dataSize)
    for(epoch in 0 until epochs) {
        val predictedLabels = cnn.fit(x, y)
        val accuracy = cnn.accuracy(predictedLabels, y)
        println("R: Epoch (${epoch + 1}/$epochs) accuracy ${"%.3f".format(Locale.ENGLISH, accuracy * 100)}%")
    }
}