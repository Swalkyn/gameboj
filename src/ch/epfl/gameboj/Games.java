package ch.epfl.gameboj;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.gameboj.gui.GameItem;
import javafx.scene.image.Image;

/**
 * A utility class that generates a list of games
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Games {
    
    private static final String ROM_PATH = "res/rom/";
    private static final String IMG_PATH = "res/img/";
    private static final Image PLACEHOLDER = new Image(new File(IMG_PATH + "placeholder.png")
            .toURI().toString());
    
    private static final Map<String, String> NAMES = buildNames();
    private static final Map<String, File> ROMS = buildRoms();
    private static final Map<String, Image> IMGS = buildImages();
    
    private static final Map<String, GameItem> GAMES = buildGames();
    
    /**
     * @return a list of GameItems containing all games to be shown in list
     */
    public static List<GameItem> asList() {
        List<GameItem> gamesList = new ArrayList<>();
        GAMES.entrySet().forEach(e -> gamesList.add(e.getValue()));
        
        return gamesList;
    }
    
    /**
     * Returns the game item corresponding to the id
     * @param id : game id
     * @throws NullPointerException if game not found
     * @return the GameItem
     */
    public static GameItem game(String id) {
        return GAMES.get(id);
    }
    
    private static GameItem buildGame(String id) {
        return new GameItem(id, NAMES.get(id), ROMS.get(id), IMGS.get(id));
    }
    
    private static Map<String, GameItem> buildGames() {
        Map<String, GameItem> games = new HashMap<>();
        games.put("flappy", buildGame("flappy"));
        games.put("zelda", buildGame("zelda"));
        games.put("mario", buildGame("mario"));
        games.put("mario2", buildGame("mario2"));
        
        return games;
    }
    
    private static Map<String, String> buildNames() {
        Map<String, String> names = new HashMap<>();
        names.put("zelda", "The Legend Of Zelda");
        names.put("flappy", "Flappy Boy");
        names.put("mario", "Super Mario Land");
        names.put("mario2", "Super Mario Land 2");
        
        return names;
    }
    
    private static Map<String, File> buildRoms() {
        Map<String, File> roms = new HashMap<>();
        roms.put("zelda", new File(ROM_PATH + "zelda"));
        roms.put("flappy", new File(ROM_PATH + "flappy"));
        roms.put("mario", new File(ROM_PATH + "mario"));
        roms.put("mario2", new File(ROM_PATH + "mario2"));
        
        return roms;
    }
    
    private static Map<String, Image> buildImages() {
        Map<String, Image> imgs = new HashMap<>();
        imgs.put("zelda", readImage("zelda"));
        imgs.put("flappy", readImage("flappy"));
        imgs.put("mario", readImage("mario"));
        imgs.put("mario2", readImage("mario2"));
        
        return imgs;
    }
    
    private static Image readImage(String id) {
        File file = new File(IMG_PATH + id);
        return file.exists() ? new Image(file.toURI().toString()) : PLACEHOLDER;
    }
}
