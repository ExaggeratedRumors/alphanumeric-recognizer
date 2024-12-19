package com.ertools.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

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
        val response = """
            <html>
            <head><title>Train</title></head>
            <body>
                <h1>Train</h1>
                <p>Train model</p>
            </body>
            </html>
        """
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
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

    private fun serviceDeleteModel(exchange: HttpExchange) {
        val modelId = exchange.requestURI.query.split("=").last()

        //ModelSerialization.remove()

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