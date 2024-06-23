package hu.perit.spvitamin.core.filename;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileNameUtils
{
    public static String sanitizeFileName(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }

        return fileName.replaceAll("[\\\\/:*?\"<>|\r\n]", "");
    }


    public static String getFileExtension(String fileName)
    {
        return StringUtils.toRootLowerCase(FilenameUtils.getExtension(sanitizeFileName(fileName)));
    }
}
