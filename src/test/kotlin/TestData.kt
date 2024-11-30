import com.ertools.io.DataLoader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit


object TestData{

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    fun `loading train labels test`() {
        val balancedTrainLabelsPath = "data/emnist_source_files/emnist-balanced-train-labels-idx1-ubyte"
        val balancedLabelsAmount = 47
        val data = DataLoader.loadLabelData(balancedTrainLabelsPath)
        data[0].forEach { print("$it ") }
        println()
        assert(data[0].size == balancedLabelsAmount)
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    fun `loading train images test`() {
        val balancedTrainImagesPath = "data/emnist_source_files/emnist-balanced-train-images-idx3-ubyte"
        val balancedImagesSize = 784
        val metadata = DataLoader.loadImageData(balancedTrainImagesPath)
        assert(metadata.data[0].size == balancedImagesSize)
    }

}
