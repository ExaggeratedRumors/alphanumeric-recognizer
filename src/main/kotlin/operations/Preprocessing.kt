package com.ertools.operations

import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Preprocessing {
    fun fileToMatrix(file: File): Matrix {
        val image: BufferedImage = ImageIO.read(file)
        val resizedImage = BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY)
        val graphics = resizedImage.createGraphics()
        graphics.drawImage(image, 0, 0, 28, 28, null)
        graphics.dispose()

        val matrix = (0 until 28).map { row ->
            (0 until 28).map { column ->
                (resizedImage.getRGB(row, column) and 0xFF) / 255.0
            }.toTypedArray()
        }.toTypedArray().toMatrix()

        return matrix
    }
}