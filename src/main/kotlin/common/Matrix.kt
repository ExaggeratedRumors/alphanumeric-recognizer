package com.ertools.common


class Matrix(
    val rows: Int,
    val columns: Int,
    val initalizer: (Int) -> (Double) = { 0.0 }
) {
    var data: Array<Array<Double>> = Array(rows) { Array(columns) { initalizer.invoke(it) } }

    fun flatten(): Vector {
        val flattenData = data.flatten().toTypedArray()
        return Vector(flattenData.size).apply { this.data = flattenData }
    }

    fun applyPadding(padding: Int): Matrix {
        val paddedArray = Array(rows + padding * 2) {
            Array(columns + padding * 2) { 0.0 }
        }
        (0 until rows).forEach { row ->
            (0 until columns).forEach { column ->
                paddedArray[padding + row][column + padding] = this.data[row][column]
            }
        }
        return Matrix(rows, columns).apply {
            this.data = paddedArray
        }
    }

    fun dot(rightMatrix: Matrix): Matrix {
        require(this.columns == rightMatrix.rows) { "Invalid matrix dimensions" }
        val result = Array(this.rows) { Array(rightMatrix.columns) { 0.0 } }
        (0 until this.rows).forEach { i ->
            (0 until rightMatrix.columns).forEach { j ->
                (0 until this.columns).forEach { k ->
                    result[i][j] += this.data[i][k] * rightMatrix.data[k][j]
                }
            }
        }
        return Matrix(this.rows, rightMatrix.columns).apply {
            this.data = result
        }
    }

    fun transpose(): Matrix {
        val result = Array(columns) { Array(rows) { 0.0 } }
        (0 until rows).forEach { i ->
            (0 until columns).forEach { j ->
                result[j][i] = this.data[i][j]
            }
        }
        return Matrix(columns, rows).apply {
            this.data = result
        }
    }


    fun slice(rowIndices: IntRange, colIndices: IntRange): Matrix {
        require(rowIndices.first >= 0 && rowIndices.last < rows) { "Invalid row indices" }
        require(colIndices.first >= 0 && colIndices.last < columns) { "Invalid column indices" }
        val newMatrixData = rowIndices.map { rowIndex ->
            colIndices.map { colIndex ->
                this.data[rowIndex][colIndex]
            }.toTypedArray()
        }.toTypedArray()
        return Matrix(rowIndices.count(), colIndices.count()).apply {
            this.data = newMatrixData
        }
    }
}

