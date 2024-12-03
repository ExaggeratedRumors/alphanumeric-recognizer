package com.ertools.model

import kotlin.math.exp
import kotlin.math.pow

object ActivationFunction {
    fun linear(x: Array<Double>, derivative: Boolean = false): Array<Double> {
        return if(derivative) x.map { 1.0 }.toTypedArray()
        else x
    }

    fun tanh(x: Array<Double>, derivative: Boolean = false): Array<Double> {
        return if(derivative) x.map { 1 - it.pow(2.0) }.toTypedArray()
        else x.map { kotlin.math.tanh(it) }.toTypedArray()
    }

    fun relu(x: Array<Double>, derivative: Boolean = false): Array<Double> {
        return if(derivative) x.map { if(it > 0) 1.0 else 0.0 }.toTypedArray()
        else x.map { 0.0.coerceAtLeast(it) }.toTypedArray()
    }

    fun sigmoid(x: Array<Double>, derivative: Boolean = false): Array<Double> {
        return if(derivative) x.map { it * (1.0 - it) }.toTypedArray()
        else x.map { 1.0 / (1.0 + exp(-it)) }.toTypedArray()
    }

    fun softmax(x: Array<Double>, derivative: Boolean = false): Array<Double> {
        return if(derivative) x.map { 1.0 }.toTypedArray()
        else {
            val expVal = x.map { exp(it) }
            val sumVal = expVal.sum()
            expVal.map { it / sumVal }.toTypedArray()
        }
    }
}