package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gameboj.component.lcd.LcdImageLine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

public final class DebugDrawImage {

    private static final int[] COLOR_MAP = new int[] {
        0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    public static void main(String[] args) throws IOException {
        LcdImage li = sml3Image();
        
        int w = 256, h = 256;
        BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; ++y)
            for (int x = 0; x < w; ++x)
                i.setRGB(x, y, COLOR_MAP[li.get(x, y)]);
        ImageIO.write(i, "png", new File("sml3.png"));
        System.out.println("done");
    }
    
    public static LcdImage smlImage() throws IOException {
        String f = "test/ch/epfl/gameboj/component/lcd/sml.bin.gz";
        int w = 256, h = 256;
        LcdImage.Builder ib = new LcdImage.Builder(w, h);

        try (InputStream s = new GZIPInputStream(new FileInputStream(f))) {
            for (int y = 0; y < h; ++y) {
                LcdImageLine.Builder lb = new LcdImageLine.Builder(w);
                for (int x = 0; x < w / Byte.SIZE; ++x)
                    lb.setBytes(x, s.read(), s.read());
                ib.setLine(y, lb.build());
            }
        }
        return ib.build();
    }
    
    public static LcdImage sml2Image() throws IOException {
        String f = "test/ch/epfl/gameboj/component/lcd/sml.bin.gz";
        int w = 256, h = 256;
        LcdImage.Builder ib = new LcdImage.Builder(w, h);

        try (InputStream s = new GZIPInputStream(new FileInputStream(f))) {
            for (int y = 0; y < h; ++y) {
                LcdImageLine.Builder lb = new LcdImageLine.Builder(w);
                
                for (int x = 0; x < w / Byte.SIZE; ++x) {
                    lb.setBytes(x, s.read(), s.read());
                }
                lb.setBytes(12, y, y);
                ib.setLine(y, lb.build());
            }
        }
        return ib.build();
    }
    
    public static LcdImage sml3Image() throws IOException {
        String f = "test/ch/epfl/gameboj/component/lcd/sml.bin.gz";
        int w = 256, h = 256;
        LcdImage.Builder ib = new LcdImage.Builder(w, h);

        try (InputStream s = new GZIPInputStream(new FileInputStream(f))) {
            for (int y = 0; y < h; ++y) {
                LcdImageLine.Builder lb = new LcdImageLine.Builder(w);
                
                for (int x = 0; x < w / Byte.SIZE; ++x) {
                    lb.setBytes(x, s.read(), s.read());
                }
                ib.setLine(y, lb.build().mapColors(0b00011011));
            }
        }
        return ib.build();
    }
}
