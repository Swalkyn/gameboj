package ch.epfl.gameboj.gui;

import java.util.List;

import ch.epfl.gameboj.Games;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.VBox;

/**
 * A pane containing the list of games
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class GameList {
    private static final List<GameItem> GAMES = Games.asList();
    private final VBox pane;

    private ReadOnlyObjectWrapper<GameItem> selectedGame = new ReadOnlyObjectWrapper<>();

    /**
     * Creates a new GameList
     */
    public GameList() {
        VBox vbox = new VBox();
        vbox.setFillWidth(true);

        for (int i = 0; i < GAMES.size(); i++) {
            GameItem game = GAMES.get(i);
            game.asPane().setOnMouseClicked(e -> {
                if (selectedGame.get() != null) {
                    selectedGame.get().asPane().getStyleClass().remove("selected-game");
                }
                
                selectedGame.set(game);
                game.asPane().getStyleClass().add("selected-game");
            });
            game.asPane().getStyleClass().add(i % 2 == 0 ? "game-item-even" : "game-item-odd");
            vbox.getChildren().add(game.asPane());
        }

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        
       
        pane = new VBox(scroll/*, buttonContainer*/);
        pane.setMaxHeight(GBScreen.HEIGHT);
    }

    /**
     * @return the pane associated with the GameList
     */
    public VBox asPane() {
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
