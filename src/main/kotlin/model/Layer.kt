package com.ertools.model

import com.ertools.common.Matrix

abstract class Layer {
    var outputHeight: Int = 0
        protected set
    var outputWidth: Int = 0
        protected set
    var previousLayer: Layer? = null
        protected set
    var nextLayer: Layer? = null
        protected set

    fun bind(previousLayer: Layer? = null, nextLayer: Layer? = null) {
        this.previousLayer = previousLayer
        this.nextLayer = nextLayer
        initialize()
    }
    abstract fun initialize()
    abstract fun response(input: Matrix): Matrix
    abstract fun error(input: Matrix): Matrix
}