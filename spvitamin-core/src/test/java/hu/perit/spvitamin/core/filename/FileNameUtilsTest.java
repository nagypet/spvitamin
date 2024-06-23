package hu.perit.spvitamin.core.filename;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class FileNameUtilsTest
{
    @Test
    void testSanitizeFileName()
    {
        String fixedFileName = FileNameUtils.sanitizeFileName("Mieten MV\r\n\n\n: 213104");
        log.debug(fixedFileName);
        assertThat(fixedFileName).isEqualTo("Mieten MV 213104");
    }


    @Test
    void testGetFileExtension()
    {
        assertThat(FileNameUtils.getFileExtension("Mieten MV: 213104.pdf")).isEqualTo("pdf");
        assertThat(FileNameUtils.getFileExtension("Mieten MV: 213104.PDF")).isEqualTo("pdf");
        assertThat(FileNameUtils.getFileExtension("alma")).isEmpty();
        assertThat(FileNameUtils.getFileExtension("alma.korte.szilva")).isEqualTo("szilva");
    }
}