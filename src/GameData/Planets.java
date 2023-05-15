package GameData;

import Bot.Controll;
import Bot.DefensePlanets;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.*;

public class Planets {

    private static List<Planet> planets = new ArrayList<>();
    private static List<Planet> planets_owned = new ArrayList<>();
    public static List<Planet> unhabitable_planets = new ArrayList<>();
    public static List<Planet> destroyed_planets = new ArrayList<>();
    public static int numberOfAllPlanets = 0;

    public static void setPlanets(List<Planet> _planets) {
        planets = _planets;
        numberOfAllPlanets = planets.size();
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
        if (!isPlanetDestroyed(planet_id)) {
            Planet destroyed_planet = getDestroyedPlanet(planet_id);
            if (destroyed_planet != null) destroyed_planets.add(destroyed_planet);
        }
        planets.removeIf(p -> p.getId() == planet_id);
        planets_owned.removeIf(p -> p.getId() == planet_id);
        unhabitable_planets.removeIf(pID -> pID.getId() == planet_id);
        DefensePlanets.removePlanet(planet_id);
        OnGoingMBHShots.onPlanetExploded(planet_id);
    }

    private static boolean isPlanetDestroyed(int planet_id) {
        return destroyed_planets.stream().anyMatch(planet -> planet.getId() == planet_id);
    }

    private static Planet getDestroyedPlanet(int planet_id) {
        Optional<Planet> result = Controll.game.getWorld().getPlanets().stream()
                .filter(planet -> planet.getId() == planet_id)
                .findFirst();
        return result.orElse(null);
    }

    public static void onPlanetCaptured(int planet_id) {
        // Ha túl gyorsan hajt végre akciókat a Bot, néha véletlen nem lakhatónak jelöl meg egy lakható bolygót
        unhabitable_planets.removeIf(planet -> planet.getId() == planet_id);
        Planet planet_captured = getPlanetByID(planet_id);
        if (planet_captured != null) {
            planet_captured.setPlayer(JavalessWonders.getCurrentPlayer().getId());
            planets_owned.add(planet_captured);
        }
    }

    public static void planetIsUnhabitable(Planet planet) {
        unhabitable_planets.add(planet);
    }

    public static List<Planet> getPlanets_owned() {
        return planets_owned;
    }

    public static boolean ownedPlanetsContains(Planet planet) {
        return planets_owned.stream().anyMatch(p -> p.getId() == planet.getId());
    }

    public static List<Planet> getPlanets() {
        return planets;
    }

    public static Pair<Double, Pair<Planet, Planet>> findClosestPlanets(boolean shallIncludeUnhabitablePlanets) {
        SortedMap<Double, Pair<Planet, Planet>> closestPlanets = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                return Double.compare(d1, d2);
            }
        });

        for (Planet planet : planets) {
            if (isPlanetDestroyed(planet.getId())) {
                planets.remove(planet);
                OnGoingMBHShots.onPlanetExploded(planet.getId());
                continue;
            }
            if (OnGoingMBHShots.isOngoingMBHShotToTarget(planet.getId())) continue;
            if (planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId()) continue;
            if (!shallIncludeUnhabitablePlanets)
                if (unhabitable_planets.stream().anyMatch(p -> p.getId() == planet.getId()))
                    continue;
            if (OnGoingSpaceMissions.isOngoingSpaceMissionToTarget(planet)) continue;

            Pair<Double, Integer> minimum = findMinimumDistanceBetweenTargetAndOwnedPlanets(planet);
            closestPlanets.put(minimum.getFirst(), new Pair<>(planets_owned.get(minimum.getSecond()), planet));
        }

        if (closestPlanets.isEmpty()) return null;
        return new Pair<>(closestPlanets.firstKey(), closestPlanets.get(closestPlanets.firstKey()));
    }

    public static Pair<Double, Pair<Planet, Planet>> findClosestUnhabitablePlanet() {
        if (unhabitable_planets.isEmpty()) return null;

        SortedMap<Double, Pair<Planet, Planet>> closestPlanets = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                return Double.compare(d1, d2);
            }
        });

        for (Planet planet : unhabitable_planets) {
            if (isPlanetDestroyed(planet.getId())) {
                unhabitable_planets.remove(planet);
                OnGoingMBHShots.onPlanetExploded(planet.getId());
                continue;
            }
            if (OnGoingSpaceMissions.isOngoingSpaceMissionToTarget(planet)) continue;

            Pair<Double, Integer> minimum = findMinimumDistanceBetweenTargetAndOwnedPlanets(planet);
            closestPlanets.put(minimum.getFirst(), new Pair<>(planets_owned.get(minimum.getSecond()), planet));
        }

        if (closestPlanets.isEmpty()) return null;
        return new Pair<>(closestPlanets.firstKey(), closestPlanets.get(closestPlanets.firstKey()));
    }

    public static Planet findClosestOwnedPlanetToTarget(Planet target) {
        return planets_owned.get(findMinimumDistanceBetweenTargetAndOwnedPlanets(target).getSecond());
    }

    private static Pair<Double, Integer> findMinimumDistanceBetweenTargetAndOwnedPlanets(Planet target) {
        double minimum_distance = Double.MAX_VALUE;
        int minimum_distance_id = 0;
        for (int i = 0; i < planets_owned.size(); i++) {
            double distance = planets_owned.get(i).distance(target);
            if (distance < minimum_distance) {
                minimum_distance = distance;
                minimum_distance_id = i;
            }
        }
        return new Pair<>(minimum_distance, minimum_distance_id);
    }
}
