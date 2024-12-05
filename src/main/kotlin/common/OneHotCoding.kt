package com.ertools.common

class OneHotCoding {
    fun encode(value: Int, size: Int): Array<Double> {
        require(value < size) { "E: Value must be less than size." }
        return Array(size) { i -> if(i == value) 1.0 else 0.0 }
    }
}