package ch.epfl.gameboj.gui;

import java.util.List;

import ch.epfl.gameboj.Games;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.Pane;
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
    private final VBox pane;
    
    private ReadOnlyObjectWrapper<GameItem> selectedGame = new ReadOnlyObjectWrapper<>();
    
    /**
     * Creates a new GameList
     */
    public GameList() {
        pane = new VBox();
        
        for (int i = 0; i < GAMES.size(); i++) {
            GameItem game = GAMES.get(i);
            game.asPane().setOnMouseClicked(e -> selectedGame.set(game));
            pane.getChildren().add(game.asPane());
        }
        
        selectedGame.set(DEFAULT_GAME);
    }
   
    
    /**
     * @return the pane associated with the GameList
     */
    public Pane asPane() {
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
