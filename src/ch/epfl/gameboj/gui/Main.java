package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class Main extends Application {
        
    private GameBoy gb;
    
    private static final double TIME_TO_CYCLES = Math.pow(2, 20) / Math.pow(10, 9);
    private long previousTime;

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
        
        imgView.setFitWidth(2 * LcdController.LCD_WIDTH);
        imgView.setFitHeight(2 * LcdController.LCD_HEIGHT);
        
        imgView.setOnKeyPressed(e -> handleKeyEvent(e, k -> gb.joypad().keyPressed(k)));
        imgView.setOnKeyReleased(e -> handleKeyEvent(e, k -> gb.joypad().keyReleased(k)));
        
        AnimationTimer timer = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                
                long cycles = (long) ((currentNanoTime - previousTime) * TIME_TO_CYCLES);
                gb.runUntil(gb.cycles() + cycles);
                previousTime = currentNanoTime;
                imgView.setImage(getImage(gb));
            }
        };
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gameboy Emulator");
        primaryStage.sizeToScene();
        primaryStage.show();
        imgView.requestFocus();
        
        previousTime = System.nanoTime();
        timer.start();
        
    }
    
    private void verifyArguments() {
        if (getParameters().getRaw().size() != 1) {
            System.exit(1);
        }
    }
    
    private void createGameboy() {
        try {
            File rom = new File(getParameters().getRaw().get(0));
            Cartridge cartridge = Cartridge.ofFile(rom);
            gb = new GameBoy(cartridge);            
        } catch (IOException e) {
            System.out.println("File not found");
            System.exit(1);
        }
    }
    
    private Image getImage(GameBoy gb) {
        LcdImage currentImage = gb.lcdController().currentImage();
        return ImageConverter.convert(currentImage);
    }
    
    private void handleKeyEvent(KeyEvent e, Consumer<Joypad.Key> c) {
        KeyCode keyCode = e.getCode();
        String keyString = e.getText();
        
        Key k;
        
        switch (keyString) {
        case "a":
            k = Key.A;
            break;
        case "b":
            k = Key.B;
            break;
        case "s":
            k = Key.START;
            break;
        default:
            switch (keyCode) {
            case SPACE:
                k = Key.SELECT;
                break;
            case LEFT:
                k = Key.LEFT;
                break;
            case RIGHT:
                k = Key.RIGHT;
                break;
            case UP:
                k = Key.UP;
                break;
            case DOWN:
                k = Key.DOWN;
                break;
            
            default:
                return;
            }
        }
                
        c.accept(k);
    }
}
