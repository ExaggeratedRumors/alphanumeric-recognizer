package com.ertools.model

import com.ertools.common.Matrix
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Input::class, name = "Input"),
    JsonSubTypes.Type(value = Conv::class, name = "Conv"),
    JsonSubTypes.Type(value = Flatten::class, name = "Flatten"),
    JsonSubTypes.Type(value = Dense::class, name = "Dense"),
    JsonSubTypes.Type(value = Dropout::class, name = "Dropout")
)
abstract class Layer : Serializable {
    var outputHeight: Int = 0
        protected set
    var outputWidth: Int = 0
        protected set
    @JsonIgnore
    var previousLayer: Layer? = null
        protected set
    @JsonIgnore
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