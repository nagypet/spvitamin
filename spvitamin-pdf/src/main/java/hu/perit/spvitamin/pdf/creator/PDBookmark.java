package hu.perit.spvitamin.pdf.creator;

import lombok.Data;

@Data
public class PDBookmark
{
    private float currentPosition;

    public PDBookmark(float currentPosition)
    {
        this.currentPosition = currentPosition;
    }

    public void increase(float amount)
    {
        currentPosition -= amount;
    }

    public void newLine(PDDrawer.Font font)
    {
        currentPosition -= font.getLineHeight();
    }

    public void newParagraph(PDDrawer.Font font)
    {
        currentPosition -= font.getFontSize();
    }
}
