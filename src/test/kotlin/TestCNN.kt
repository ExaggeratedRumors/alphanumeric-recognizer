import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestCNN {

    @Test
    fun `build CNN`() {
        val layers = listOf(
            Input(16, 16),
            Conv(2, 3),
            Flatten(),
            Dense(4),
        )

        val cnn = CNN(
            layers = layers
        )
        cnn.build()
    }

    @Test
    fun `CNN response`() {
        val conv = Conv(filtersAmount = 2, kernel = 3, learningRate = 0.01)
        val dense = Dense(neurons  = 2, learningRate = 0.01)
        val layers = listOf(
            Input(4, 3),
            conv,
            Flatten(),
            dense,
        )

        val cnn = CNN(
            layers = layers
        )
        cnn.build()

        val filters = arrayOf(
            arrayOf(0.1, 0.2, -0.1, -0.1, 0.1, 0.9, 0.1, 0.4, 0.1),
            arrayOf(0.3, 1.1, -0.3, 0.1, 0.2, 0.0, 0.0, 1.3, 0.1)
        ).toMatrix()
        conv.loadFilters(filters)
        val weights = arrayOf(
            arrayOf(0.1, -0.2, 0.1, 0.3),
            arrayOf(0.2, 0.1, 0.5, -0.3)
        ).toMatrix()
        dense.loadWeights(weights)

        val trainX = arrayOf(
            arrayOf(8.5, 0.65, 1.2),
            arrayOf(9.5, 0.8, 1.3),
            arrayOf(9.9, 0.8, 0.5),
            arrayOf(9.0, 0.9, 1.0)
        ).toMatrix()
        val trainY = arrayOf(0.0, 1.0)
        val expectedY = arrayOf(1.8555, -0.1375)
        val response = cnn.fit(listOf(trainX), listOf(trainY))[0]

        response.forEachIndexed { i, v ->
            assertEquals(expectedY[i], v, 0.0001)
        }
    }
}