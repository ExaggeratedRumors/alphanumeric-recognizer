import com.ertools.common.Matrix
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test


class TestConv {

    @Test
    fun `applyPadding adds correct padding to the matrix`() {
        val inputMatrixData = arrayOf(
            arrayOf(0.5, 0.3, 0.2),
            arrayOf(0.6, 0.9, 0.1),
            arrayOf(0.7, 0.4, 0.5),
        )
        val matrix = Matrix(
            rows = inputMatrixData.size,
            columns = inputMatrixData[0].size
        ).apply {
            data = inputMatrixData
        }.applyPadding(2)


        val correctMatrixData = arrayOf(
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.5, 0.3, 0.2, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.6, 0.9, 0.1, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.7, 0.4, 0.5, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        )

        assertArrayEquals(correctMatrixData, matrix.data)

    }
}