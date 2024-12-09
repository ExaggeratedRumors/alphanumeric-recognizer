import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.Conv
import com.ertools.model.Input
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
        ).toMatrix().matrixFlatten().transpose()

        val filters = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()

        val expectedOutput = arrayOf(
            arrayOf(3.185, 11.995),
            arrayOf(3.27, 12.03)
        )

        val conv = Conv(2, 3, 1, 0)
        conv.bind(Input(4, 3).apply { initialize() })

        conv.loadFilters(filters)
        val outputMatrix = conv.response(inputMatrix)

        outputMatrix.data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(value, expectedOutput[i][j], 0.0001) {
                    "Expected ${expectedOutput[i][j]} but got $value"
                }
            }
        }
    }

    @Test
    fun `convolution error`() {
        val conv = Conv(
            filtersAmount = 2,
            learningRate = 0.01
        )
        conv.bind(Input(4, 3).apply { initialize() })
        val filters = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()
        conv.loadFilters(filters)


        val inputMatrix = arrayOf(
            arrayOf(8.5, 0.65, 1.2),
            arrayOf(9.5, 0.8, 1.3),
            arrayOf(9.9, 0.8, 0.5),
            arrayOf(9.0, 0.9, 1.0)
        ).toMatrix().matrixFlatten().transpose()
        conv.response(inputMatrix)


        val inputError = arrayOf(
            arrayOf(-0.04195, -0.48485),
            arrayOf(-0.3832, 0.8979)
        ).toMatrix()
        val errorResult = conv.error(inputError)

        val errorExpected = arrayOf(
            arrayOf(-3.997, -4.192, -3.864, -0.334, -0.34, -0.378, -0.548, -0.246, -0.404),
            arrayOf(4.409, 4.283, 3.2781, 0.403, 0.33, 0.42, 0.586, -0.181, 0.655)
        )

        errorResult.data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(value, errorExpected[i][j], 0.01) {
                    "Expected ${errorExpected[i][j]} but got $value"
                }
            }
        }
    }

    @Test
    fun `convolution filters update`() {
        /** 1. Load filters **/
        val conv = Conv(
            filtersAmount = 2,
            learningRate = 0.01
        )
        conv.bind(Input(4, 3).apply { initialize() })
        val filters = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()
        conv.loadFilters(filters)

        /** 2. Response **/
        val inputMatrix = arrayOf(
            arrayOf(8.5, 0.65, 1.2),
            arrayOf(9.5, 0.8, 1.3),
            arrayOf(9.9, 0.8, 0.5),
            arrayOf(9.0, 0.9, 1.0)
        ).toMatrix().matrixFlatten().transpose()
        conv.response(inputMatrix)

        /** 3. Error **/
        val inputError = arrayOf(
            arrayOf(-0.04195, -0.48485),
            arrayOf(-0.3832, 0.8979)
        ).toMatrix()
        conv.error(inputError)

        /** 4. Check updated filters **/
        val convUpdatedFiltersExpected = arrayOf(
            arrayOf(0.13997, 0.2419, -0.0614, -0.0967, 0.1034, 0.9038, 0.1055, 0.4025, 0.104),
            arrayOf(0.2559, 1.0571, -0.3328, 0.096, 0.1967, -0.0042, -0.0059, 1.3018, 0.0934)
        )

        val convUpdatedFilters = conv.javaClass.declaredFields
            .first { it.name == "filters" }
            .apply { isAccessible = true }
            .get(conv) as Matrix

        convUpdatedFilters.data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(convUpdatedFiltersExpected[i][j], value, 0.0001)
            }
        }
    }
}