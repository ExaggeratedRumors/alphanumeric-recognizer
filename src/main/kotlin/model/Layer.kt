package com.ertools.model

abstract class Layer<I, O> (
    layerSize: Int
){
    var size: Int = layerSize
        protected set

    protected var previousLayer: Layer<*, *>? = null
    protected var nextLayer: Layer<*, *>? = null

    fun bind(previousLayer: Layer<*, *>) {
        this.previousLayer = previousLayer
        initialize()
    }
    abstract fun initialize()
    abstract fun response(input: I): O
    abstract fun error(input: O): I
}