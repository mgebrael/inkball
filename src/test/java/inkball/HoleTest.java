package inkball;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import processing.core.PApplet;

public class HoleTest {
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
    public void attractBall() {
        // Test the attraction that holes apply to balls.
        app.setup();
        level = app.levels.get(2);
        app.current = 2;

        for (int x = 0; x < 30; x++) {
          app.draw();
        }
    }

    @Test
    public void colourMatch() {
        // Test the colourMatches() method.
        app.setup();

        Ball greyBall = new Ball(0, 0, "grey");
        Ball orangeBall = new Ball(0, 0, "orange");
        Ball blueBall = new Ball(0, 0, "blue");
        Ball greenBall = new Ball(0, 0, "green");
        Ball yellowBall = new Ball(0, 0, "yellow");

        Hole greyHole = new Hole(0, 0, 0);
        Hole orangeHole = new Hole(0, 0, 1);
        Hole blueHole = new Hole(0, 0, 2);
        Hole greenHole = new Hole(0, 0, 3);
        Hole yellowHole = new Hole(0, 0, 4);

        assert (orangeHole.colourMatches(orangeBall));
        assert (blueHole.colourMatches(blueBall));
        assert (greenHole.colourMatches(greenBall));
        assert (yellowHole.colourMatches(yellowBall));

        assert (yellowHole.colourMatches(greyBall));
        assert (greyHole.colourMatches(greyBall));
        assert (greyHole.colourMatches(blueBall));
    }
}