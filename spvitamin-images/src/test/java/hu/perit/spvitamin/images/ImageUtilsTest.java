package hu.perit.spvitamin.images;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ImageUtilsTest
{
    private static final double MIN_PAPER_AREA_RATIO = 0.25;


    @ParameterizedTest
    @CsvSource({
            "poor1.jpeg, 67, 67, 590, 439, 44, 748",
            "12MP.jpeg, 108, 265, 3738, 2857, 310, 15205",
            "genertel_1.jpg, 0, 0, 988, 988, 84, 13344",
            "genertel_1_small.jpg, 0, 0, 790, 790, 67, 6363",
            "genertel_1_small_blur1.jpg, 0, 0, 790, 790, 67, 3093",
            "genertel_1_small_blur3.jpg, 0, 0, 790, 790, 67, 683",
            "genertel_1_small_blur5.jpg, 0, 0, 790, 790, 67, 258",
            "genertel_1_small_blur8.jpg, 0, 0, 790, 790, 67, 96",
            "genertel_1_small2_blur8.jpg, 0, 0, 632, 632, 54, 114",
    })
    void testImageQuality(String input, int left, int top, int right, int bottom, int expectedDpi, int expectedSharpness) throws IOException
    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (var is = classloader.getResourceAsStream(input))
        {
            GrayscaleImage img = GrayscaleImage.from(ImageIO.read(is));
            int w = img.getWidth();
            int h = img.getHeight();

            // 1. Lap detektálása (fallback: teljes kép)
            PaperBounds bounds = ImageUtils.detectPaper(img);
            boolean paperDetected = (double) bounds.width() * bounds.height() / ((double) w * h) >= MIN_PAPER_AREA_RATIO;
            if (!paperDetected)
            {
                bounds = new PaperBounds(0, 0, w - 1, h - 1);
            }
            log.info("Paper detected: {}, bounds: {}", paperDetected, bounds);
            assertThat(paperDetected).isTrue();
            assertThat(bounds).isEqualTo(new PaperBounds(left, top, right, bottom));

            // 2. Effektív DPI
            int dpi = ImageUtils.effectiveDpi(bounds);
            log.info("DPI: {}", dpi);
            assertThat(dpi).isEqualTo(expectedDpi);

            // 3. Élesség a kivágott, normalizált méretű lapon
            BufferedImage paper = img.getSubimage(bounds.getLeft(), bounds.getTop(), bounds.width(), bounds.height());
            BufferedImage normalizedImage = ImageUtils.resize(paper, 900);
            int sharpness = ImageUtils.tiledSharpness(GrayscaleImage.from(normalizedImage));
            //ImageIO.write(normalizedImage, "png", new File(String.format("resized_%s.png", input)));
            log.info("Sharpness: {}", sharpness);
            assertThat(sharpness).isEqualTo(expectedSharpness);
        }
    }
}
