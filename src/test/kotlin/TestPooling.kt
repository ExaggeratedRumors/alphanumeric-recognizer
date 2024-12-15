import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.CNN
import com.ertools.network.Conv
import com.ertools.model.Input
import com.ertools.model.MaxPool
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class TestPooling {

    @Test
    fun `max pooling layer response`() {
        /*
        Pooling input image is 6x6, so before Conv (3x3) layer it must be 8x8 image.
         */

        val inputMatrix = arrayOf(
            arrayOf(3.0, 6.0, 7.0, 5.0, 3.0, 5.0),
            arrayOf(6.0, 2.0, 9.0, 1.0, 2.0, 7.0),
            arrayOf(0.0, 9.0, 3.0, 6.0, 0.0, 6.0),
            arrayOf(2.0, 6.0, 1.0, 8.0, 8.0, 9.0),
            arrayOf(2.0, 0.0, 2.0, 3.0, 7.0, 5.0),
            arrayOf(9.0, 2.0, 2.0, 8.0, 9.0, 7.0)
        ).toMatrix().matrixFlatten().transpose()
        
        val expectedOutputMatrix = arrayOf(
            arrayOf(6.0, 9.0, 7.0),
            arrayOf(9.0, 8.0, 9.0),
            arrayOf(9.0, 8.0, 9.0)
        )

        val maxPool = MaxPool(
            poolSize = 2,
            stride = 2
        )
        val testCNN = CNN(
            listOf(
                Input(8, 8),
                Conv(1, 3),
                maxPool
            )
        )
        testCNN.build()

        val outputMatrix = maxPool.response(inputMatrix).reconstructMatrix(3)

        expectedOutputMatrix.forEachIndexed { j, row ->
            row.forEachIndexed { i, value ->
                assertEquals(value, outputMatrix.data[j][i], 0.001) {
                    "E: Expected ${outputMatrix.data[j][i]} but got $value."
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
        ).toMatrix().matrixFlatten().transpose()

        val expectedInputMatrix = arrayOf(
            arrayOf(0.0, 6.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 9.0, 0.0, 0.0, 7.0),
            arrayOf(0.0, 9.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 8.0, 0.0, 9.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(9.0, 0.0, 0.0, 8.0, 9.0, 0.0)
        )

        val maxPool = MaxPool(
            poolSize = 2,
            stride = 2
        )
        val testCNN = CNN(
            listOf(
                Input(8, 8),
                Conv(1, 3),
                maxPool
            )
        )
        testCNN.build()

        val outputMatrix = maxPool.response(inputMatrix)
        val outputError = maxPool.error(outputMatrix).reconstructMatrix(6)

        expectedInputMatrix.forEachIndexed { j, row ->
            row.forEachIndexed { i, value ->
                assertEquals(value, outputError.data[j][i], 0.001) {
                    "E: Expected ${outputError.data[j][i]} but got $value."
                }
            }
        }


    }
}