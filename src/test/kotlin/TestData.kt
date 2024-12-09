import com.ertools.common.Error.dmse
import com.ertools.io.DataLoader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class TestData {
    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    fun `loading train labels test`() {
        val balancedTrainLabelsPath = "data/emnist_source_files/emnist-balanced-train-labels-idx1-ubyte"
        val balancedLabelsAmount = 47
        val data = DataLoader.loadLabelData(balancedTrainLabelsPath, 1000)
        assert(data.labelsAmount == balancedLabelsAmount)
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    fun `loading train images test`() {
        val balancedTrainImagesPath = "data/emnist_source_files/emnist-balanced-train-images-idx3-ubyte"
        val balancedImagesSize = 784
        val data = DataLoader.loadImageData(balancedTrainImagesPath, 1000)
        assert(data.columns * data.rows == balancedImagesSize)
    }

    @Test
    fun `dmse error`() {
        val actualOutput = arrayOf(1.8555, -0.1375)
        val expectedOutput = arrayOf(0.0, 1.0)
        val error = dmse(actualOutput, expectedOutput)
        val expectedError = arrayOf(1.8555, -1.1375)
        error.forEachIndexed { i, _ ->
            assertEquals(error[i], expectedError[i], 0.001)
        }
    }
}
