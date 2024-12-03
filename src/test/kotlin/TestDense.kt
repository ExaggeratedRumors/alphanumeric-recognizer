import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Dense
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestDense {

    @Test
    fun `dense response`() {
        val flatLayerOutput = arrayOf(3.185, 11.995, 3.27, 12.03)
        val denseWeights = arrayOf(
            arrayOf(0.1, -0.2, 0.1, 0.3),
            arrayOf(0.2, 0.1, 0.5, -0.3)
        ).toMatrix()
        val expectedOutput = arrayOf(1.8555, -0.1375)

        val dense = Dense(2)
        dense.loadWeights(denseWeights)
        val response = dense.response(flatLayerOutput)

        expectedOutput.forEachIndexed { i, _ ->
            assertEquals(expectedOutput[i], response[i], 0.0001)
        }
    }

    @Test
    fun `dense error`() {
        val responseVector = arrayOf(1.8555, -0.1375)
        val expectedVector = arrayOf(0.0, 1.0)
        val diff = responseVector.zip(expectedVector).map { it.first - it.second }.toTypedArray()
        val dense = Dense(2)
        val error = dense.error(diff)
        val expectedError = arrayOf(1.8555, -1.1375)
        expectedError.forEachIndexed { i, _ ->
            assertEquals(expectedError[i], error[i], 0.0001)
        }
    }
}