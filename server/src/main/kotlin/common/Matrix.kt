package com.ertools.common

import com.fasterxml.jackson.annotation.JsonIgnore


class Matrix(
    val rows: Int = 1,
    val columns: Int = 1,
    @JsonIgnore private val initalizer: (Int) -> (Double) = { 0.0 }
) {
    enum class FlattenOrientation {
        Horizontal,
        Vertical
    }

    companion object {
        fun Array<Array<Double>>.toMatrix(): Matrix {
            val rows = this.size
            val columns = this[0].size
            val matrixData = this
            return Matrix(rows, columns).apply { this.data = matrixData }
        }

        fun Array<Double>.toMatrix(): Matrix {
            val rows = 1
            val columns = this.size
            val matrixData = arrayOf(this)
            return Matrix(rows, columns).apply { this.data = matrixData }
        }
    }

    fun asVector(): Array<Double> {
        require(rows == 1 || columns == 1) { "Matrix must be a vector. Received $rows rows and $columns columns." }
        if(columns == 1) return this.transpose().data[0]
        return this.data[0]
    }

    var data: Array<Array<Double>> = Array(rows) { Array(columns) { initalizer.invoke(it) } }


    /**
     * Flatten orientation determines the order of flattening - by rows (vertical) or by columns (horizontal).
     */
    fun matrixFlatten(orientation: FlattenOrientation = FlattenOrientation.Horizontal): Matrix {
        if(rows == 1) return this
        if(orientation == FlattenOrientation.Vertical) {
            val result = Array(1) { Array(rows * columns) { 0.0 } }
            (0 until columns).forEach { j ->
                (0 until rows).forEach { i ->
                    result[0][j * columns + i] = data[i][j]
                }
            }
            return result.toMatrix()
        } else {
            return this.data.flatten().toTypedArray().toMatrix()
        }
    }

    /**
     * Reverse-flatten operation. Flatten orientation determines the order of flattening - by rows (vertical)
     * or by columns (horizontal).
     */
    fun reconstructMatrix(rows: Int, orientation: FlattenOrientation = FlattenOrientation.Horizontal): Matrix {
        val vector = this.asVector()
        val columns = vector.size / rows
        if(orientation == FlattenOrientation.Vertical) {
            val result = Array(rows) { Array(columns) { 0.0 } }
            (0 until rows).forEach { i ->
                (0 until columns).forEach { j ->
                    result[i][j] = vector[i * columns + j]
                }
            }
            return result.toMatrix()
        } else {
            return Array(rows) { row ->
                Array(columns) { column ->
                    vector[row * columns + column]
                }
            }.toMatrix()
        }
    }

    /**
     * Apply padding to the matrix by adding zeros to the borders.
     */
    fun applyPadding(padding: Int): Matrix {
        require(padding >= 0) { "E: Invalid padding value" }
        if(padding == 0) return this
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

    fun plus(rightMatrix: Matrix): Matrix {
        require(this.rows == rightMatrix.rows && this.columns == rightMatrix.columns) { "Invalid matrix dimensions" }
        val result = Array(this.rows) { Array(this.columns) { 0.0 } }
        (0 until this.rows).forEach { i ->
            (0 until this.columns).forEach { j ->
                result[i][j] = this.data[i][j] + rightMatrix.data[i][j]
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

    /**
     * Transpose matrix.
     */
    fun transpose(): Matrix {
        val result = Array(columns) { Array(rows) { 0.0 } }
        (0 until rows).forEach { i ->
            (0 until columns).forEach { j ->
                result[j][i] = this.data[i][j]
            }
        }
        return result.toMatrix()
    }

    /**
     * Slice matrix to a smaller one.
     */
    fun slice(rowIndices: IntRange, colIndices: IntRange): Matrix {
        require(rowIndices.first >= 0 && rowIndices.last < rows) {
            "Invalid row indices. Got ${rowIndices.first}..${rowIndices.last} from matrix with $rows rows."
        }
        require(colIndices.first >= 0 && colIndices.last < columns) {
            "Invalid column indices. Got ${colIndices.first}..${colIndices.last} from matrix with $columns columns."
        }
        val result = rowIndices.map { rowIndex ->
            colIndices.map { colIndex ->
                this.data[rowIndex][colIndex]
            }.toTypedArray()
        }.toTypedArray()
        return result.toMatrix()
    }

    /**
     * Matrix rotation for full-convolution operation.
     */
    fun rotate180degree(): Matrix {
        val rotated = Array(this.rows) { Array(this.columns) { 0.0 } }
        for (i in 0 until this.rows) {
            for (j in 0 until this.columns) {
                rotated[i][j] = this.data[this.rows - i - 1][this.columns - j - 1]
            }
        }
        return rotated.toMatrix()
    }

    fun applyForEachRow(operation: (Int, Array<Double>) -> Array<Double>): Matrix {
        val result = this.data.mapIndexed { i, v -> operation.invoke(i, v) }.toTypedArray()
        return result.toMatrix()
    }

    /**
     * Print matrix for a test.
     */
    fun print() {
        data.forEach { row ->
            row.forEach { value ->
                print("$value ")
            }
            println()
        }
    }
}
