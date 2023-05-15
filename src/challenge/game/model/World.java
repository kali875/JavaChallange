package challenge.game.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Ez tárolja a világot, vagyis azt az univerzumot, amelyben a játék zajlik.
 */
@Data
public class World {
    /**
     * Az univerzum szélessége 2D-ben.
     */
    private long width;
    /**
     * Az univerzum magassága 2D-ben.
     */
    private long height;
    /**
     * Tárolja az univerzumban található bolygókat.
     */
    private List<Planet> planets = new ArrayList<>();
    /**
     * Tárolja az univerumban található féreglyukakat.
     */
    private List<WormHole> wormHoles = new ArrayList<>();
}
