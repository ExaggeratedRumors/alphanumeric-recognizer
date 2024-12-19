package com.ertools.server

import com.ertools.common.Matrix
import com.ertools.io.DataLoader
import com.ertools.io.Mapper
import com.ertools.io.ModelSerialization
import com.ertools.network.*
import com.ertools.operations.Evaluation
import com.ertools.operations.Initializer
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
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
                        else -> generateErrorResponse()
                    }
                }
                "POST" -> {
                    when(endpoint) {
                        "train" -> servicePostTrain(exchange)
                        "classify" -> servicePostClassify(exchange)
                        else -> generateErrorResponse()
                    }
                }
                "DELETE" -> {
                    when(endpoint) {
                        "model" -> serviceDeleteModel(exchange)
                        else -> generateErrorResponse()
                    }
                }
                else -> generateErrorResponse()
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
        val modelDTO = Mapper.getObjectMapper().readValue(isr, ModelDTO::class.java)

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
        val response = """
            <html>
            <head><title>Classify</title></head>
            <body>
                <h1>Classify</h1>
                <p>Classify data</p>
            </body>
            </html>
        """
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    private fun reply(response: String, code: Int, exchange: HttpExchange) {
        exchange.sendResponseHeaders(code, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    private fun serviceDeleteModel(exchange: HttpExchange) {
        try {
            val modelId = exchange.requestURI.query.trim('/').split("/")[1]
            ModelSerialization.remove(modelId)
            val response = """
                <html>
                <head><title>Delete Model</title></head>
                <body>
                    <h1>Delete Model</h1>
                    <p>Delete model</p>
                </body>
                </html>
            """
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateResponse(data: List<Pair<String, Int>>): String {
        val combinedData = data.groupBy({ it.first }, { it.second })
            .mapValues { it.value.sum() }

        val tableRows = combinedData.entries.joinToString("") {
            "<tr><td>${it.key}</td><td>${it.value}</td></tr>"
        }

        val sum = data.sumOf { it.second }

        return """
            <html>
            <head><title>Data</title></head>
            <body>
                <h1>Header</h1>
                <table border="1">
                    <tr><th>Product</th><th>Quantity</th></tr>
                    $tableRows
                </table>
                Cost: $sum
            </body>
            </html>
        """
    }

    private fun generateErrorResponse(): String {
        return """
            <html>
            <head><title>Error</title></head>
            <body>
                <h1>Error: Invalid input data</h1>
                <p>Please provide valid data</p>
            </body>
            </html>
        """
    }
}