package com.ertools.model

class CNN(
    private val inputHeight: Int,
    private val inputWidth: Int
) {

    /**************/
    /** Privates **/
    /**************/

    private fun flatten(data2d: Array<DoubleArray>): DoubleArray {
        return data2d.map { it.toTypedArray() }.toTypedArray().flatten().toDoubleArray()
    }
}