package hu.perit.spvitamin.pdf.creator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Data
@Slf4j
public class PDDrawer implements AutoCloseable
{
    private final float pageWidth;
    private final float pageHeight;
    private final PDDocument document;
    private final Supplier<PDPage> pageSupplier;
    private PDPageContentStream contentStream;

    public PDDrawer(PDDocument document, PDPage page, PDPageContentStream.AppendMode appendMode) throws IOException
    {
        this.pageWidth = page.getMediaBox().getWidth();
        this.pageHeight = page.getMediaBox().getHeight();
        this.document = document;
        this.pageSupplier = null;
        this.contentStream = new PDPageContentStream(document, page, appendMode, true);
    }


    public PDDrawer(PDDocument document, PDPage page, PDPageContentStream.AppendMode appendMode, Supplier<PDPage> pageSupplier) throws IOException
    {
        this.pageWidth = page.getMediaBox().getWidth();
        this.pageHeight = page.getMediaBox().getHeight();
        this.document = document;
        this.pageSupplier = pageSupplier;
        this.contentStream = new PDPageContentStream(document, page, appendMode, true);
    }


    public void drawText(Font font, Margin margin, PDBookmark bookmark, String text) throws IOException
    {
        drawText(font, margin, bookmark, text, false, Alignment.LEFT);
    }


    public void drawText(Font font, Margin margin, PDBookmark bookmark, String text, boolean wordWrap) throws IOException
    {
        drawText(font, margin, bookmark, text, wordWrap, Alignment.LEFT);
    }


    public void drawText(Font font, Margin margin, PDBookmark bookmark, String text, boolean wordWrap, Alignment alignment) throws IOException
    {
        if (this.pageSupplier != null && bookmark.getCurrentPosition() <= margin.marginBottom)
        {
            // This text has not enough place => create new page
            this.contentStream.close();
            PDPage page = this.pageSupplier.get();
            this.document.addPage(page);
            bookmark.setCurrentPosition(this.pageHeight - margin.marginTop);
            this.contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        }

        if (wordWrap)
        {
            List<String> lines = wrapText(text, font, margin);
            for (String line : lines)
            {
                drawSingleLineText(font, margin, bookmark, line, alignment);
            }
        }
        else
        {
            drawSingleLineText(font, margin, bookmark, text, alignment);
        }
    }


    private void drawSingleLineText(Font font, Margin margin, PDBookmark bookmark, String text, Alignment alignment) throws IOException
    {
        float xPos = margin.marginLeft;
        if (alignment == Alignment.RIGHT)
        {
            float textSize = getTextSize(font, text);
            xPos = this.pageWidth - margin.getMarginRight() - textSize;
        }

        //log.debug("Drawing {} in position {}, {}", StringUtils.abbreviate(text, 20), xPos, bookmark.getCurrentPosition());

        contentStream.beginText();
        contentStream.setFont(FontLoader.getFont(font.fontName), font.fontSize);
        contentStream.newLineAtOffset(xPos, bookmark.getCurrentPosition());
        contentStream.showText(text);
        contentStream.endText();
        bookmark.newLine(font);
    }


    private float getTextSize(Font font, String text) throws IOException
    {
        PDType0Font pdType0Font = FontLoader.getFont(font.fontName);
        return pdType0Font.getStringWidth(text) / 1000 * font.fontSize;
    }


    private List<String> wrapText(String text, Font font, Margin margin) throws IOException
    {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words)
        {
            String testLine = currentLine + (!currentLine.isEmpty() ? " " : "") + word;
            float textWidth = getTextSize(font, testLine);

            if (textWidth > (this.pageWidth - margin.marginLeft - margin.marginRight))
            {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
            else
            {
                currentLine.append((!currentLine.isEmpty() ? " " : "")).append(word);
            }
        }

        if (!currentLine.isEmpty())
        {
            lines.add(currentLine.toString());
        }

        return lines;
    }


    public void drawLine(Margin margin, PDBookmark bookmark, Color color) throws IOException
    {
        contentStream.setStrokingColor(color);
        contentStream.moveTo(margin.marginLeft, bookmark.getCurrentPosition());
        contentStream.lineTo(this.pageWidth - margin.marginRight, bookmark.getCurrentPosition());
        contentStream.stroke();
    }


    @Override
    public void close() throws Exception
    {
        if (contentStream != null)
        {
            contentStream.close();
        }
    }


    @Data
    public static class Font
    {
        private final String fontName;
        private final int fontSize;
        private final int gap;

        public float getLineHeight()
        {
            return (float) this.fontSize + this.gap;
        }
    }

    @Data
    public static class Margin
    {
        private final float marginTop;
        private final float marginBottom;
        private final float marginLeft;
        private final float marginRight;

        public float getTotalHorizontalMargin()
        {
            return this.marginLeft + this.marginRight;
        }
    }

    public enum Alignment
    {
        LEFT,
        RIGHT
    }
}
