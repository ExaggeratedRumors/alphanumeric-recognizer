import com.ertools.common.Matrix
import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.network.Conv
import com.ertools.network.Input
import com.ertools.operations.ActivationFunction
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

        conv.loadFilters(arrayOf(filters))
        val outputMatrix = conv.response(inputMatrix)

        outputMatrix.data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(expectedOutput[i][j], value, 0.0001) {
                    "Expected ${expectedOutput[i][j]} but got $value"
                }
            }
        }
    }

    @Test
    fun `convolution filters update`() {
        /** 1. Load filters **/
        val conv = Conv(
            filtersAmount = 2,
            learningRate = 0.01,
            activationFunction = ActivationFunction.Relu
        )
        conv.bind(Input(4, 3).apply { initialize() })
        val filters = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()
        conv.loadFilters(arrayOf(filters))

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
            .get(conv) as Array<Matrix>

        convUpdatedFilters[0].data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(convUpdatedFiltersExpected[i][j], value, 0.0001)
            }
        }
    }

    @Test
    fun `convolution backpropagation error`() {
        /** 1. Load filters **/
        val conv = Conv(
            filtersAmount = 2,
            learningRate = 0.01,
            activationFunction = ActivationFunction.Relu
        )
        conv.bind(Input(4, 3).apply { initialize() })
        val filters = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()
        conv.loadFilters(arrayOf(filters))

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
        val result = conv.error(inputError).reconstructMatrix(4)

        assertEquals(4, result.rows)
        assertEquals(3, result.columns)
    }
}