import com.ertools.common.Matrix.Companion.toMatrix
import com.ertools.model.ActivationFunction.linear
import com.ertools.model.Dense
import org.junit.jupiter.api.Test

class TestDense {



    @Test
    fun `dense response`() {
        val flatLayerOutput = arrayOf(3.185, 11.995, 3.27, 12.03)
        val denseWeights = arrayOf(
            arrayOf(0.1, -0.2, 0.1, 0.3),
            arrayOf(0.2, 0.1, 0.5, -0.3)
        ).toMatrix()
        val dense = Dense(2, { it -> linear(it) }, )
    }
}