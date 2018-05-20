package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {
        
    private GameBoy gb;
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage){
        verifyArguments();
        createGameboy();
        
        GBScreen screen = new GBScreen(gb);
        Scene scene = new Scene(screen.asPane());
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gameboj");
        primaryStage.sizeToScene();
        primaryStage.show();
        
        screen.requestFocus();
        screen.startTimer();
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
}
