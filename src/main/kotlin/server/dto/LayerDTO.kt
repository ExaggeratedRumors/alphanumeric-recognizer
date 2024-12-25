package com.ertools.server.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = LayerDTO.InputDTO::class, name = "Input"),
    JsonSubTypes.Type(value = LayerDTO.ConvDTO::class, name = "Conv"),
    JsonSubTypes.Type(value = LayerDTO.FlattenDTO::class, name = "Flatten"),
    JsonSubTypes.Type(value = LayerDTO.DenseDTO::class, name = "Dense"),
    JsonSubTypes.Type(value = LayerDTO.DropoutDTO::class, name = "Dropout"),
    JsonSubTypes.Type(value = LayerDTO.MaxPoolDTO::class, name = "MaxPool")
)
sealed class LayerDTO {
    class InputDTO(
        val height: Int,
        val width: Int,
        val channels: Int
    ): LayerDTO()

    class ConvDTO(
        val filtersAmount: Int,
        val kernel: Int,
        val stride: Int,
        val padding: Int,
        val activation: String,
        val weightRange: Double
    ): LayerDTO()

    class MaxPoolDTO(
        val poolSize: Int,
        val stride: Int,
        val padding: Int
    ): LayerDTO()

    class FlattenDTO: LayerDTO()

    class DenseDTO(
        val neurons: Int,
        val activation: String,
        val weightRange: Double
    ): LayerDTO()

    class DropoutDTO(
        val rate: Double
    ): LayerDTO()

    class LayerDTODeserializer : JsonDeserializer<LayerDTO>() {
        override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): LayerDTO {
            val node: JsonNode = parser.codec.readTree(parser)
            return when (val type = node.get("type").asText()) {
                "Input" -> InputDTO(
                    height = node.get("height").asInt(),
                    width = node.get("width").asInt(),
                    channels = node.get("channels").asInt()
                )
                "Conv" -> ConvDTO(
                    filtersAmount = node.get("filtersAmount").asInt(),
                    kernel = node.get("kernel").asInt(),
                    stride = node.get("stride").asInt(),
                    padding = node.get("padding").asInt(),
                    activation = node.get("activation").asText(),
                    weightRange = node.get("weightRange").asDouble()
                )
                "MaxPool" -> MaxPoolDTO(
                    poolSize = node.get("poolSize").asInt(),
                    stride = node.get("stride").asInt(),
                    padding = node.get("padding").asInt()
                )
                "Flatten" -> FlattenDTO()
                "Dense" -> DenseDTO(
                    neurons = node.get("neurons").asInt(),
                    activation = node.get("activation").asText(),
                    weightRange = node.get("weightRange").asDouble()
                )
                "Dropout" -> DropoutDTO(
                    rate = node.get("rate").asDouble()
                )
                else -> throw IllegalArgumentException("Unknown layer type: $type")
            }
        }
    }
}