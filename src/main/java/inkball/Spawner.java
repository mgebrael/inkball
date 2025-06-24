package inkball;


/**
 * A tile from which balls can be spawned from.
 */
public class Spawner extends Tile {

    public Spawner(int x, int y) {
        super(x, y);
        this.type = "entrypoint";
    }
}
