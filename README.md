# Gameboj

## Description

This is a working Game Boy emulator creating for the CS-108: Practice of Object
Oriented Programming class. ([Course
website](https://cs108.epfl.ch/archive/18/)). The emulator is written in java,
with JavaFX as its GUI library. It includes an easy to use GUI interface to
select games and also supports cartridge saves for games such as Zelda. Sound
support was later added in the context of COM-418: Computers & Musics.

A variety of games comes preloaded with the application, including:
  - Super Mario Land 2
  - Zelda: Link's Awakening
  - Tetris
  - Donkey Kong

![Application GUI Interface](https://github.com/bataillard/Gameboj/blob/master/res/img/Capture.PNG "The emulator interface")

### Controls
| Keyboard         | Gameboy Button   |
| :--------------: | ---------------- |
| *Space*          | Start            |
| *S*              | Select           |
| *A*              | A                |
| *B*              | B 		          |

## Dependencies

Make sure you have the following installed:

- [Java 11](https://jdk.java.net/archive/)
- [OpenJFX 17+](https://openjfx.io/openjfx-docs/#introduction)

## Launch

The easy option is to use IntelliJ, since it should use the configuration
in this directory and provide you with the option to build and run Gameboj.
You might need to edit the VM options and change the module path to your
installation of OpenJFX.

Otherwise, you can run the following commands from the project's root:

```
export $PATH_TO_FX={path to your installation of OpenJFX, for example /usr/lib/jvm/java-11-openjfx/lib}
# Compile
javac --module-path /usr/lib/jvm/java-11-openjfx/lib \
      --add-modules=java.desktop,javafx.fxml,javafx.controls \
      -d mods/gameboj \
      $(find src/ -name "*.java")

# Run
java --module-path /usr/lib/jvm/java-11-openjfx/lib:mods \
     --add-modules=javafx.fxml,javafx.controls \
     -m ch.epfl.gameboj/ch.epfl.gameboj.gui.Main
```

