package inkball;

import java.util.LinkedList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import processing.core.PApplet;
import processing.event.KeyEvent;

public class LevelTest {
    static App app;
    static Level level;

    @BeforeAll
    public static void setup() {
        app = new App();
        App.random.setSeed(2);
        app.configPath = "src/test/java/inkball/simple.json";
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
    }

    @Test
    public void pauseTest() {
        // Test that pressing space pauses the game.
        app.setup();

        KeyEvent pressSpace = new KeyEvent(null, 0, 0, 0, ' ', 0);
        app.keyPressed(pressSpace);

        app.keyReleased();
    }

    @Test
    public void restartTest() {
        // Test that pressing r restarts the level.
        app.setup();

        KeyEvent pressR = new KeyEvent(null, 0, 0, 0, 'r', 0);
        app.keyPressed(pressR);

        app.keyReleased();
    }

    @Test
    public void endLevel() {
        // Test that a level with no balls in it will end.
        app.setup();
        level = app.levels.get(1);

        level.topbarBalls = new LinkedList<>();
        app.current = 1;

        for (int x = 0; x < 30; x++) {
            app.draw();
        }
    }

    @Test
    public void timeUp() {
        // Tests the level upon time running out.
        app.setup();
        level = app.levels.get(1);
        app.current = 1;

        app.draw();
    }

    @Test
    public void countRemainingTime() {
        // Test the win sequence.
        app.setup();
        level = app.levels.get(0);

        level.topbarBalls = new LinkedList<>();

        for (int x = 0; x < 30; x++) {
            app.draw();
        }
    }
}
