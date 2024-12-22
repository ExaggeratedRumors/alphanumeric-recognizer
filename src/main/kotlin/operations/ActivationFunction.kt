package com.ertools.operations

import kotlin.math.exp
import kotlin.math.pow

sealed class ActivationFunction {
    abstract fun invoke(x: Array<Double>, derivative: Boolean = false): Array<Double>

    companion object {
        fun fromString(type: String): ActivationFunction {
            return when (type.lowercase()) {
                "linear" -> Linear
                "tanh" -> Tanh
                "relu" -> Relu
                "sigmoid" -> Sigmoid
                "softmax" -> Softmax
                else -> throw IllegalArgumentException("Invalid activation function")
            }
        }
    }

    data object Linear : ActivationFunction() {
        override fun invoke(x: Array<Double>, derivative: Boolean): Array<Double> {
            return if (derivative) x.map { 1.0 }.toTypedArray()
            else x
        }
    }

    data object Tanh : ActivationFunction() {
        override fun invoke(x: Array<Double>, derivative: Boolean): Array<Double> {
            return if (derivative) x.map { 1 - it.pow(2.0) }.toTypedArray()
            else x.map { kotlin.math.tanh(it) }.toTypedArray()
        }
    }

    data object Relu : ActivationFunction() {
        override fun invoke(x: Array<Double>, derivative: Boolean): Array<Double> {
            return if (derivative) x.map { if (it > 0) 1.0 else 0.0 }.toTypedArray()
            else x.map { 0.0.coerceAtLeast(it) }.toTypedArray()
        }
    }

    data object Sigmoid : ActivationFunction() {
        override fun invoke(x: Array<Double>, derivative: Boolean): Array<Double> {
            return if (derivative) x.map { it * (1.0 - it) }.toTypedArray()
            else x.map { 1.0 / (1.0 + exp(-it)) }.toTypedArray()
        }
    }

    data object Softmax : ActivationFunction() {
        override fun invoke(x: Array<Double>, derivative: Boolean): Array<Double> {
            return if (derivative) x.map { 1.0 }.toTypedArray()
            else {
                val expVal = x.map { exp(it) }
                val sumVal = expVal.sum()
                expVal.map { it / sumVal }.toTypedArray()
            }
        }
    }
}