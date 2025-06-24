package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.io.*;

/**
 * Base class for the Inkball game.
 */
public class App extends PApplet {
    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = (HEIGHT-TOPBAR)/CELLSIZE;

    public static final int FPS = 30;

    public String configPath;

    public static Random random = new Random();

    public static final int[] winTile = {
        0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 480, 512, 544, 
        544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544, 544,
        512, 480, 448, 416, 384, 352, 320, 288, 256, 224, 192, 160, 128, 96, 64, 32, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    // Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    private int score;
    private int winState;

    private boolean resetFlag;
    private boolean pauseFlag;
    private boolean gameEnd;
    
    private HashMap<String, PImage> sprites = new HashMap<>();

    public static Level currentLevel;
    public ArrayList<Level> levels;
    public int current;

    public Line currentLine;

    /**
     * Method for loading images.
     */
    public PImage getSprite(String s) {
        PImage result = sprites.get(s);
        if (result == null) {
            if (s.startsWith("brick")) {
                result = loadImage(this.getClass().getResource("inkball_spritesheet.png").getPath().replace("%20", " "));
                result = result.get(99, 166 + 33*(s.charAt(5)-'0'), CELLSIZE, CELLSIZE);
            }
            else
                result = loadImage(this.getClass().getResource(s+".png").getPath().replace("%20", " "));
            sprites.put(s, result);
        }
        return result;
    }

    /**
     * Returns cumulative game score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        strokeWeight(10);
        textSize(20);

		// JSON configuration:
		JSONObject config = loadJSONObject(configPath);
        
		// the image is loaded from relative path: "src/main/resources/inkball/..."
		/*try {
            result = loadImage(URLDecoder.decode(this.getClass().getResource(filename+".png").getPath(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }*/
        getSprite("tile");
        getSprite("entrypoint");
        for (int i = 0; i < 5; i++) {
            getSprite("wall"+String.valueOf(i));
            getSprite("ball"+String.valueOf(i));
            getSprite("hole"+String.valueOf(i));
            getSprite("brick"+String.valueOf(i));
        }
        
        current = 0;
        levels = new ArrayList<>();
        JSONArray configLevels = config.getJSONArray("levels");
        for (int i = 0; i < configLevels.size(); i++) {
            Level level = new Level(this, config, configLevels.getJSONObject(i));
            levels.add(level);
        }
        currentLevel = levels.get(current);
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == 'r') {
            resetFlag = true;
        }
        else if (event.getKey() == ' ') {
            pauseFlag = true;
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased() {
        if (resetFlag && gameEnd) {
            resetFlag = false;
            gameEnd = false;
            score = 0;
            setup();
        }
        if (resetFlag) {
            JSONObject config = loadJSONObject(configPath);
            JSONArray configLevels = config.getJSONArray("levels");
            currentLevel = new Level(this, config, configLevels.getJSONObject(current));
            levels.set(current, currentLevel);
            resetFlag = false;
        }
        if (pauseFlag) {
            if (!currentLevel.isLost() && !gameEnd)
                currentLevel.pause();
            pauseFlag = false;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (currentLevel.isLost()) return;

        if (e.getButton() == RIGHT || e.isControlDown()) {
            deleteLine(e);
        }
        else if (e.getButton() == LEFT) { // create a new player-drawn line object
            currentLevel.drawnLines.add(new Line(e.getX(), e.getY()));
            currentLine = currentLevel.drawnLines.getLast();
        }
    }
	
	@Override
    public void mouseDragged(MouseEvent e) {
        if (currentLevel.isLost()) return;

        // add line segments to player-drawn line object if left mouse button is held
        if (e.getButton() == LEFT && !e.isControlDown()) {
            currentLine.addSegment(e.getX(), e.getY());
        }
		
		// remove player-drawn line object if right mouse button is held 
		// and mouse position collides with the line
        if (e.getButton() == RIGHT || e.isControlDown()) {
            deleteLine(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    /**
     * Method for deleting the line that overlaps with the mouse cursor.
     */
    public void deleteLine(MouseEvent e) {
        Line collidedLine = null;
        for (Line line : currentLevel.drawnLines) {
            if (line.collidesWith(e.getX(), e.getY(), 5) != null) {
                collidedLine = line;
            }
        }
        currentLevel.drawnLines.remove(collidedLine);
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {

        //----------------------------------
        //display Board for current level:
        //----------------------------------
        currentLevel = levels.get(current);

        for (int i = currentLevel.grid.length - 1; i >= 0; i--) {
            for (int j = currentLevel.grid[i].length - 1; j >= 0; j--) {
                currentLevel.grid[i][j].draw(this);
            }
        }

        for (Ball ball : currentLevel.balls) {
            ball.draw(this);
            if (currentLevel.isPaused() == false)
                ball.move(currentLevel);
        }

        LinkedList<Line> removedLines = new LinkedList<>();
        Segment collidedSegment;

        for (Line drawnLine : currentLevel.drawnLines) {
            drawnLine.draw(this);

            for (Ball ball : currentLevel.balls) {
                collidedSegment = drawnLine.collidesWith(ball.getX(), ball.getY(), ball.getRadius());

                if (collidedSegment != null) {
                    ball.reflectOff(collidedSegment);
                    removedLines.add(drawnLine);
                }
            }
        }

        currentLevel.drawnLines.removeAll(removedLines);

        LinkedList<Ball> removedBalls = new LinkedList<>();

        for (Hole hole : currentLevel.holes) {
            for (Ball ball : currentLevel.balls) {
                hole.attract(ball);

                if (hole.captures(ball)) {
                    removedBalls.add(ball);
                    currentLevel.updateScore(ball, hole);
                }
            }
        }

        currentLevel.balls.removeAll(removedBalls);

        // refresh topbar
        fill(200);
        strokeWeight(0);
        rect(0, 0, WIDTH, TOPBAR);
        strokeWeight(10);
        fill(0);

        if (currentLevel.isLost()) {
            textSize(16);
            text("=== TIME'S UP ===", 240, App.TOPBAR-22);
            textSize(20);
        }
        else if (currentLevel.isPaused())
            text("*** PAUSED ***", 240, App.TOPBAR-22);

        currentLevel.displayTimer(this);

        //----------------------------------
        //display score
        //----------------------------------
        currentLevel.displayScore(this);

        fill(0);
        rect(16, 16, 160, 32);

        LinkedList<Ball> topbarBalls = currentLevel.topbarBalls;

        clip(16, 16, 160, 32); // do not draw outside of the black box
        for (Ball queuedBall : topbarBalls) {
            queuedBall.draw(this);
            if (currentLevel.topbarBallsShifting() && currentLevel.isPaused() == false)
                queuedBall.shift();
        }
        noClip();

        if (!topbarBalls.isEmpty()) {
            if (topbarBalls.getFirst().getX() == App.CELLSIZE)
                currentLevel.stopShifting();
            currentLevel.displaySpawnTimer(this);
        } else currentLevel.stopShifting();
        
		//----------------------------------
		//display game end message
        //----------------------------------
        if (currentLevel.hasEnded()) {
            score = currentLevel.getScore();
            winState = 0;

            if (current == levels.size() - 1) {
                text("=== ENDED ===", 240, App.TOPBAR-22);
                gameEnd = true;
            }
            else {
                current++;
                levels.get(current).setScore(score);
            }
        }

        else if (currentLevel.remainingBalls() == 0) {
            if (frameCount % 2 == 0)
                currentLevel.countRemainingTime(this);
            image(getSprite("wall4"), winTile[winState % 68], winTile[(winState+51) % 68]+App.TOPBAR);
            image(getSprite("wall4"), winTile[(winState+34) % 68], winTile[(winState+17) % 68]+App.TOPBAR);
            winState += (frameCount % 2);
        }

    }

    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
