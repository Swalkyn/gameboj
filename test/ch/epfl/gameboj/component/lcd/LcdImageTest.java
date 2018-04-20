package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class LcdImageTest {
    
    @Test
    void getWorksForKnownValues() throws IOException {
        LcdImage img = DebugDrawImage.smlImage();
        
        assertEquals(0b11, img.get(2, 2));
        assertEquals(0b01, img.get(61, 26));
        assertEquals(0b10, img.get(192, 139));
        assertEquals(0b00, img.get(22, 124));
    }
    
    @Test
    void getFailsForInvalidIndex() throws IOException {
        LcdImage img = DebugDrawImage.smlImage();
        
        assertThrows(IndexOutOfBoundsException.class, () -> img.get(312, 14));
        assertThrows(IndexOutOfBoundsException.class, () -> img.get(-1, 14));
        assertThrows(IndexOutOfBoundsException.class, () -> img.get(14, 312));
        assertThrows(IndexOutOfBoundsException.class, () -> img.get(14, -1));
    }
    
    @Test
    void equalsWorksForKnownValues() throws IOException {
        LcdImage img1 = DebugDrawImage.smlImage();
        LcdImage img2 = DebugDrawImage.smlImage();
        LcdImage img3 = DebugDrawImage.sml2Image();
        
        assertTrue(img1.equals(img2));
        assertFalse(img1.equals(img3));
        assertFalse(img1.equals(null));
    }
    
    @Test
    void hashCodeIsCompatibleWithEquals() throws IOException {
        LcdImage img1 = DebugDrawImage.smlImage();
        LcdImage img2 = DebugDrawImage.smlImage();
        
        assertTrue(img1.hashCode() == img2.hashCode());
    }
}
