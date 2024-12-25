package com.ertools.io

import com.ertools.common.Utils
import com.ertools.network.CNN
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.VisibilityChecker
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

object ModelSerialization {
    private val serializationMapper: ObjectMapper = jacksonObjectMapper()
        .registerKotlinModule()
        .setVisibility(
            VisibilityChecker.Std.defaultInstance()
                .withFieldVisibility(com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY)
        )
        .setDefaultTyping(
            StdTypeResolverBuilder()
                .init(JsonTypeInfo.Id.CLASS, null)
                .inclusion(JsonTypeInfo.As.PROPERTY)
        )

    data class ModelMetadata(
        val name: String,
        val info: String
    )

    data class TrainingInfo(
        val modelName: String,
        val epochs: Int,
        val trainingDataAmount: Int,
        val batch: Int
    )

    fun save(cnn: CNN, trainingInfo: TrainingInfo) {
        val info = sequence {
            yield("Epochs: ${trainingInfo.epochs}")
            yield("Training data amount: ${trainingInfo.trainingDataAmount}")
            yield("Batch: ${trainingInfo.batch}")
            yield(cnn.info)
        }.joinToString("\n")

        val json = serializationMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cnn)
        File("${Utils.MODELS_PATH}/${trainingInfo.modelName}.${Utils.MODEL_EXTENSION}").writeText(json)
        File("${Utils.MODELS_PATH}/${trainingInfo.modelName}.${Utils.MODEL_METADATA_EXTENSION}").writeText(info)
    }

    fun load(filename: String): CNN {
        val json = File("${Utils.MODELS_PATH}/$filename.${Utils.MODEL_EXTENSION}").readText()
        return serializationMapper.readValue(json, CNN::class.java)
    }

    fun remove(filename: String): Boolean {
        File("${Utils.MODELS_PATH}/$filename.${Utils.MODEL_METADATA_EXTENSION}").delete()

        val success = File("${Utils.MODELS_PATH}/$filename.${Utils.MODEL_EXTENSION}").delete()
        return success
    }

    fun getModelsInfo(): String {
        val models = File(Utils.MODELS_PATH).list()?.toList() ?: emptyList()
        val modelsAmount = models.count { it.endsWith(Utils.MODEL_EXTENSION) }
        val infoModelsAmount = models.count { it.endsWith(Utils.MODEL_METADATA_EXTENSION) }
        val info = models.filter { it.endsWith(Utils.MODEL_METADATA_EXTENSION) }.map {
            val name = it.removeSuffix(".${Utils.MODEL_METADATA_EXTENSION}")
            val data = File("${Utils.MODELS_PATH}/$name.${Utils.MODEL_METADATA_EXTENSION}").readText()
            ModelMetadata(name, data)
        }

        val sequence = sequence {
            yield("Models amount: $modelsAmount")
            models.forEach {
                yield(it)
            }
            yield("\n")
            yield("Model with full info amount: $infoModelsAmount")
            info.forEach {
                yield("Name: ${it.name}")
                yield(it.info)
                yield("\n")
            }
        }
        return sequence.toList().joinToString("\n")
    }
}