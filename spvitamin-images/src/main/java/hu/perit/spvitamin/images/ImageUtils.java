package hu.perit.spvitamin.images;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

@UtilityClass
public class ImageUtils
{
    // --- Konfiguráció (kalibrálandó) ---
    private static final int TILE_SIZE = 64;
    private static final double SHARPNESS_PERCENTILE = 0.90;

    private static final double MM_TO_INCH = 25.4;


    public PaperBounds detectPaper(GrayscaleImage img)
    {
        int[] gray = img.getGray();
        int w = img.getWidth();
        int h = img.getHeight();

        long sum = 0;
        for (int g : gray)
        {
            sum += g;
        }
        int mean = (int) (sum / gray.length);
        // Sötét háttérnél ~140, teljesen fehér szkennelésnél max 200 (különben semmi sem lenne "világos")
        int threshold = Math.clamp(mean + 20L, 140, 200);

        int[] rowBright = new int[h];
        int[] colBright = new int[w];
        for (int y = 0; y < h; y++)
        {
            int base = y * w;
            for (int x = 0; x < w; x++)
            {
                if (gray[base + x] > threshold)
                {
                    rowBright[y]++;
                    colBright[x]++;
                }
            }
        }

        double ratio = 0.4;
        int top = 0;
        int bottom = h - 1;
        int left = 0;
        int right = w - 1;
        while (top < h - 1 && rowBright[top] < w * ratio)
        {
            top++;
        }
        while (bottom > top && rowBright[bottom] < w * ratio)
        {
            bottom--;
        }
        while (left < w - 1 && colBright[left] < h * ratio)
        {
            left++;
        }
        while (right > left && colBright[right] < h * ratio)
        {
            right--;
        }

        return new PaperBounds(left, top, right, bottom);
    }


    public int effectiveDpi(PaperBounds b)
    {
        PaperFormat format = PaperFormat.detect(b);
        int longSide = Math.max(b.width(), b.height());
        int shortSide = Math.min(b.width(), b.height());
        double shortDpi = shortSide / (format.shortMm / MM_TO_INCH);
        if (format.longMm == null)
        {
            // Változó hossz (pl. nyugta): csak a rövid oldal alapján számolunk
            return (int) shortDpi;
        }
        double longDpi = longSide / (format.longMm / MM_TO_INCH);
        return (int) Math.min(longDpi, shortDpi);
    }


    double laplacianVariance(int[] gray, int stride, int x0, int y0, int rw, int rh)
    {
        double sum = 0;
        double sumSq = 0;
        int n = 0;
        for (int y = y0 + 1; y < y0 + rh - 1; y++)
        {
            for (int x = x0 + 1; x < x0 + rw - 1; x++)
            {
                int i = y * stride + x;
                int lap = gray[i - stride] + gray[i + stride] + gray[i - 1] + gray[i + 1] - 4 * gray[i];
                sum += lap;
                sumSq += (double) lap * lap;
                n++;
            }
        }
        if (n == 0)
        {
            return 0;
        }
        double mean = sum / n;
        return sumSq / n - mean * mean;
    }


    public int tiledSharpness(GrayscaleImage img)
    {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] gray = img.getGray();

        int tilesX = w / TILE_SIZE;
        int tilesY = h / TILE_SIZE;
        if (tilesX == 0 || tilesY == 0)
        {
            return (int) laplacianVariance(gray, w, 0, 0, w, h);
        }

        double[] values = new double[tilesX * tilesY];
        int k = 0;
        for (int ty = 0; ty < tilesY; ty++)
        {
            for (int tx = 0; tx < tilesX; tx++)
            {
                values[k++] = laplacianVariance(gray, w, tx * TILE_SIZE, ty * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        Arrays.sort(values);
        return (int) values[(int) Math.min(values.length - 1, Math.floor(values.length * SHARPNESS_PERCENTILE))];
    }


    public BufferedImage downscale(BufferedImage src, int targetLongSide)
    {
        int w = src.getWidth();
        int h = src.getHeight();
        int longSide = Math.max(w, h);
        if (longSide <= targetLongSide)
        {
            return src;   // soha nem nagyítunk
        }

        double scale = (double) targetLongSide / longSide;
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));

        // Area averaging: lekicsinyítésnél jó minőségű, átlagolja a JPEG-zajt is
        Image scaled = src.getScaledInstance(nw, nh, Image.SCALE_AREA_AVERAGING);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        out.getGraphics().drawImage(scaled, 0, 0, null);
        out.getGraphics().dispose();
        return out;
    }


    public BufferedImage resize(BufferedImage src, int targetLongSide)
    {
        int w = src.getWidth();
        int h = src.getHeight();
        int longSide = Math.max(w, h);
        if (longSide == targetLongSide)
        {
            return src;
        }

        double scale = (double) targetLongSide / longSide;
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));

        // Kicsinyítésnél area averaging (JPEG-zajt is simítja), nagyításnál smooth
        int hint = longSide > targetLongSide ? Image.SCALE_AREA_AVERAGING : Image.SCALE_SMOOTH;
        Image scaled = src.getScaledInstance(nw, nh, hint);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics g = out.getGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return out;
    }
}
