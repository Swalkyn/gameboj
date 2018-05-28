package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * GUI Application for gameboy emulator
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Main extends Application {

    GBSaver saver = new GBSaver();

    /**
     * Launch the application with given rom file
     * 
     * @param args : takes one argument, the path to the rom file
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) {
        GBScreen screen = new GBScreen();
        GameList list = new GameList();
        

        list.selectedGameProperty().addListener((o, oV, nV) -> {
            screen.detachGameboy();
            saver.save();
            
            try {
                Cartridge newGame = createCartridge(nV.rom());
                saver.load(newGame, nV.save());
                screen.attachGameboy(createGameboy(newGame));                
            } catch (IOException e) {
                System.err.println("Something went wrong when loading this game :" + e);
            }
        });

        list.asPane().setOnMouseClicked(e -> screen.asPane().requestFocus());

        HBox pane = new HBox(screen.asPane(), list.asPane());
        HBox.setHgrow(list.asPane(), Priority.ALWAYS);
        HBox.setHgrow(screen.asPane(), Priority.NEVER);

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(Main.class.getResource("css/main.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Gameboj");
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        saver.save();  
    }
    
    
    private Cartridge createCartridge(File rom) throws IOException {
        return Cartridge.ofFile(rom);
    }

    
    private GameBoy createGameboy(Cartridge cartridge) {
        return new GameBoy(cartridge);
    }

}
