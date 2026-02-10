package hu.perit.spvitamin.pdf.creator;

import hu.perit.spvitamin.core.exception.CheckedExceptionConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontLoader
{
    public static final String LIBERATION_SANS_REGULAR = "LiberationSans-Regular";
    public static final String LIBERATION_SANS_BOLD = "LiberationSans-Bold";

    private static final Map<String, String> FONT_FILES = new HashMap<>();
    private static final Map<String, PDType0Font> FONTS = new HashMap<>();

    static
    {
        FONT_FILES.put(LIBERATION_SANS_REGULAR, "LiberationSans-Regular.ttf");
        FONT_FILES.put(LIBERATION_SANS_BOLD, "LiberationSans-Bold.ttf");
    }


    public static void embedFont(PDDocument document, String fontName)
    {
        CheckedExceptionConverter.invokeVoid(() -> {
                    try (InputStream inputStream = FontLoader.class.getClassLoader().getResourceAsStream(FONT_FILES.get(fontName)))
                    {
                        PDType0Font font = PDType0Font.load(document, inputStream);
                        FONTS.put(fontName, font);
                    }
                }
        );
    }


    public static PDType0Font getFont(String fontName)
    {
        if (FONTS.containsKey(fontName))
        {
            return FONTS.get(fontName);
        }

        throw new RuntimeException("Font not found: " + fontName);
    }
}
