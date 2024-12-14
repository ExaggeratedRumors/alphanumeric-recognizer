package com.ertools.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class ServerHandler: HttpHandler {
    override fun handle(exchange: HttpExchange) {
        try {
            when(exchange.requestMethod) {
                "GET" -> serviceGet(exchange)
                "POST" -> servicePost(exchange)
                "DELETE" -> serviceDelete(exchange)
                "PUT" -> servicePut(exchange)
                "PATCH" -> servicePatch(exchange)
                else -> generateErrorResponse()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*************/
    /** Private **/
    /*************/
    private fun servicePost(exchange: HttpExchange) {
        TODO()
    }

    private fun serviceGet(exchange: HttpExchange) {
        val requestURI = exchange.requestURI.toString()
        val filesIds = requestURI
            .trim('/')
            .split("_")
            .mapNotNull { it.toIntOrNull() }
            .distinct()
        if (filesIds.isEmpty()) generateErrorResponse()
        val data = getData(filesIds)
        val response = if (data == null) generateErrorResponse()
        else generateResponse(data)

        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(response.toByteArray()) }
    }

    private fun serviceDelete(exchange: HttpExchange) {
        TODO()
    }

    private fun servicePut(exchange: HttpExchange) {
        TODO()
    }

    private fun servicePatch(exchange: HttpExchange) {
        TODO()
    }

    private fun getData(ids: List<Int>): List<Pair<String, Int>>? {
        TODO()
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