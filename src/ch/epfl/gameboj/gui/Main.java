package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class Main extends Application {
        
    private GameBoy gb;
    private long previousTime;
    
    private static final double TIME_TO_CYCLES = GameBoy.CYCLES_PER_NANOSECOND;
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage){
        verifyArguments();
        createGameboy();
        
        ImageView imgView = new ImageView();
        BorderPane bpane = new BorderPane(imgView);
        Scene scene = new Scene(bpane);
        
        KeyboardHandler kh = new KeyboardHandler(imgView, gb.joypad());
        
        imgView.setFitWidth(2 * LcdController.LCD_WIDTH);
        imgView.setFitHeight(2 * LcdController.LCD_HEIGHT);
        
        AnimationTimer timer = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                long cycles = (long) ((currentNanoTime - previousTime) * TIME_TO_CYCLES);
                gb.runUntil(gb.cycles() + cycles);
                previousTime = currentNanoTime;
                imgView.setImage(getImage(gb));
            }
        };
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gameboj");
        primaryStage.sizeToScene();
        primaryStage.show();
        imgView.requestFocus();
        
        previousTime = System.nanoTime();
        timer.start();
    }
    
    private void verifyArguments() {
        if (getParameters().getRaw().size() != 1) {
            System.err.println("Invalid number of arguments");
            System.exit(1);
        }
    }
    
    private void createGameboy() {
        try {
            File rom = new File(getParameters().getRaw().get(0));
            Cartridge cartridge = Cartridge.ofFile(rom);
            gb = new GameBoy(cartridge);            
        } catch (IOException e) {
            System.err.println("File not found");
            System.exit(1);
        }
    }
    
    private Image getImage(GameBoy gb) {
        LcdImage currentImage = gb.lcdController().currentImage();
        return ImageConverter.convert(currentImage);
    }
}
