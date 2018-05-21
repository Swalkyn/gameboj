package ch.epfl.gameboj.gui;

import java.util.List;

import ch.epfl.gameboj.Games;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * A pane containing the list of games
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class GameList {
    private static final List<GameItem> GAMES = Games.asList();
    private static final GameItem DEFAULT_GAME = Games.game("zelda");
    private final ScrollPane pane;
    
    private ReadOnlyObjectWrapper<GameItem> selectedGame = new ReadOnlyObjectWrapper<>();
    
    /**
     * Creates a new GameList
     */
    public GameList() {
        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        
        for (int i = 0; i < GAMES.size(); i++) {
            GameItem game = GAMES.get(i);
            game.asPane().setOnMouseClicked(e -> selectedGame.set(game));
            vbox.getChildren().add(game.asPane());
        }
        
        selectedGame.set(DEFAULT_GAME);
        
        pane = new ScrollPane(vbox);
        pane.setFitToWidth(true);
        pane.setMaxHeight(GBScreen.SIZE);
        
    }
   
    
    /**
     * @return the pane associated with the GameList
     */
    public ScrollPane asPane() {
        return pane;
    }
    
    /**
     * @return the readonly property for the selected game
     */
    public ReadOnlyObjectProperty<GameItem> selectedGameProperty() {
        return selectedGame.getReadOnlyProperty();
    }
    
    /**
     * @return the selected game
     */
    public GameItem getSelectedGame() {
        return selectedGameProperty().get();
    }
}
