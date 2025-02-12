package com.ertools.model

import com.ertools.common.Matrix
import com.ertools.network.*
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
    JsonSubTypes.Type(value = Dropout::class, name = "Dropout"),
    JsonSubTypes.Type(value = MaxPool::class, name = "MaxPool")
)
abstract class Layer : Serializable {
    class Dimensions(
        val height: Int = 1,
        val width: Int = 1,
        val channels: Int = 1,
        val batch: Int = 1
    )

    var dimensions: Dimensions = Dimensions()
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
    abstract fun info(): String
    abstract fun response(input: Matrix): Matrix
    abstract fun error(input: Matrix): Matrix
}