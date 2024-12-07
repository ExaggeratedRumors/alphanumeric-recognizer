import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Conv
import com.ertools.model.MaxPool
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class TestPooling {

    @Test
    fun `max pooling layer response`() {
        val inputMatrix = arrayOf(
            arrayOf(3.0, 6.0, 7.0, 5.0, 3.0, 5.0),
            arrayOf(6.0, 2.0, 9.0, 1.0, 2.0, 7.0),
            arrayOf(0.0, 9.0, 3.0, 6.0, 0.0, 6.0),
            arrayOf(2.0, 6.0, 1.0, 8.0, 8.0, 9.0),
            arrayOf(2.0, 0.0, 2.0, 3.0, 7.0, 5.0),
            arrayOf(9.0, 2.0, 2.0, 8.0, 9.0, 7.0)
        ).toMatrix().matrixFlatten()
        
        val expectedOutputMatrix = arrayOf(
            arrayOf(6.0, 9.0, 7.0),
            arrayOf(9.0, 8.0, 9.0),
            arrayOf(9.0, 8.0, 9.0)
        )
        
        val maxPool = MaxPool(
            poolSize = 2,
            stride = 2
        )
        val outputMatrix = maxPool.response(inputMatrix).data[0].toMatrix().reconstructMatrix(3)

        outputMatrix.data.forEachIndexed { j, row ->
            row.forEachIndexed { i, value ->
                assertEquals(value, expectedOutputMatrix[j][i], 0.001) {
                    "E: Expected ${expectedOutputMatrix[j][i]} but got $value."
                }
            }
        }
    }

    @Test
    fun `max pooling layer error`() {
        val inputMatrix = arrayOf(
            arrayOf(3.0, 6.0, 7.0, 5.0, 3.0, 5.0),
            arrayOf(6.0, 2.0, 9.0, 1.0, 2.0, 7.0),
            arrayOf(0.0, 9.0, 3.0, 6.0, 0.0, 6.0),
            arrayOf(2.0, 6.0, 1.0, 8.0, 8.0, 9.0),
            arrayOf(2.0, 0.0, 2.0, 3.0, 7.0, 5.0),
            arrayOf(9.0, 2.0, 2.0, 8.0, 9.0, 7.0)
        ).toMatrix().matrixFlatten()

        val expectedInputMatrix = arrayOf(
            arrayOf(0.0, 6.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 9.0, 0.0, 0.0, 7.0),
            arrayOf(0.0, 9.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 8.0, 0.0, 9.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(9.0, 0.0, 0.0, 8.0, 9.0, 0.0)
        )

        val conv = Conv(
            kernel = 6,
            stride = 1,
            padding = 0,
            filtersAmount = 1
        ).apply { bind() }
        val maxPool = MaxPool(
            poolSize = 2,
            stride = 2
        ).apply { bind(conv) }

        val outputMatrix = maxPool.response(inputMatrix)
        val outputError = maxPool.error(outputMatrix).data[0].toMatrix().reconstructMatrix(6)

        outputError.data.forEachIndexed { j, row ->
            row.forEachIndexed { i, value ->
                assertEquals(value, expectedInputMatrix[j][i], 0.001) {
                    "E: Expected ${expectedInputMatrix[j][i]} but got $value."
                }
            }
        }


    }
}