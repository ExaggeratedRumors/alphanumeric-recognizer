package com.ertools.model

import com.ertools.common.Matrix

class CNN(
    private val layers: List<Layer>
) {
    fun build() {
        val log = sequence {
            yield("I: Building CNN.")
            layers.forEachIndexed { index, layer ->
                layer.bind(layers.getOrNull(index - 1), layers.getOrNull(index + 1))
                yield("I: Layer ${layer.javaClass.simpleName} (${layer.size}) initialized.")
            }
        }.toList()
        log.forEach { println(it) }
    }

    fun fit(images: List<Matrix>) {
        images.forEach { image ->
            /** 1. Calculate response **/
            val inputLayer = layers.first() as Input
            inputLayer.response(image)

        }
    }

    /**************/
    /** Privates **/
    /**************/
}