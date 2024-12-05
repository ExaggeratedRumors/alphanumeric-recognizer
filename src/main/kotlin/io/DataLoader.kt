package com.ertools.io

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object DataLoader {
    data class ImageSetData(
        val amount: Int,
        val rows: Int,
        val columns: Int,
        val data: List<DoubleArray>
    )

    fun loadLabelData(filePath: String, size: Int = Int.MAX_VALUE): List<Array<Double>> {
        val buffer: ByteBuffer = ByteBuffer
            .wrap(File(filePath).readBytes())
            .order(ByteOrder.BIG_ENDIAN)
            .also { it.int }
        val labelsAmount: Int = buffer.int.coerceAtMost(size)
        val labels = IntArray(labelsAmount)
        labels.indices.forEach {
            try {
                labels[it] = buffer.get().toInt() and 0xFF
            } catch (e: Exception) {
                e.printStackTrace()
                error("E: Data loader failed to load ${it + 1} sample of $labelsAmount total.")
            }
        }

        val uniqueLabels = labels.distinct().sorted()
        return labels.map { label ->
            uniqueLabels.indices.map { index ->
                if(index == label) 1.0
                else 0.0
            }.toTypedArray()
        }
    }

    fun loadImageData(filePath: String, size: Int = Int.MAX_VALUE): ImageSetData {
        val buffer: ByteBuffer = ByteBuffer
            .wrap(File(filePath).readBytes())
            .order(ByteOrder.BIG_ENDIAN)
            .also { it.int }

        val imagesAmount = buffer.int.coerceAtMost(size)
        val rows = buffer.int
        val columns = buffer.int
        val imageSize = rows * columns

        val data = Array(imagesAmount) { sample ->
            DoubleArray(imageSize) { pixel ->
                try {
                    (buffer.get().toInt() and 0xFF) / 255.0
                } catch (e: Exception) {
                    e.printStackTrace()
                    error("E: Data loader failed to load ${pixel + 1} sample of $imagesAmount total.")
                }
            }
        }.toList()

        return ImageSetData(
            amount = imagesAmount,
            rows = rows,
            columns = columns,
            data = data
        )
    }
}