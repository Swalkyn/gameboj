package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * A list element containing the GameBoy screen, acts as a pane
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class GameItem {

    private static final int HEIGHT = 50;
    private static final double PADDING = 10d;
    
    private final String id;
    private final String name;
    private final File rom;
    private final Image image;
    
    private final Pane pane;
    
    /**
     * Creates a new GameItem 
     * @param name : game name
     * @param rom : game rom file
     * @param description : a description of the game (nullable)
     * @param image : an image of the game (nullable)
     * @throws NullPointerException if name or rom are null
     */
    public GameItem(String id, String name, File rom, Image image) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.rom = Objects.requireNonNull(rom);
        this.image = image;
        
        this.pane = buildPane();
    }
    
    /**
     * @return the pane associated with the GameItem
     */
    public Pane asPane() {
        return pane;
    }
    
    public String id() {
        return id;
    }

    /**
     * @return the game rom file
     */
    public File rom() {
        return rom;
    }
    
    private Pane buildPane() {
        ImageView imgView = new ImageView(image);
        Label label = new Label(name);
        HBox top = new HBox(imgView, label);
        
        label.setPadding(new Insets(0, PADDING, 0, PADDING));
        
        imgView.setFitHeight(HEIGHT);
        imgView.setFitWidth(HEIGHT);
        
        HBox.setHgrow(imgView, Priority.NEVER);
        HBox.setHgrow(label, Priority.ALWAYS);
        top.setAlignment(Pos.CENTER);
        
        return new Pane(top);
    }
    
}
