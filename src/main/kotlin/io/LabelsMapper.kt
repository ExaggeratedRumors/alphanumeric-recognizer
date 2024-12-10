package com.ertools.io

import com.ertools.common.Utils
import java.io.File

object LabelsMapper {
    fun loadLabels(labels: List<Int>): List<Char> {
        val mappingFile = File(Utils.LABELS_BALANCED_DICTIONARY)
        val mapping = mappingFile.readLines().map { it.split(" ")[1].toInt().digitToChar() }
        return labels.map { mapping[it] }
    }
}