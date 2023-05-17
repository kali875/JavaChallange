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
    private static List<Planet> ignored_planets = new ArrayList<>();
    public static int numberOfAllPlanets = 0;
    public static List<Planet> planetsShielded = new ArrayList<>();
    public static Planet basePlanet = null;
    private static int radius = 10;
    public static int colonised_planet_count = 0;

    public static void setPlanets(List<Planet> _planets) {
        planets = _planets;
        numberOfAllPlanets = planets.size();
        Optional<Planet> base_planet = planets.stream()
                .filter(planet -> planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId())
                .findFirst();
        base_planet.ifPresent(value -> {
            planets_owned.add(value);
            basePlanet = value;
        });
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

    public static boolean isPlanetDestroyed(int planet_id) {
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
            colonised_planet_count++;
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
            if (isPlanetShielded(planet)) continue;

            Pair<Double, List<Integer>> minimum = findMinimumDistanceBetweenTargetAndOwnedPlanets(planet, false);
            if(minimum.getSecond() != null && minimum.getSecond().get(0) != -1)
                closestPlanets.put(minimum.getFirst(), new Pair<>(planets_owned.get(minimum.getSecond().get(0)), planet));
        }

        if (closestPlanets.isEmpty()) return null;

        return new Pair<>(closestPlanets.firstKey(), closestPlanets.get(closestPlanets.firstKey()));
    }

    public static Pair<Pair<Double, Pair<Planet, Planet>>, List<Integer>> findClosestPlanetsWH(boolean shallIncludeUnhabitablePlanets) {
        SortedMap<Double, Pair<Planet, Planet>> closestPlanets = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                return Double.compare(d1, d2);
            }
        });

        List<Integer> temp = new ArrayList<>();

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
            if (isPlanetShielded(planet)) continue;

            Pair<Double, List<Integer>> minimum = findMinimumDistanceBetweenTargetAndOwnedPlanets(planet, true);
            if(minimum.getSecond() != null && minimum.getSecond().get(0) != -1)
                closestPlanets.put(minimum.getFirst(), new Pair<>(planets_owned.get(minimum.getSecond().get(0)), planet));
            temp.clear();
            temp.add(minimum.getSecond().get(1));
            temp.add(minimum.getSecond().get(2));
        }

        if (closestPlanets.isEmpty()) return null;

        return new Pair<>(new Pair<>(closestPlanets.firstKey(), closestPlanets.get(closestPlanets.firstKey())), temp);
    }

    public static Pair<Double, Pair<Planet, Planet>> findClosestUnhabitablePlanet() {
        if (unhabitable_planets.isEmpty()) return null;

        SortedMap<Double, Pair<Planet, Planet>> closestPlanets = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                return Double.compare(d1, d2);
            }
        });

        Iterator<Planet> iterator = unhabitable_planets.iterator();
        while (iterator.hasNext()) {
            Planet planet = iterator.next();
            if (OnGoingSpaceMissions.isOngoingSpaceMissionToTarget(planet)) continue;
            if (isPlanetShielded(planet)) continue;
            if (isPlanetIgnored(planet)) continue;

            Pair<Double, List<Integer>> minimum = findMinimumDistanceBetweenTargetAndOwnedPlanets(planet, false);
            if(minimum.getSecond() != null && minimum.getSecond().get(0) != -1)
                closestPlanets.put(minimum.getFirst(), new Pair<>(planets_owned.get(minimum.getSecond().get(0)), planet));
            if (isPlanetDestroyed(planet.getId())) {
                iterator.remove(); // Safely remove the element
                OnGoingMBHShots.onPlanetExploded(planet.getId());
            }
        }

        if (closestPlanets.isEmpty()) return null;
        return new Pair<>(closestPlanets.firstKey(), closestPlanets.get(closestPlanets.firstKey()));
    }

    public static Planet findClosestOwnedPlanetToTarget(Planet target) {
        Pair<Double, List<Integer>> minimum = findMinimumDistanceBetweenTargetAndOwnedPlanets(target, false);
        if(minimum.getSecond() != null && minimum.getSecond().get(0) != -1)
            return planets_owned.get(minimum.getSecond().get(0));
        return null;
    }

    private static Pair<Double, List<Integer>> findMinimumDistanceBetweenTargetAndOwnedPlanets(Planet target, boolean doWH) {
        double minimum_distance = Double.MAX_VALUE;
        int minimum_distance_id = -1;
        int wormHoleId = -1;
        int wormHoleSide = -1;
        for (int i = 0; i < planets_owned.size(); i++) {
            if (isPlanetShielded(planets_owned.get(i))) continue;
            if(doWH){
                for (int j = 0; j < Controll.wormHoles.size(); j++) {
                    if(Controll.wormHoles.get(j).getId() != -1){
                        Planet whA = new Planet();
                        Planet whB = new Planet();
                        whA.setX(Controll.wormHoles.get(j).getX());
                        whA.setY(Controll.wormHoles.get(j).getY());
                        whB.setX(Controll.wormHoles.get(j).getXb());
                        whB.setY(Controll.wormHoles.get(j).getYb());
                        double distance = planets_owned.get(i).distance(whA);
                        distance += whB.distance(target);
                        if (distance < minimum_distance) {
                            minimum_distance = distance;
                            minimum_distance_id = i;
                            wormHoleId = Controll.wormHoles.get(j).getId();
                            wormHoleSide = 0;
                        }
                        distance = planets_owned.get(i).distance(whB);
                        distance += whA.distance(target);
                        if (distance < minimum_distance) {
                            minimum_distance = distance;
                            minimum_distance_id = i;
                            wormHoleId = Controll.wormHoles.get(j).getId();
                            wormHoleSide = 1;
                        }
                    }
                }
            }
            double distance = planets_owned.get(i).distance(target);
            if (distance < minimum_distance) {
                minimum_distance = distance;
                minimum_distance_id = i;
            }
        }
        List<Integer> temp = new ArrayList<>();
        temp.add(minimum_distance_id);
        temp.add(wormHoleId);
        temp.add(wormHoleSide);
        return new Pair<>(minimum_distance, temp);
    }

    private static void findPointsInCircle() {
        // Pont távolsága a középponttól
        for (Planet point : planets)
            if (basePlanet.distance(point) <= radius)
                ignored_planets.add(point);
    }

    private static boolean isPlanetIgnored(Planet planet) {
        for (Planet ignored : ignored_planets)
            if (ignored.getId() == planet.getId()) return true;
        return false;
    }

    public static boolean isPlanetShielded(Planet planet) {
        for (Planet ignored : planetsShielded)
            if (ignored.getId() == planet.getId()) return true;
        return false;
    }
}
