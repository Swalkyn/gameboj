package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Represents an image which can be displayed by the gameboy
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class LcdImage {
    
    private final int width;
    private final int height;
    private final List<LcdImageLine> lines;
    
    public static final class Builder {
        
        private final int width;
        private final int height;
        private List<LcdImageLine> lines;
        
        private boolean enabled = true;
        
        /**
         * Creates a new builder
         * @param width : width of the image to build
         * @param height : height of the image to build
         * @throws IllegalArgumentException if width or height are negative
         */
        public Builder(int width, int height) {
            Preconditions.checkArgument(width > 0 && height > 0);
            
            this.width = width;
            this.height = height;
            this.lines = new ArrayList<>();
            
            for (int i = 0; i < height; i++) {
                lines.add(new LcdImageLine.Builder(width).build());
            }
        }
        
        /**
         * Sets a given line of pixels at specified index
         * @param index : index
         * @param line : line to be set
         * @throws IllegalArgumentException if line has an incorrect size
         * @throws IndexOutOfBoundsException if index incorrect
         * @return this
         */
        public Builder setLine(int index, LcdImageLine line) {
            Preconditions.checkArgument(line.size() == width);
            Objects.checkIndex(index, height);
            
            if (!enabled) {
                throw new IllegalStateException();
            }
            
            lines.set(index, line);
            
            return this;
        }
        
        /**
         * Builds the image that has been constructed so far
         * @return the new image
         */
        public LcdImage build() {
            enabled = false;
            return new LcdImage(width, height, lines);
        }
    }
    
    /**
     * Creates a new image with given parameters
     * @param width : width of the new image
     * @param height : height of the new image
     * @param lines : the lines that form the image
     * @throws IllegalArgumentException if number of lines is not the same as the height or if height or width are invalid
     */
    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        Preconditions.checkArgument(lines.size() == height && lines.get(0).size() == width && width > 0 && height > 0);
        
        this.width = width;
        this.height = height;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }
    
    /**
     * @return the width of the image
     */
    public int width() {
        return width;
    }
    
    /**
     * @return the height of the image
     */
    public int height() {
        return height;
    }
    
    /**
     * Returns the value of the pixel at specified coordinates
     * @param x : horizontal position of the pixel
     * @param y : vertical position of the pixel
     * @return the color value (0-3) of the pixel 
     */
    public int get(int x, int y) {
        if (x >= width || y >= height) {
            throw new IndexOutOfBoundsException();
        }
        
        int msb = lines.get(y).msb().testBit(x) ? 0b10 : 0;
        int lsb = lines.get(y).lsb().testBit(x) ? 0b01 : 0;
        return msb | lsb;
    }
    
    @Override
    public boolean equals(Object that) {
        if (that instanceof LcdImage && ((LcdImage) that).width == this.width && ((LcdImage) that).height == this.height) {
            return ((LcdImage) that).lines.equals(this.lines);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return lines.hashCode();
    }
}
