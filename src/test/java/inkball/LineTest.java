package inkball;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class LineTest {
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
    public void pressMouse() {
        // Test that lines can be created and deleted with left and right mouse clicks.
        app.setup();

        MouseEvent mouse1 = new MouseEvent(null, 0, 0, 0, 0, 0, App.LEFT, 0);
        app.mousePressed(mouse1);

        MouseEvent mouse2 = new MouseEvent(null, 0, 0, 0, 100, 100, App.LEFT, 0);
        app.mouseDragged(mouse2);

        // app.deleteLine(mouse2);
        MouseEvent mouse3 = new MouseEvent(null, 0, 0, 0, 0, 0, App.RIGHT, 0);
        app.mousePressed(mouse3);

        app.mousePressed(mouse1);
        app.mouseDragged(mouse2);

        app.draw();
        app.mouseDragged(mouse3);
    }
}
