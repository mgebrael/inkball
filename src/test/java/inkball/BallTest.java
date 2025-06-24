package inkball;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class BallTest {
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
    public void moveWithinBoard() {
        // Test that the move() method works as intended.
        app.setup();
        level = app.levels.get(0);

        Ball ball = new Ball(0, 0, 0);
        ball.i = 1;
        ball.j = 2.5;

        // move the ball for 40 loop cycles
        for (int n = 0; n < 40; n++)
            ball.move(level);

        assert (ball.getX() == 16 + 40);
        assert (ball.getY() == 16 + App.TOPBAR + 40*2.5);
    }

    @Test
    public void moveOutsideBoard() {
        // Test that the ball cannot move out of bounds.
        app.setup();
        level = app.levels.get(0);

        Ball ball = new Ball(0, 0, 0);
        ball.i = -1;
        ball.j = -2.5;

        // move the ball for 20 loop cycles
        for (int n = 0; n < 20; n++)
            ball.move(level);

        assert (ball.getX() == 28);
        assert (ball.getY() == 120);
    }

}
