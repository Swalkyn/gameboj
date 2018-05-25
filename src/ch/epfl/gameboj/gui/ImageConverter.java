package ch.epfl.gameboj.gui;

import java.util.Objects;

import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {

    private static final int[] RGB_VALUES = { 0xFFFFFFFF, 0xFFD3D3D3, 0xFFA9A9A9, 0xFF000000 };
//  private static final int[] RGB_VALUES = { 0xFF9BBC0F, 0xFF8BAC0F, 0xFF306230, 0xFF0F380F };
    
    /**
     * Converts an LcdImage to a writable javafx Image
     * @param image : an lcd image from the gameboy
     * @return the converted image
     * @throws NullPointerException if the image is null
     */
    public static Image convert(LcdImage image) {
        Objects.requireNonNull(image);
        WritableImage wi = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        PixelWriter pwriter = wi.getPixelWriter();

        for (int y = 0; y < LcdController.LCD_HEIGHT; y++) {
            for (int x = 0; x < LcdController.LCD_WIDTH; x++) {
                int rgb = RGB_VALUES[image.get(x, y)];
                pwriter.setArgb(x, y, rgb);
            }
        }

        return wi;
    }
}
