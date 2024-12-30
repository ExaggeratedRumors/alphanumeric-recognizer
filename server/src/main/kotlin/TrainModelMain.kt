package com.ertools

import com.ertools.operations.Evaluation
import com.ertools.operations.Initializer
import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.io.ModelSerialization
import com.ertools.network.*
import com.ertools.operations.ActivationFunction
import java.util.*

fun main(args: Array<String>) {
    val modelName = if(args.isEmpty()) "balanced_100e_1c_1d" else args[0]
    val epochs = if(args.size > 1) args[1].toInt() else 100
    val trainingDataAmount = if(args.size > 2) args[2].toInt() else 8000

    println("I: Start program.")
    println("I: Loading data from ${Utils.DATA_PATH}.")
    val xTrain = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TRAIN_IMAGE_BALANCED_DATA_FILENAME}", 10000)
    println("R: Load ${xTrain.data.size} train images.")
    val yTrain = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TRAIN_LABEL_BALANCED_DATA_FILENAME}", 10000)
    println("R: Load ${yTrain.labels.size} train labels.")
    val xTest = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TEST_IMAGE_BALANCED_DATA_FILENAME}", 10000)
    println("R: Load ${xTest.data.size} test images.")
    val yTest = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TEST_LABEL_BALANCED_DATA_FILENAME}", 10000)
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
                neurons = 128,
                activationFunction = ActivationFunction.Relu,
                learningRate = 0.01,
                weightsInitializer = { Initializer.random(0.1) }
            ),
            Dense(
                neurons = yTest.labelsAmount,
                activationFunction = ActivationFunction.Softmax,
                learningRate = 0.01,
                weightsInitializer = { Initializer.random(0.1) }
            )
        )
    )
    println("I: Building CNN.")
    val log = cnn.build()
    println(log)

    val trainingInfo = ModelSerialization.TrainingInfo(
        modelName = modelName,
        epochs = epochs,
        trainingDataAmount = trainingDataAmount,
        batch = 1
    )
    println("I: Start training ${trainingInfo.trainingDataAmount} data samples for ${trainingInfo.epochs} epochs.")
    val (x, y) = DataLoader.shuffle(xTrain, yTrain, trainingInfo.trainingDataAmount)
    var predictedLabels = emptyList<Array<Double>>()
    for(epoch in 0 until trainingInfo.epochs) {
        predictedLabels = cnn.fit(x, y)
        val accuracy = Evaluation.accuracy(y, predictedLabels)
        println("R: Epoch (${epoch + 1}/${trainingInfo.epochs}) accuracy ${"%.3f".format(Locale.ENGLISH, accuracy * 100)}%")
    }
    val matrix = Evaluation.confusionMatrix(y, predictedLabels)
    println("R: Confusion matrix:\n$matrix")

    ModelSerialization.save(cnn, trainingInfo)
}