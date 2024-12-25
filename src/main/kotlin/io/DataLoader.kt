package com.ertools.io

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.common.Utils
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object DataLoader {
    data class ImageSetData(
        val amount: Int,
        val rows: Int,
        val columns: Int,
        val data: List<Matrix>
    )

    data class LabelSetData(
        val amount: Int,
        val labelsAmount: Int,
        val labels: List<Array<Double>>
    )

    fun loadLabelData(filePath: String, size: Int = Int.MAX_VALUE): LabelSetData {
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
        val data = labels.map { label ->
            uniqueLabels.indices.map { index ->
                if (index == label) 1.0
                else 0.0
            }.toTypedArray()
        }
        val labelsData = LabelSetData(
            amount = data.size,
            labelsAmount = uniqueLabels.size,
            labels = data
        )
        return labelsData
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
            Array(imageSize) { pixel ->
                try {
                    (buffer.get().toInt() and 0xFF) / 255.0
                } catch (e: Exception) {
                    e.printStackTrace()
                    error("E: Data loader failed to load ${pixel + 1} sample of $imagesAmount total.")
                }
            }.toMatrix().reconstructMatrix(rows)
        }.toList()

        return ImageSetData(
            amount = imagesAmount,
            rows = rows,
            columns = columns,
            data = data
        )
    }

    fun shuffle(x: ImageSetData, y: LabelSetData, amount: Int): Pair<List<Matrix>, List<Array<Double>>> {
        require(x.amount == y.amount && x.amount >= amount) {
            "E: Amount of images and labels must be the same." +
                    "\nGot: ${x.amount} and ${y.amount}."
        }
        val shuffledIndices = (0 until amount).shuffled()
        val newX = shuffledIndices.map { x.data[it] }
        val newY = shuffledIndices.map { y.labels[it] }
        return Pair(newX, newY)
    }

    fun loadLabels(labels: List<Int>): List<Char> {
        val mappingFile = File(Utils.LABELS_BALANCED_DICTIONARY)
        val mapping = mappingFile.readLines().map { it.split(" ")[1].toInt().toChar() }
        return labels.map { mapping[it] }
    }

    fun getFullDataInfo(dir: String): String {
        val info = sequence {
            yield("Files in $dir:")
            File(dir).list()?.forEach {
                yield(it)
            }
        }.toList().joinToString("\n")
        return info
    }
}