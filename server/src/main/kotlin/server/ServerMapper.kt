package com.ertools.server

import com.ertools.server.dto.LayerDTO
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object ServerMapper {
    fun getMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(customLayerModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private fun customLayerModule(): SimpleModule {
        val module = SimpleModule()
        module.addDeserializer(LayerDTO::class.java, LayerDTO.LayerDTODeserializer())
        return module
    }
}