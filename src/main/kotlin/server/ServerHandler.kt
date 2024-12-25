package com.ertools.server

import com.ertools.common.Matrix
import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.io.ModelSerialization
import com.ertools.network.*
import com.ertools.operations.ActivationFunction
import com.ertools.operations.Evaluation
import com.ertools.operations.Initializer
import com.ertools.operations.Preprocessing
import com.ertools.server.dto.LayerDTO
import com.ertools.server.dto.TrainModelDTO
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.File
import java.io.InputStreamReader
import java.util.*

class ServerHandler(private val statusQueue: ArrayList<ModelStatus>): HttpHandler {
    override fun handle(exchange: HttpExchange) {
        println("I: handle request [${exchange.requestURI}]")
        try {
            val endpoint = getEndpoint(exchange)
            when(exchange.requestMethod) {
                "GET" -> {
                    when(endpoint) {
                        "models" -> serviceGetModels(exchange)
                        "data" -> serviceGetData(exchange)
                        "status" -> serviceGetStatus(exchange)
                        else -> reply("E: Invalid endpoint", 400, exchange)
                    }
                }
                "POST" -> {
                    when(endpoint) {
                        "train" -> servicePostTrain(exchange)
                        "classify" -> servicePostClassify(exchange)
                        else -> reply("E: Invalid endpoint", 400, exchange)
                    }
                }
                "DELETE" -> {
                    when(endpoint) {
                        "model" -> serviceDeleteModel(exchange)
                        else -> reply("E: Invalid endpoint", 400, exchange)
                    }
                }
                else -> reply("E: Invalid endpoint", 400, exchange)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*************/
    /** Private **/
    /*************/
    private fun getEndpoint(exchange: HttpExchange): String {
        val requestURI = exchange.requestURI.toString()
        val endpoint = requestURI.trim('/').split("/").first()
        return endpoint
    }

    private fun reply(response: String, code: Int, exchange: HttpExchange) {
        println("R: reply [$response] with code [$code]")
        exchange.sendResponseHeaders(code, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }
    
    private fun updateStatus(status: ModelStatus, newStatus: ModelStatus.Status, info: String) {
        println("R: Update status for model ${status.modelName} to $newStatus.")
        println(info)
        status.update(newStatus, info)
    }

    private fun serviceGetModels(exchange: HttpExchange) {
        val modelsData = ModelSerialization.getModelsInfo()
        reply(modelsData, 200, exchange)
    }

    private fun serviceGetData(exchange: HttpExchange) {
        val fullDirectoryInfo = DataLoader.getFullDataInfo(Utils.DATA_PATH)
        reply(fullDirectoryInfo, 200, exchange)
    }

    private fun serviceGetStatus(exchange: HttpExchange) {
        val modelName = exchange.requestURI.toString().trim('/').split("/").last()
        val status = statusQueue.firstOrNull { it.modelName == modelName }
        if(status == null) {
            reply("E: Model with name $modelName not found.", 400, exchange)
            return
        }
        reply(status.info, 200, exchange)
    }

    private fun servicePostTrain(exchange: HttpExchange) {
        /** Read request **/
        val modelDTO: TrainModelDTO
        try {
            val isr = InputStreamReader(exchange.requestBody)
            modelDTO = ServerMapper.getMapper().readValue(isr, TrainModelDTO::class.java)
        } catch (e: Exception) {
            reply("E: Failed to parse request.", 500, exchange)
            return
        }
        statusQueue.firstOrNull { it.modelName == modelDTO.modelName && it.status != ModelStatus.Status.EMPTY }?.let {
            reply("E: Model with name ${modelDTO.modelName} is already training.", 400, exchange)
            return
        }

        statusQueue.count {
            it.status in listOf(ModelStatus.Status.PREPARING, ModelStatus.Status.BUILT, ModelStatus.Status.TRAINING)
        }.let {
            if(it >= Utils.MAX_MODELS_TRAINING) {
                reply("E: Maximum concurrent models reached.", 400, exchange)
                return
            }
        }

        val status = ModelStatus(modelDTO.modelName, ModelStatus.Status.PREPARING, "I: Building model.")
        statusQueue.add(status)

        /** Build model **/
        val model: CNN
        try {
            val layers = modelDTO.layers.map {
                when (it) {
                    is LayerDTO.InputDTO -> Input(it.height, it.width)
                    is LayerDTO.ConvDTO -> Conv(
                        it.filtersAmount,
                        it.kernel,
                        it.stride,
                        it.padding,
                        modelDTO.learningRate,
                        { Initializer.random(it.weightRange) },
                        ActivationFunction.fromString(it.activation)
                    )
                    is LayerDTO.MaxPoolDTO -> MaxPool(it.poolSize, it.stride, it.padding)
                    is LayerDTO.FlattenDTO -> Flatten()
                    is LayerDTO.DenseDTO -> Dense(
                        it.neurons,
                        modelDTO.learningRate,
                        { Initializer.random(it.weightRange) },
                        ActivationFunction.fromString(it.activation)
                    )
                    is LayerDTO.DropoutDTO -> Dropout(it.rate)
                }
            }

            model = CNN(layers)
            model.build()
            reply("R: Model successfully built and start training. Request /status/${modelDTO.modelName} to check status.", 202, exchange)
            updateStatus(status, ModelStatus.Status.BUILT, model.info)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "E: Failed to build model."
            reply(errorMessage, 400, exchange)
            statusQueue.remove(status)
            return
        }

        /** Load training data **/
        updateStatus(status, ModelStatus.Status.BUILT, "I: Loading training data.")
        val trainingInfo = ModelSerialization.TrainingInfo(
            modelDTO.modelName,
            modelDTO.epochs,
            modelDTO.trainDataSize,
            modelDTO.batchSize
        )

        val trainData: DataLoader.ImageSetData
        val trainLabels: DataLoader.LabelSetData

        try {
            trainData = DataLoader.loadImageData(
                "${Utils.DATA_PATH}/${modelDTO.trainDataPath}",
                modelDTO.trainDataSize
            )
            trainLabels = DataLoader.loadLabelData(
                "${Utils.DATA_PATH}/${modelDTO.trainLabelsPath}",
                modelDTO.trainDataSize
            )
            updateStatus(status, ModelStatus.Status.BUILT, "I: Loaded ${trainData.amount} training images.")
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            updateStatus(status, ModelStatus.Status.EMPTY, "E: Output size mismatch data labels amount.")
            return
        } catch (e: Exception) {
            e.printStackTrace()
            updateStatus(status, ModelStatus.Status.EMPTY, "E: Failed to load training data.")
            return
        }
        val (x, y) = DataLoader.shuffle(trainData, trainLabels, modelDTO.trainDataSize)

        /** Train model **/
        updateStatus(status, ModelStatus.Status.TRAINING, "I: Training model.")
        var predictedLabels = emptyList<Array<Double>>()
        try {
            for (epoch in 0 until modelDTO.epochs) {
                predictedLabels = model.fit(x, y)
                val accuracy = Evaluation.accuracy(y, predictedLabels)
                val response = "R: Epoch (${epoch + 1}/${modelDTO.epochs}) accuracy ${
                    "%.3f".format(
                        Locale.ENGLISH,
                        accuracy * 100
                    )
                }%"
                updateStatus(status, ModelStatus.Status.TRAINING, response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateStatus(status, ModelStatus.Status.EMPTY, "E: Failed to train model.")
            return
        }

        /** Save model **/
        try {
            ModelSerialization.save(model, trainingInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            updateStatus(status, ModelStatus.Status.EMPTY, "E: Failed to save model.")
            return
        }

        /** Evaluate model **/
        try {
            val matrix = Evaluation.confusionMatrix(y, predictedLabels)
            updateStatus(status, ModelStatus.Status.READY, "R: Confusion matrix:\n$matrix")

            val testData = DataLoader.loadImageData(
                "${Utils.DATA_PATH}/${modelDTO.testDataPath}",
                modelDTO.testDataSize
            )
            val testLabel = DataLoader.loadLabelData(
                "${Utils.DATA_PATH}/${modelDTO.testLabelsPath}"
                , modelDTO.testDataSize
            )
            val (xTest, yTest) = DataLoader.shuffle(testData, testLabel, modelDTO.testDataSize)

            val accuracy = model.test(xTest, yTest)
            updateStatus(status, ModelStatus.Status.READY, "R: Accuracy: $accuracy")
        } catch (e: Exception) {
            e.printStackTrace()
            updateStatus(status, ModelStatus.Status.READY, "E: Failed to evaluate model.")
            return
        }
    }

    private fun servicePostClassify(exchange: HttpExchange) {
        /** Load model **/
        val modelName: String
        val model: CNN
        try {
            modelName = exchange.requestHeaders["Model-Name"]?.firstOrNull()
                ?: throw IllegalArgumentException()
            model = ModelSerialization.load(modelName)
            model.build()
        } catch(e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
            reply("E: Model-Name header not found.", 404, exchange)
            return
        } catch(e: Exception) {
            e.printStackTrace()
            reply("E: Failed to load model.", 404, exchange)
            return
        }

        /** Read image **/
        val imageToPrediction: Matrix
        val imageFile: File
        try {
            val imageData = exchange.requestBody.readBytes()
            File(Utils.TEMP_DATA_PATH).mkdirs()
            imageFile = File(Utils.TEMP_DATA_PATH + "/temp_image.png")
            imageFile.writeBytes(imageData)
            imageToPrediction = Preprocessing.fileToMatrix(imageFile)
            imageFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            reply("E: Failed to parse image.", 400, exchange)
            return
        }

        /** Predict **/
        try {
            val prediction = model.predict(imageToPrediction)
            val label = Evaluation.valuesToLabel(prediction)
            reply(label.toString(), 200, exchange)
        } catch (e: Exception) {
            e.printStackTrace()
            reply("E: Failed to predict image.", 400, exchange)
            return
        }
    }

    private fun serviceDeleteModel(exchange: HttpExchange) {
        val modelName = exchange.requestURI.query.trim('/').split("/")[1]
        val success = ModelSerialization.remove(modelName)
        when(success) {
            true -> reply("R: Model deleted", 200, exchange)
            false -> reply("E: Failed to delete model", 400, exchange)
        }
    }
}