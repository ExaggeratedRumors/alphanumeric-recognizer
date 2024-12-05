package com.ertools.model

import com.ertools.common.Matrix

abstract class Layer (
    layerSize: Int
){
    var size: Int = layerSize
        protected set

    protected var previousLayer: Layer? = null
    protected var nextLayer: Layer? = null

    fun bind(previousLayer: Layer? = null, nextLayer: Layer? = null) {
        this.previousLayer = previousLayer
        this.nextLayer = nextLayer
        initialize()
    }
    abstract fun initialize()
    abstract fun response(input: Matrix): Matrix
    abstract fun error(input: Matrix): Matrix
}