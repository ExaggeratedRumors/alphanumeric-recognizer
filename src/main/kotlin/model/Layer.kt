package com.ertools.model

interface Layer<T> {
    fun response(input: T): T
}