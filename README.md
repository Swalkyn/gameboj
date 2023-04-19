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
- [Maven 3.8+](https://maven.apache.org/download.cgi)

## Launch

```
mvn javafx:run
```

