package GameData;

import challenge.game.model.Planet;

import java.util.ArrayList;
import java.util.List;

public class OnGoingMBHShots {
    public static List<Planet> shots = new ArrayList<>();

    public static void onShot(Planet planet) {
        shots.add(planet);
    }

    public static void onPlanetExploded(int planet_id) {
        shots.removeIf(planet -> planet.getId() == planet_id);
    }

    public static boolean isOngoingMBHShotToTarget(int planet_id) {
        if (shots.isEmpty()) return false;
        for (Planet planet : shots)
            if (planet.getId() == planet_id) return true;
        return false;
    }
}
