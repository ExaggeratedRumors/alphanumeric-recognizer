package com.ertools.io

import com.ertools.common.Utils
import com.ertools.network.CNN
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.VisibilityChecker
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object ModelSerialization {
    private fun getObjectMapper(): ObjectMapper = jacksonObjectMapper()
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

    fun save(cnn: CNN, filename: String) {
        val objectMapper = getObjectMapper()

        val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cnn)
        java.io.File("${Utils.MODELS_PATH}/$filename").writeText(json)
    }

    fun load(filename: String): CNN {
        val objectMapper = getObjectMapper()
        val json = java.io.File("${Utils.MODELS_PATH}/$filename").readText()
        return objectMapper.readValue(json, CNN::class.java)
    }

    fun remove(filename: String) {
        java.io.File("${Utils.MODELS_PATH}/$filename").delete()
    }

    fun getModelsList(): List<String> {
        return java.io.File(Utils.MODELS_PATH).list()?.toList() ?: emptyList()
    }
}