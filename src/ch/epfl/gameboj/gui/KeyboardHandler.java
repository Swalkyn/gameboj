package ch.epfl.gameboj.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Joypad.Key;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A class used to link a gameboy joypad to a given javafx node.
 * When a key is pressed in javafx, the KeyboardHandler will translate 
 * it into an in-game keypress 
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public class KeyboardHandler {
    
    private static final Map<String, Key> TEXT_KEY_MAP = buildTextKeyMap();
    private static final Map<KeyCode, Key> CODE_KEY_MAP = buildCodeKeyMap();
    
    private final EventHandler<KeyEvent> pressed; 
    private final EventHandler<KeyEvent> released;
    private final Node node;
    
    /**
     * Creates a new KeyboardHandler, attached to given node and controlling
     * given joypad
     * @param node : the node to which the KH will be attached
     * @param joypad : the joypad to be controlled by the KH
     * @throws NullPointerException if either node or joypad are null
     */
    public KeyboardHandler(Node node, Joypad joypad) {
        Objects.requireNonNull(joypad);
        
        this.node = Objects.requireNonNull(node);
        this.pressed = e -> handleKeyEvent(e, k -> joypad.keyPressed(k));
        this.released = e -> handleKeyEvent(e, k -> joypad.keyReleased(k));
        
        node.setOnKeyPressed(pressed);
        node.setOnKeyReleased(released);
    }
    
    /**
     * Removes the event handlers from the node. Renders the KeyboardHandler useless
     */
    public void detach() {
        node.removeEventHandler(KeyEvent.KEY_PRESSED, pressed);
        node.removeEventHandler(KeyEvent.KEY_RELEASED, released);
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
