package com.ertools.server

import com.ertools.common.Utils
import com.ertools.io.DataLoader
import com.ertools.io.Mapper
import com.ertools.io.ModelSerialization
import com.ertools.network.*
import com.ertools.operations.ActivationFunction
import com.ertools.operations.Evaluation
import com.ertools.operations.Initializer
import com.ertools.operations.Preprocessing
import com.ertools.server.dto.ClassifyImageRequest
import com.ertools.server.dto.LayerDTO
import com.ertools.server.dto.TrainModelRequest
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.File
import java.io.InputStreamReader
import java.util.*

class ServerHandler: HttpHandler {
    override fun handle(exchange: HttpExchange) {
        try {
            val endpoint = getEndpoint(exchange)
            when(exchange.requestMethod) {
                "GET" -> {
                    when(endpoint) {
                        "models" -> serviceGetModels(exchange)
                        "data" -> serviceGetData(exchange)
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

    private fun serviceGetModels(exchange: HttpExchange) {
        val response = """
            <html>
            <head><title>Models</title></head>
            <body>
                <h1>Server Info</h1>
                <p>Server is running</p>
            </body>
            </html>
        """
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    private fun serviceGetData(exchange: HttpExchange) {
        val response = """
            <html>
            <head><title>Data</title></head>
            <body>
                <h1>Data</h1>
                <p>Provide data</p>
            </body>
            </html>
        """
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    private fun servicePostTrain(exchange: HttpExchange) {
        val isr = InputStreamReader(exchange.requestBody)
        val modelDTO = Mapper.getObjectMapper().readValue(isr, TrainModelRequest::class.java)

        /** Build model **/
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

        val model = CNN(layers)

        /** Load training data **/
        val trainData: DataLoader.ImageSetData
        val trainLabels: DataLoader.LabelSetData

        try {
            trainData = DataLoader.loadImageData(modelDTO.trainDataPath, modelDTO.trainDataSize)
            trainLabels = DataLoader.loadLabelData(modelDTO.trainDataPath, modelDTO.trainDataSize)
        } catch (e: Exception) {
            e.printStackTrace()
            reply("E: Failed to load data.", 400, exchange)
            return
        }
        val (x, y) = DataLoader.shuffle(trainData, trainLabels, modelDTO.trainDataSize)

        /** Train model **/
        var predictedLabels = emptyList<Array<Double>>()
        for(epoch in 0 until modelDTO.epochs) {
            predictedLabels = model.fit(x, y)
            val accuracy = Evaluation.accuracy(y, predictedLabels)
            val response = "R: Epoch (${epoch + 1}/$modelDTO.epochs) accuracy ${"%.3f".format(Locale.ENGLISH, accuracy * 100)}%"
            reply(response, 200, exchange)
        }

        /** Save model **/
        ModelSerialization.save(model, modelDTO.modelName)

        /** Evaluate model **/
        val matrix = Evaluation.confusionMatrix(y, predictedLabels)
        reply(matrix, 200, exchange)

        val testData = DataLoader.loadImageData(modelDTO.testDataPath, modelDTO.testDataSize)
        val testLabel = DataLoader.loadLabelData(modelDTO.testDataPath, modelDTO.testDataSize)
        val (xTest, yTest) = DataLoader.shuffle(testData, testLabel, modelDTO.testDataSize)

        val accuracy = model.test(xTest, yTest)
        reply("R: Accuracy: $accuracy", 200, exchange)
    }

    private fun servicePostClassify(exchange: HttpExchange) {
        val isr = InputStreamReader(exchange.requestBody)

        val imageDTO = Mapper.getObjectMapper().readValue(isr, ClassifyImageRequest::class.java)
        val decodedData = Base64.getDecoder().decode(imageDTO.imageData)
        val imageFile = File("${Utils.TEMP_DATA_PATH}/temp_image_${imageDTO.imageData.hashCode()}").apply {
            createNewFile()
            writeBytes(decodedData)
        }

        val imageToPrediction = Preprocessing.fileToMatrix(imageFile)
        imageToPrediction.print()

        val model: CNN
        try {
            model = ModelSerialization.load(imageDTO.modelName)
        } catch(e: Exception) {
            e.printStackTrace()
            reply("E: Failed to load model.", 400, exchange)
            return
        }

        val prediction = model.predict(imageToPrediction)
        val label = Evaluation.valuesToLabels(prediction)

        val response = """
            <html>
            <head><title>Classify</title></head>
            <body>
            ${label.joinToString("") { "<p>${it.first}: ${it.second}</p>" }}
            </body>
            </html>
        """
        reply(response, 200, exchange)
    }

    private fun serviceDeleteModel(exchange: HttpExchange) {
        try {
            val modelId = exchange.requestURI.query.trim('/').split("/")[1]
            val success = ModelSerialization.remove(modelId)
            when(success) {
                true -> reply("R: Model deleted", 200, exchange)
                false -> reply("E: Failed to delete model", 404, exchange)
            }
        } catch (e: Exception) {
            reply("E: Error occurred during removing model", 500, exchange)
            e.printStackTrace()
        }
    }

    private fun reply(response: String, code: Int, exchange: HttpExchange) {
        exchange.sendResponseHeaders(code, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }
}