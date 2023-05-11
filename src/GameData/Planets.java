package GameData;

import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.*;

public class Planets {

    private static List<Planet> planets = new ArrayList<>();
    private static List<Planet> planets_owned = new ArrayList<>();
    public static List<Integer> unhabitable_planets = new ArrayList<>();

    public static void setPlanets(List<Planet> _planets) {
        planets = _planets;
        Optional<Planet> base_planet = planets.stream()
                .filter(planet -> planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId())
                .findFirst();
        base_planet.ifPresent(value -> planets_owned.add(value));
    }
    public static Planet getPlanetByID(int planet_id) {
        Optional<Planet> result = planets.stream()
                .filter(planet -> planet.getId() == planet_id)
                .findFirst();
        return result.orElse(null);
    }

    public static void onPlanetDestroyed(int planet_id) {
        Planet planet_destroyed = getPlanetByID(planet_id);
        if (planet_destroyed != null) planet_destroyed.setDestroyed(true);
    }

    public static void onPlanetCaptured(int planet_id) {
        Planet planet_captured = getPlanetByID(planet_id);
        if (planet_captured != null) {
            planet_captured.setPlayer(JavalessWonders.getCurrentPlayer().getId());
            planets_owned.add(planet_captured);
        }
    }

    public static void planetIsUnhabitable(int planetID) {
        unhabitable_planets.add(planetID);
    }

    public static List<Planet> getPlanets_owned() {
        return planets_owned;
    }

    public static List<Planet> getPlanets() {
        return planets;
    }

    public static Pair<Double, Pair<Planet, Planet>> findClosestPlanets() {
        SortedMap<Double, Pair<Planet, Planet>> closestPlanets = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                return Double.compare(d1, d2);
            }
        });

        for (Planet planet : planets) {
            if (planet.isDestroyed()) continue;
            if (planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId()) continue;
            if (unhabitable_planets.contains(planet)) continue;

            double minimum_distance = Double.MAX_VALUE;
            int minimum_distance_id = 0;
            for (int i = 0; i < planets_owned.size(); i++) {
                double distance = planets_owned.get(i).distance(planet);
                if (distance < minimum_distance) {
                    minimum_distance = distance;
                    minimum_distance_id = i;
                }
            }
            closestPlanets.put(minimum_distance, new Pair<>(planets_owned.get(minimum_distance_id), planet));
        }

        return new Pair<>(closestPlanets.firstKey(), closestPlanets.get(closestPlanets.firstKey()));
    }
}
