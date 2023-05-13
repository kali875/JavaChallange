package Bot;

import challenge.game.model.Planet;

import java.util.*;

public class DefensePlanets
{
    static HashMap<Integer, List<Planet>> defPlanets = new HashMap<>();

    public static void putEndangeredPlanet(Planet planet) {
        if (containsValue(planet.getId())) {
            int previousKey = 0;
            for (Map.Entry<Integer, List<Planet>> entry : defPlanets.entrySet()) {
                if (entry.getValue().contains(planet)) {
                    entry.getValue().remove(planet);
                    previousKey = entry.getKey();
                    break;
                }
            }
            if (defPlanets.containsKey(previousKey + 1)) {
                defPlanets.get(previousKey + 1).add(planet);
            } else {
                List<Planet> temp_list = new ArrayList<>();
                temp_list.add(planet);
                defPlanets.put(previousKey + 1, temp_list);
            }
        } else {
            if (defPlanets.containsKey(0)) {
                defPlanets.get(0).add(planet);
            } else {
                List<Planet> temp_list = new ArrayList<>();
                temp_list.add(planet);
                defPlanets.put(0, temp_list);
            }
        }
    }

    private static boolean containsValue(int planet_id) {
        for (List<Planet> planets : defPlanets.values())
            for (Planet p : planets)
                if (p.getId() == planet_id) return true;
        return false;
    }

    public static int getTheHighestKey() {
        return Collections.max(defPlanets.keySet());
    }

    public static Planet getHighestValuedEndangeredPlanet() {
        for (Planet p : defPlanets.get(getTheHighestKey())) {
            return p;
        }
        return null;
    }

    private static void syncMap() {
        defPlanets.values().removeIf(List::isEmpty);
    }

    public static void removePlanet(int planet_id) {
        for (Map.Entry<Integer, List<Planet>> entry : defPlanets.entrySet()) {
            if (entry.getValue().removeIf(p -> p.getId() == planet_id)) {
                syncMap();
                return;
            }
        }
    }

    public static boolean isEmpty() {
        return defPlanets.isEmpty();
    }
}
