package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

/**
 * A list element containing the GameBoy screen, acts as a pane
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class GameItem {

    public static final double MIN_WIDTH = 225;

    private static final double THUMB_WIDTH = 50d;
    private static final double IMG_SCALE = 0.85d;

    private static final double CIRC_POS = IMG_SCALE*THUMB_WIDTH/2;
    private static final double CLIP_SCALE = 0.8d;
    private static final double PADDING = 10d;

    
    private final String id;
    private final String name;
    private final File rom;
    private final File save;
    private final Image image;

    private final Pane pane;

    /**
     * Creates a new GameItem
     * 
     * @param name : game name
     * @param rom : game rom file
     * @param description : a description of the game (nullable)
     * @param image : an image of the game (nullable)
     * @throws NullPointerException if name or rom are null
     */
    public GameItem(String id, String name, File rom, File save, Image image) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.rom = Objects.requireNonNull(rom);
        this.save = save;
        this.image = image;

        this.pane = buildPane();
        
    }

    /**
     * @return the pane associated with the GameItem
     */
    public Pane asPane() {
        return pane;
    }

    /**
     * @return the id associated with the game item
     */
    public String id() {
        return id;
    }

    /**
     * @return the game rom file
     */
    public File rom() {
        return rom;
    }
    
    public File save() {
        return save;
    }

    private Pane buildPane() {
        ImageView imgView = new ImageView(image);
        StackPane thumbnail = new StackPane(imgView);
        
        Label label = new Label(name);
        HBox thumbContainer = new HBox(thumbnail);
        HBox top = new HBox(thumbContainer, label);

        label.setPadding(new Insets(0, PADDING, 0, PADDING));
        label.getStyleClass().add("game-label");

        Circle imgClip = new Circle(CIRC_POS, CIRC_POS, CLIP_SCALE*THUMB_WIDTH/ 2);
        imgView.setFitHeight(IMG_SCALE * THUMB_WIDTH);
        imgView.setFitWidth(IMG_SCALE * THUMB_WIDTH);
        imgView.setClip(imgClip);
        
        thumbnail.getStyleClass().add("game-thumbnail");
        thumbnail.setMaxHeight(IMG_SCALE *THUMB_WIDTH);
        thumbnail.setMaxWidth(IMG_SCALE *THUMB_WIDTH);
        
        thumbContainer.setMinHeight(THUMB_WIDTH);
        thumbContainer.setMinWidth(THUMB_WIDTH);
        thumbContainer.setAlignment(Pos.CENTER);
        

        HBox.setHgrow(thumbContainer, Priority.NEVER);
        HBox.setHgrow(label, Priority.ALWAYS);
        top.setPadding(new Insets(0, PADDING, 0, 0));
        top.setAlignment(Pos.CENTER);
        top.getStyleClass().add("game-item");

        return new Pane(top);
    }

}
