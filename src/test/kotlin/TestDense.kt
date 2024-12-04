import com.ertools.common.Matrix
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
        val input = arrayOf(1.8555, -1.1375)
        val denseWeights = arrayOf(
            arrayOf(0.1, -0.2, 0.1, 0.3),
            arrayOf(0.2, 0.1, 0.5, -0.3)
        ).toMatrix()
        val layerErrorExpected = arrayOf(-0.04195, -0.48485, -0.3832, 0.8979)
        val dense = Dense(2)
        dense.loadWeights(denseWeights)
        val error = dense.error(input)

        error.forEachIndexed { i, _ ->
            assertEquals(layerErrorExpected[i], error[i], 0.0001)
        }
    }

    @Test
    fun `dense weights update`() {
        /** 1. Load weights **/
        val dense = Dense(
            neurons = 2,
            learningRate = 0.01
        )

        val denseWeights = arrayOf(
            arrayOf(0.1, -0.2, 0.1, 0.3),
            arrayOf(0.2, 0.1, 0.5, -0.3)
        ).toMatrix()
        dense.loadWeights(denseWeights)

        /** 2. Response **/
        val flatLayerOutput = arrayOf(3.185, 11.995, 3.27, 12.03)
        dense.response(flatLayerOutput)

        /** 3. Error **/
        val input = arrayOf(1.8555, -1.1375)
        dense.error(input)

        /** 4. Check updated weights **/
        val denseUpdatedWeightsExpected = arrayOf(
            arrayOf(0.0409023, -0.422567, 0.0393252, 0.0767834),
            arrayOf(0.236229, 0.236443, 0.537196, -0.163159)
        )

        dense.javaClass.getDeclaredField("weights").isAccessible = true
        val denseUpdatedWeights = dense.javaClass.declaredFields
            .first { it.name == "weights" }
            .apply { isAccessible = true }
            .get(dense) as Matrix

        denseUpdatedWeights.data.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                assertEquals(denseUpdatedWeightsExpected[i][j], value, 0.0001)
            }
        }
    }
}