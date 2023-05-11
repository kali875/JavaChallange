package Bot;

import challenge.game.model.Planet;
import org.glassfish.tyrus.core.uri.internal.MultivaluedHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnemyPlanets {
    static MultivaluedHashMap<Integer, Planet> enemyPlanets = new MultivaluedHashMap<>();

    public static void putEnemyPlanet(Planet planet) {
        if (enemyPlanets.containsValue(planet)) {
            int previousKey = 0;
            for (Map.Entry<Integer, List<Planet>> entry : enemyPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    previousKey = entry.getKey();
                    break;
                }
            }
            enemyPlanets.add(previousKey + 1, planet);
        } else {
            enemyPlanets.add(0, planet);
        }
    }

    private static int getTheHighestKey() {
        return Collections.max(enemyPlanets.keySet());
    }

    public static Planet getHighestValuedEnemyPlanet() {
        return enemyPlanets.get(getTheHighestKey()).get(0);
    }

    public static void removePlanet(Planet planet) {
        if (enemyPlanets.containsValue(planet)) {
            for (Map.Entry<Integer, List<Planet>> entry : enemyPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    return;
                }
            }
        }
    }

    public static boolean isEmpty() {
        return enemyPlanets.isEmpty();
    }
}
