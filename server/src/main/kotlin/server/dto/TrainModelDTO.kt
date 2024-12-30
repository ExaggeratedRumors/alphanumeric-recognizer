package com.ertools.server.dto

data class TrainModelDTO(
    val modelName: String,
    val trainDataPath: String,
    val trainLabelsPath: String,
    val trainDataSize: Int,
    val testDataPath: String,
    val testLabelsPath: String,
    val testDataSize: Int,
    val epochs: Int,
    val learningRate: Double,
    val batchSize: Int,
    val layers: List<LayerDTO>
)