import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Conv
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestConv {
    @Test
    fun `convolution response`() {
        val inputMatrix = arrayOf(
            arrayOf(8.5, 0.65, 1.2),
            arrayOf(9.5, 0.8, 1.3),
            arrayOf(9.9, 0.8, 0.5),
            arrayOf(9.0, 0.9, 1.0)
        ).toMatrix()

        val kernel = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()

        val expectedOutput = arrayOf(
            arrayOf(3.185, 11.995),
            arrayOf(3.27, 12.03)
        )

        val conv = Conv(2, 3, 1, 0) { it }
        conv.loadFilters(kernel)
        val outputMatrix = conv.response(inputMatrix)

        outputMatrix.data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(value, expectedOutput[i][j], 0.001) {
                    "Expected ${expectedOutput[i][j]} but got $value"
                }
            }
        }

    }
}