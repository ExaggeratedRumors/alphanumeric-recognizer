package com.ertools.common


class Matrix(
    val rows: Int,
    val columns: Int,
    private val initalizer: (Int) -> (Double) = { 0.0 }
) {
    companion object {
        fun Array<Array<Double>>.toMatrix(): Matrix {
            val rows = this.size
            val columns = this[0].size
            val matrixData = this
            return Matrix(rows, columns).apply { this.data = matrixData }
        }

        fun reconstructMatrix(input: Array<Double>, rows: Int): Matrix {
            val columns = input.size / rows
            return Array(rows) { row ->
                Array(columns) { column ->
                    input[row * columns + column]
                }
            }.toMatrix()
        }
    }

    var data: Array<Array<Double>> = Array(rows) { Array(columns) { initalizer.invoke(it) } }

    fun flatten(): Array<Double> {
        val result = Array(rows * columns) { 0.0 }
        (0 until columns).forEach { j ->
            (0 until rows).forEach { i ->
                result[j * columns + i] = data[i][j]
            }
        }
        return result
    }

    fun applyPadding(padding: Int): Matrix {
        require(padding >= 0) { "E: Invalid padding value" }
        val paddedArray = Array(rows + padding * 2) {
            Array(columns + padding * 2) { 0.0 }
        }
        (0 until rows).forEach { row ->
            (0 until columns).forEach { column ->
                paddedArray[padding + row][column + padding] = this.data[row][column]
            }
        }
        return paddedArray.toMatrix()
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
        return result.toMatrix()
    }

    fun minus(rightMatrix: Matrix): Matrix {
        require(this.rows == rightMatrix.rows && this.columns == rightMatrix.columns) { "Invalid matrix dimensions" }
        val result = Array(this.rows) { Array(this.columns) { 0.0 } }
        (0 until this.rows).forEach { i ->
            (0 until this.columns).forEach { j ->
                result[i][j] = this.data[i][j] - rightMatrix.data[i][j]
            }
        }
        return result.toMatrix()
    }

    fun mul(factor: Double): Matrix {
        val result = Array(this.rows) { Array(this.columns) { 0.0 } }
        (0 until this.rows).forEach { i ->
            (0 until this.columns).forEach { j ->
                result[i][j] = this.data[i][j] * factor
            }
        }
        return result.toMatrix()
    }

    fun dot(rightVector: Array<Double>): Array<Double> {
        require(this.columns == rightVector.size) { "Invalid matrix dimensions" }
        val result = Array(this.rows) { 0.0 }
        (0 until this.rows).forEach { i ->
            (0 until this.columns).forEach { j ->
                result[i] += this.data[i][j] * rightVector[j]
            }
        }
        return result
    }

    fun transpose(): Matrix {
        val result = Array(columns) { Array(rows) { 0.0 } }
        (0 until rows).forEach { i ->
            (0 until columns).forEach { j ->
                result[j][i] = this.data[i][j]
            }
        }
        return result.toMatrix()
    }

    fun slice(rowIndices: IntRange, colIndices: IntRange): Matrix {
        require(rowIndices.first >= 0 && rowIndices.last < rows) { "Invalid row indices" }
        require(colIndices.first >= 0 && colIndices.last < columns) { "Invalid column indices" }
        val result = rowIndices.map { rowIndex ->
            colIndices.map { colIndex ->
                this.data[rowIndex][colIndex]
            }.toTypedArray()
        }.toTypedArray()
        return result.toMatrix()
    }

    fun print() {
        data.forEach { row ->
            row.forEach { value ->
                print("$value ")
            }
            println()
        }
    }
}
