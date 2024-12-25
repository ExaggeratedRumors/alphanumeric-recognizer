package com.ertools.server

class ModelStatus(
    val modelName: String,
    var status: Status = Status.EMPTY,
    var info: String = ""
) {

    fun update(status: Status, info: String) {
        this.status = status
        this.info += "\n$info"
    }

    enum class Status {
        EMPTY, PREPARING, BUILT, TRAINING, READY
    }
}