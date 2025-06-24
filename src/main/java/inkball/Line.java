package inkball;

import java.util.*;

/**
 * Lines drawn by the player which can reflect balls.
 */
public class Line implements Entity {
    public LinkedList<Segment> segments = new LinkedList<>();

    public Line(int startX, int startY) {
        segments.add(new Segment(startX, startY, startX, startY));
    }

    public void draw(App app) {
        for (Segment s : segments) {
            app.line(s.getX1(), s.getY1(), s.getX2(), s.getY2());
        }
    }

    /**
     * Append a new segment to the end of the line.
     */
    public void addSegment(int x, int y) {
        int lastX = segments.getLast().getX2();
        int lastY = segments.getLast().getY2();
        segments.add(new Segment(lastX, lastY, x, y));
    }

    /**
     * Returns the segment that the point/ball collides with on this line, or null if there is no collision.
     */
    public Segment collidesWith(int x, int y, int radius) {
        for (Segment segment : segments) {
            if (segment.collidesWith(x, y, radius))
                return segment;
        }
        return null;
    }
}
