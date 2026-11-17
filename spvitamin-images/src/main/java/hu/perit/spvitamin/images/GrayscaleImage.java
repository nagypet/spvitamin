package hu.perit.spvitamin.images;

import lombok.Getter;
import lombok.experimental.Delegate;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.stream.IntStream;

@Getter
public class GrayscaleImage
{
    private static final int PARALLEL_THRESHOLD = 1_000_000; // 1 MP felett parallel

    @Delegate
    private final BufferedImage img;
    private final int[] gray;


    public static GrayscaleImage from(BufferedImage img)
    {
        return new GrayscaleImage(img);
    }


    private GrayscaleImage(BufferedImage img)
    {
        this.img = img;
        this.gray = toGray(img);
    }


    static int[] toGray(BufferedImage img)
    {
        int w = img.getWidth();
        int h = img.getHeight();
        int n = w * h;
        int[] gray = new int[n];

        int type = img.getType();
        switch (type)
        {
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB ->
            {
                int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                toGrayFromInt(pixels, gray, n);
            }
            case BufferedImage.TYPE_3BYTE_BGR ->
            {
                // JPEG alapértelmezett típusa: közvetlen byte-hozzáférés, nincs getRGB() másolás
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                toGrayFromBgr(pixels, gray, n, false);
            }
            case BufferedImage.TYPE_4BYTE_ABGR ->
            {
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                toGrayFromBgr(pixels, gray, n, true);
            }
            default ->
            {
                // Fallback: getRGB() kezeli a color model konverziót
                int[] rgb = img.getRGB(0, 0, w, h, null, 0, w);
                toGrayFromInt(rgb, gray, n);
            }
        }
        return gray;
    }


    static void toGrayFromInt(int[] pixels, int[] gray, int n)
    {
        IntStream range = IntStream.range(0, n);
        (n >= PARALLEL_THRESHOLD ? range.parallel() : range).forEach(i ->
        {
            int p = pixels[i];
            gray[i] = (77 * ((p >> 16) & 0xFF) + 150 * ((p >> 8) & 0xFF) + 29 * (p & 0xFF)) >> 8;
        });
    }


    static void toGrayFromBgr(byte[] pixels, int[] gray, int n, boolean hasAlpha)
    {
        int stride = hasAlpha ? 4 : 3;
        // ABGR: [A, B, G, R], BGR: [B, G, R]
        int rOff = hasAlpha ? 3 : 2;
        int gOff = 1;
        int bOff = 0;
        IntStream range = IntStream.range(0, n);
        (n >= PARALLEL_THRESHOLD ? range.parallel() : range).forEach(i ->
        {
            int base = i * stride;
            int r = pixels[base + rOff] & 0xFF;
            int g = pixels[base + gOff] & 0xFF;
            int b = pixels[base + bOff] & 0xFF;
            gray[i] = (77 * r + 150 * g + 29 * b) >> 8;
        });
    }
}
