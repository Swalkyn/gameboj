package ch.epfl.gameboj.gui;

import java.util.Objects;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * A pane containing the GameBoy screen
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class GBScreen {

    private static final int GUI_SCALE = 2;
    public static final int SIZE = GUI_SCALE * LcdController.LCD_WIDTH;
    
    private final ImageView imgView;
    private final Pane pane;
    private final AnimationTimer timer = createTimer();

    private double previousTime = 0;
    private GameBoy gb;
    private KeyboardHandler kh;

    /**
     * Creates a new GBScreen
     */
    public GBScreen() {
        this.imgView = new ImageView();
        this.pane = new BorderPane(imgView);
        this.kh = new KeyboardHandler(pane);
        
        imgView.setFitWidth(SIZE);
        imgView.setFitHeight(SIZE);
    }
    
    /**
     * Attaches gameboy to screen
     * @param gb
     */
    public void attachGameboy(GameBoy gb) {
        this.gb = Objects.requireNonNull(gb);
        kh.attach(gb.joypad());
        
        previousTime = System.nanoTime();
        imgView.requestFocus();
        timer.start();
    }
    
    /**
     * Detaches the current gameboy and keyboard from the screen
     */
    public void detachGameboy() {
		kh.detach();    		    	
    	timer.stop();
    	gb = null;
    }
    
    /**
     * @return the pane associated with the GBScreen
     */
    public Pane asPane() {
        return pane;
    }
    
    private AnimationTimer createTimer() {
        return new AnimationTimer() {
        	@Override
            public void handle(long currentNanoTime) {
                long cycles = (long) ((currentNanoTime - previousTime) * GameBoy.CYCLES_PER_NANOSECOND);
                gb.runUntil(gb.cycles() + cycles);
                previousTime = currentNanoTime;
                imgView.setImage(getImage(gb));
            }
        };
    }
    
    private Image getImage(GameBoy gb) {
        LcdImage currentImage = gb.lcdController().currentImage();
        return ImageConverter.convert(currentImage);
    }

}
