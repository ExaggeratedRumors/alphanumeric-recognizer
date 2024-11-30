package com.ertools.model

import kotlin.math.exp
import kotlin.math.pow

object ActivationFunction {
    fun tanh(x: DoubleArray, derivative: Boolean = false): DoubleArray {
        return if(derivative) x.map { 1 - it.pow(2.0) }.toDoubleArray()
        else x.map { kotlin.math.tanh(it) }.toDoubleArray()
    }

    fun relu(x: DoubleArray, derivative: Boolean = false): DoubleArray {
        return if(derivative) x.map { if(it > 0) 1.0 else 0.0 }.toDoubleArray()
        else x.map { 0.0.coerceAtLeast(it) }.toDoubleArray()
    }

    fun sigmoid(x: DoubleArray, derivative: Boolean = false): DoubleArray {
        return if(derivative) x.map { it * (1.0 - it) }.toDoubleArray()
        else x.map { 1.0 / (1.0 + exp(-it)) }.toDoubleArray()
    }

    fun softmax(x: DoubleArray, derivative: Boolean = false): DoubleArray {
        return if(derivative) x.map { 1.0 }.toDoubleArray()
        else {
            val expVal = x.map { exp(it) }
            val sumVal = expVal.sum()
            expVal.map { it / sumVal }.toDoubleArray()
        }
    }
}