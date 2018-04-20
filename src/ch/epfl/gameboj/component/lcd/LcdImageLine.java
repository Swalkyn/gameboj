package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * Represents a line of pixels part of an image
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class LcdImageLine {
    
    private static final int NOP_PALETTE = 0b11100100; 
    
    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity;
    
    private final int size;
    
    public static final class Builder {
    	
        	private BitVector.Builder msbBuilder;
        	private BitVector.Builder lsbBuilder;
        	
        	private final int size;
        	
        	/**
        	 * Creates a new builder
        	 * @param size : size of the line to be built
        	 */
        	public Builder(int size) {
        		msbBuilder = new BitVector.Builder(size);
        		lsbBuilder = new BitVector.Builder(size);
        		
        		this.size = size;
        	}
        	
        	/**
        	 * Sets a byte at
        	 * @param byteIndex
        	 * @param msb : byte containing most significant bits
        	 * @param lsb : byte containing least significant bits
        	 */
        	public void setBytes(int byteIndex, int msb, int lsb) {
        	    Objects.checkIndex(byteIndex, size);
        	    
        		msbBuilder.setByte(byteIndex, (byte) msb);
        		lsbBuilder.setByte(byteIndex, (byte) lsb);
       	}
        	
        	/**
        	 * Builds the line that has been constructed so far
        	 * @return the new line
        	 */
        	public LcdImageLine build() {
        		BitVector msb = msbBuilder.build();
        		BitVector lsb = lsbBuilder.build();
        		BitVector opacity = msb.not().and(lsb.not());
        		
        		return new LcdImageLine(msb, lsb, opacity);
        	}
    }
    
    /**
     * Creates a new line with given bits
     * @param msb : vector containing the most significant bits of the colors
     * @param lsb : vector containing the least significant bits of the colors
     * @param opacity : vector containing the opacity of each pixel
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());

        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
        
        this.size = msb.size();
    }
    
    /**
     * @return the size of the line
     */
    public int size() {
        return size;
    }
    
    /**
     * @return the vector containing the most significant bits
     */
    public BitVector msb() {
        return msb;
    }
    
    /**
     * @return the vector containing the most significant bits
     */
    public BitVector lsb() {
        return lsb;
    }
    
    /**
     * @return the vector containing the opacity of each pixel
     */
    public BitVector opacity() {
        return opacity;
    }
    
    /**
     * Shifts the pixels of the line by a given distance
     * @param distance : distance of the shift
     * @return a new line with the shifted pixels
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
    }
    
    /**
     * Extracts the line with zero extraction method
     * @param start : start of the extraction
     * @param size : size of the extraction
     * @return a new line with the extracted pixels
     */
    public LcdImageLine extractWrapped(int start, int size) {
        return new LcdImageLine(msb.extractWrapped(start, size), lsb.extractWrapped(start, size), opacity.extractWrapped(start, size));
    }
    
    /**
     * Changes the colors of the line in function of a given palette
     * @param palette : 8-bit integer describing the color map
     * @throws IllegalArgumentException if the palette is not 8 bits
     * @return a new line with the new colors
     */
    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);
        
        if (palette == NOP_PALETTE) {
            return this;
        }
        
        BitVector newMsb = msb;
        BitVector newLsb = lsb;
        
        for (int i = 3; i >= 0; i--) {
            int color = extractColor(palette, i);
            BitVector mask = generateMask(i);
            newMsb = applyColorMap(extractReplacement(color, false), newMsb, mask);
            newLsb = applyColorMap(extractReplacement(color, true), newLsb, mask);
        }
        
        return new LcdImageLine(newMsb, newLsb, opacity);
    }
    
    /**
     * Superimposes this line and another
     * @param above : the other line
     * @return a new line with the pixels of the combined lines
     */
    public LcdImageLine below(LcdImageLine above) {
        return below(above, above.opacity);
    }
    
    /**
     * Superimposes this line and another
     * @param above : the other line
     * @param opacity : the opacity to use
     * @return a new line with the pixels of the combined lines
     */
    public LcdImageLine below(LcdImageLine above, BitVector opacity) {
        Objects.requireNonNull(opacity);
        Preconditions.checkArgument(this.size == above.size);
        
        BitVector newMsb = multiplexer(above.msb, this.msb, opacity);
        BitVector newLsb = multiplexer(above.lsb, this.lsb, opacity);
        
        return new LcdImageLine(newMsb, newLsb, this.opacity.or(opacity));
    }
    
    /**
     * Join this line with another one
     * @param second : the other line
     * @param index : index of the pixel where the lines will be joined
     * @throws IllegalArgumentException if the sizes don't match
     * @throws IllegalArgumentException if index is not 0<=index<=size
     * @return a new line of the joined lines
     */
    public LcdImageLine join(LcdImageLine second, int index) {
        Preconditions.checkArgument(0 <= index && index <= size);
    	    Preconditions.checkArgument(size == second.size());
    	    
    	    BitVector mask = new BitVector(size, true).shift(index).not();
    	
        BitVector newMsb = multiplexer(this.msb, second.msb, mask);
        BitVector newLsb = multiplexer(this.lsb, second.lsb, mask);
        BitVector newOpacity = multiplexer(this.opacity, second.opacity, mask);
                
        return new LcdImageLine(newMsb, newLsb, newOpacity);    
    }
    
    @Override
    public boolean equals(Object that) {
        if (that instanceof LcdImageLine && ((LcdImageLine) that).size == size) {
            LcdImageLine lcdImgLine= (LcdImageLine) that;
            return this.msb.equals(lcdImgLine.msb) && this.lsb.equals(lcdImgLine.lsb) && this.opacity.equals(lcdImgLine.opacity);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }
    
    private BitVector applyColorMap(boolean repl, BitVector bits, BitVector mask) {
        BitVector replacement = new BitVector(size, repl);
        return multiplexer(replacement, bits, mask);
    }
    
    private BitVector generateMask(int color) {
        switch (color) {
            case 0b00:
                return msb.not().and(lsb.not());
            case 0b01:
                return msb.not().and(lsb);
            case 0b10:
                return msb.and(lsb.not());
            case 0b11:
                return msb.and(lsb);
            
             default: throw new IllegalArgumentException("Invalid color code");
        }
    }
    
    private BitVector multiplexer(BitVector high, BitVector low, BitVector toggle) {
        return high.and(toggle).or(low.and(toggle.not()));
    }
    
    private boolean extractReplacement(int color, boolean isLsb) {
        return Bits.test(color, isLsb ? 0 : 1);
    }
    
    private int extractColor(int palette, int index) {
        return (palette >>> index * 2) & 0b11;
    }
    
 }
