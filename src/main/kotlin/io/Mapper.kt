package com.ertools.io

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.VisibilityChecker
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object Mapper {
    fun getObjectMapper(): ObjectMapper = jacksonObjectMapper()
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
}