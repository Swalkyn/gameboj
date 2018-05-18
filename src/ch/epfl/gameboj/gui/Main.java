package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    private long previousTime;
    
    private static final Map<String, Key> TEXT_KEY_MAP = buildTextKeyMap();
    private static final Map<KeyCode, Key> CODE_KEY_MAP = buildCodeKeyMap();
    
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
    
    private void handleKeyEvent(KeyEvent e, Consumer<Joypad.Key> c) {
        Key k = TEXT_KEY_MAP.getOrDefault(e.getText(), CODE_KEY_MAP.getOrDefault(e.getCode(), null));
        if (k != null) {
    		c.accept(k);
        }
    }
    
    private static Map<String, Key> buildTextKeyMap() {
		Map<String, Key> keyMap = new HashMap<>();
		keyMap.put("a", Key.A);
		keyMap.put("b", Key.B);
		keyMap.put("s", Key.START);
		keyMap.put(" ", Key.SELECT);
		return keyMap;
    }
    
    private static Map<KeyCode, Key> buildCodeKeyMap() {
		Map<KeyCode, Key> keyMap = new HashMap<>();
		keyMap.put(KeyCode.LEFT, Key.LEFT);
		keyMap.put(KeyCode.RIGHT, Key.RIGHT);
		keyMap.put(KeyCode.UP, Key.UP);
		keyMap.put(KeyCode.DOWN, Key.DOWN);
		return keyMap;
    }
}
