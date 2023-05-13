package Bot;

import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;
import org.glassfish.tyrus.core.uri.internal.MultivaluedHashMap;

import java.util.*;

public class EnemyPlanets {
    static HashMap<Integer, List<Planet>> enemyPlanets = new HashMap<>();

    public static void putEnemyPlanet(Planet planet) {
        if (containsValue(planet)) {
            int previousKey = 0;
            for (Map.Entry<Integer, List<Planet>> entry : enemyPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    previousKey = entry.getKey();
                    break;
                }
            }
            if (enemyPlanets.containsKey(previousKey + 1)) {
                enemyPlanets.get(previousKey + 1).add(planet);
            } else {
                List<Planet> temp_list = new ArrayList<>();
                temp_list.add(planet);
                enemyPlanets.put(previousKey + 1, temp_list);
            }
        } else {
            if (enemyPlanets.containsKey(0)) {
                enemyPlanets.get(0).add(planet);
            } else {
                List<Planet> temp_list = new ArrayList<>();
                temp_list.add(planet);
                enemyPlanets.put(0, temp_list);
            }
        }
    }

    private static boolean containsValue(Planet planet) {
        for (List<Planet> planets : enemyPlanets.values())
            for (Planet p : planets)
                if (p.getId() == planet.getId()) return true;
        return false;
    }

    public static int getTheHighestKey() {
        return Collections.max(enemyPlanets.keySet());
    }

    public static Planet getHighestValuedEnemyPlanet() {
        for (Planet p : enemyPlanets.get(getTheHighestKey())) {
            return p;
        }
        return null;
    }

    private static void syncMap() {
        enemyPlanets.values().removeIf(List::isEmpty);
    }

    public static void removePlanet(Planet planet) {
        if (containsValue(planet)) {
            for (Map.Entry<Integer, List<Planet>> entry : enemyPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    syncMap();
                    return;
                }
            }
        }
    }

    public static boolean isEmpty() {
        return enemyPlanets.isEmpty();
    }

    public static void logContainer() {
        for(Map.Entry<Integer, List<Planet>> entry : enemyPlanets.entrySet()) {
            StringBuilder text = new StringBuilder(String.valueOf(entry.getKey()) + ": ");
            for (Planet planet : entry.getValue()) {
                text.append(" ").append(planet.getId());
            }
            System.out.println(text);
        }
    }
}
