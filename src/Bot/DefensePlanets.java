package Bot;

import challenge.game.model.Planet;
import org.glassfish.tyrus.core.uri.internal.MultivaluedHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefensePlanets
{
    static MultivaluedHashMap<Integer, Planet> DefPlanets = new MultivaluedHashMap<>();

    public static void putEnemyPlanet(Planet planet) {
        if (DefPlanets.containsValue(planet)) {
            int previousKey = 0;
            for (Map.Entry<Integer, List<Planet>> entry : DefPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    previousKey = entry.getKey();
                    break;
                }
            }
            DefPlanets.add(previousKey + 1, planet);
        } else {
            DefPlanets.add(0, planet);
        }
    }

    public static int getTheHighestKey() {
        return Collections.max(DefPlanets.keySet());
    }

    public static Planet getHighestValuedEnemyPlanet() {
        return DefPlanets.get(getTheHighestKey()).get(0);
    }

    public static void removePlanet(Planet planet) {
        if (DefPlanets.containsValue(planet)) {
            for (Map.Entry<Integer, List<Planet>> entry : DefPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    return;
                }
            }
        }
    }

    public static boolean isEmpty() {
        return DefPlanets.isEmpty();
    }
}
