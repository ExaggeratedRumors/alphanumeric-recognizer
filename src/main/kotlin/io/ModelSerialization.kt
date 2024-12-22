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
    fun save(cnn: CNN, filename: String) {
        val objectMapper = Mapper.getObjectMapper()

        val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cnn)
        java.io.File("${Utils.MODELS_PATH}/$filename").writeText(json)
    }

    fun load(filename: String): CNN {
        val objectMapper = Mapper.getObjectMapper()
        val json = java.io.File("${Utils.MODELS_PATH}/$filename").readText()
        return objectMapper.readValue(json, CNN::class.java)
    }

    fun remove(filename: String): Boolean {
        val success = java.io.File("${Utils.MODELS_PATH}/$filename").delete()
        return success
    }

    fun getModelsList(): List<String> {
        return java.io.File(Utils.MODELS_PATH).list()?.toList() ?: emptyList()
    }
}