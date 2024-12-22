package com.ertools.server.dto

import com.ertools.operations.ActivationFunction


data class TrainModelRequest(
    val modelName: String,
    val trainDataPath: String,
    val testDataPath: String,
    val trainDataSize: Int,
    val testDataSize: Int,
    val epochs: Int,
    val learningRate: Double,
    val batchSize: Int,
    val layers: List<LayerDTO>
)

sealed class LayerDTO(
    val name: String
) {
    class InputDTO(
        val height: Int,
        val width: Int,
        val channels: Int
    ): LayerDTO("input")

    class ConvDTO(
        val filtersAmount: Int,
        val kernel: Int,
        val stride: Int,
        val padding: Int,
        val activation: String,
        val weightRange: Double
    ): LayerDTO("conv")

    class MaxPoolDTO(
        val poolSize: Int,
        val stride: Int,
        val padding: Int
    ): LayerDTO("maxpool")

    class FlattenDTO: LayerDTO("flatten")

    class DenseDTO(
        val neurons: Int,
        val activation: String,
        val weightRange: Double
    ): LayerDTO("dense")

    class DropoutDTO(
        val rate: Double
    ): LayerDTO("dropout")
}