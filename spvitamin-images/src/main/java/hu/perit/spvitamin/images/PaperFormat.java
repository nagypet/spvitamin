package hu.perit.spvitamin.images;

/**
 * Ismert papírformátumok fizikai méretekkel (mm).
 * longMm == null azt jelenti, hogy a hossz változó (pl. hőpapíros nyugta).
 */
public enum PaperFormat
{
    A4(297.0, 210.0),
    RECEIPT_80MM(null, 80.0);

    // A hosszabb oldal mm-ben, null ha változó (pl. nyugta)
    final Double longMm;
    // A rövidebb oldal mm-ben
    final double shortMm;

    PaperFormat(Double longMm, double shortMm)
    {
        this.longMm = longMm;
        this.shortMm = shortMm;
    }

    /**
     * A detektált PaperBounds aspect ratio-ja alapján meghatározza a legvalószínűbb formátumot.
     * Ha a hossz/szélesség arány > 2.0, hőpapíros nyugtának tekintjük.
     */
    public static PaperFormat detect(PaperBounds b)
    {
        int longSide = Math.max(b.width(), b.height());
        int shortSide = Math.min(b.width(), b.height());
        double ratio = (double) longSide / shortSide;
        return ratio > 2.0 ? RECEIPT_80MM : A4;
    }
}
