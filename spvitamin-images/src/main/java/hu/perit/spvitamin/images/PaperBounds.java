package hu.perit.spvitamin.images;

import lombok.Data;

@Data
public final class PaperBounds
{
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;

    public int width()
    {
        return right - left + 1;
    }


    public int height()
    {
        return bottom - top + 1;
    }
}
