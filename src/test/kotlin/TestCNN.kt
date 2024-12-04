import com.ertools.model.*
import org.junit.jupiter.api.Test

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
}