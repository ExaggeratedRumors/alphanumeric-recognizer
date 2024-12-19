package com.ertools.server

import com.ertools.io.DataLoader
import com.ertools.io.ModelSerialization
import com.ertools.network.*
import com.ertools.operations.Evaluation
import com.ertools.operations.Initializer
import java.util.*

class ModelTrainingRoutine(
    private val modelDTO: ModelDTO,
    private val responseCallback: (String) -> Unit
): Thread() {
    override fun run() {
        trainModel()
    }

    private fun trainModel() {
        val layers = modelDTO.layers.map {
            when(it) {
                is LayerDTO.InputDTO -> Input(it.height, it.width)
                is LayerDTO.ConvDTO -> Conv(
                    it.filtersAmount,
                    it.kernel,
                    it.stride,
                    it.padding,
                    modelDTO.learningRate,
                    { Initializer.random(it.weightRange) },
                    it.activation
                )
                is LayerDTO.MaxPoolDTO -> MaxPool(it.poolSize, it.stride, it.padding)
                is LayerDTO.FlattenDTO -> Flatten()
                is LayerDTO.DenseDTO -> Dense(
                    it.neurons,
                    modelDTO.learningRate,
                    { Initializer.random(it.weightRange) },
                    it.activation
                )
                is LayerDTO.DropoutDTO -> Dropout(it.rate)
            }
        }

        val model = CNN(layers)
        val trainData = DataLoader.loadImageData(modelDTO.trainDataPath, modelDTO.trainDataSize)
        val trainLabels = DataLoader.loadLabelData(modelDTO.trainDataPath, modelDTO.trainDataSize)
        val (x, y) = DataLoader.shuffle(trainData, trainLabels, modelDTO.trainDataSize)
        var predictedLabels = emptyList<Array<Double>>()
        for(epoch in 0 until modelDTO.epochs) {
            predictedLabels = model.fit(x, y)
            val accuracy = Evaluation.accuracy(y, predictedLabels)
            responseCallback.invoke("R: Epoch (${epoch + 1}/$modelDTO.epochs) accuracy ${"%.3f".format(Locale.ENGLISH, accuracy * 100)}%")
        }
        Evaluation.confusionMatrix(y, predictedLabels)
        ModelSerialization.save(model, modelDTO.modelName)
    }
}