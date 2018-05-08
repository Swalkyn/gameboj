package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    
    private static final int[] rgbMap = {0xFFFFFFFF, 0xFFD3D3D3, 0xFFA9A9A9, 0xFF000000};
    
    public static Image convert(LcdImage image) {
        WritableImage wi = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
        PixelWriter pwriter = wi.getPixelWriter();
        
        for (int y = 0; y < LcdController.LCD_HEIGHT; y++) {
            for (int x = 0; x < LcdController.LCD_WIDTH; x++) {
                int rgb = rgbMap[image.get(x, y)];
                pwriter.setArgb(x, y, rgb);
            }
        }
        
        return wi;       
    }
}
