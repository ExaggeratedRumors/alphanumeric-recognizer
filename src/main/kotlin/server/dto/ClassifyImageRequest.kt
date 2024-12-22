package com.ertools.server.dto

data class ClassifyImageRequest(
    val modelName: String,
    val imageData: String
)
