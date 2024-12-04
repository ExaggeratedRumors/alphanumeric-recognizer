package com.ertools.model

class Dropout(
    factor: Double
): Layer<Array<Double>, Array<Double>>(1) {
    override fun initialize() {
        size = previousLayer!!.size
    }

    override fun response(input: Array<Double>): Array<Double> {
        TODO("Not yet implemented")
    }

    override fun error(input: Array<Double>): Array<Double> {
        TODO()
    }
}