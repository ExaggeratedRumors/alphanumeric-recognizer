package com.ertools

import com.ertools.common.Utils
import com.ertools.io.DataLoader

fun main() {
    val xTrain = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TRAIN_IMAGE_DATA_FILENAME}")
    val yTrain = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TRAIN_LABEL_DATA_FILENAME}")
    val xTest = DataLoader.loadImageData("${Utils.DATA_PATH}/${Utils.TEST_IMAGE_DATA_FILENAME}")
    val yTest = DataLoader.loadLabelData("${Utils.DATA_PATH}/${Utils.TEST_LABEL_DATA_FILENAME}")
}