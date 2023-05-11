package GameData;

import challenge.game.model.Planet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Planets {

    private static List<Planet> planets = new ArrayList<>();
    private static List<Planet> planets_owned = new ArrayList<>();

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
        if (planet_captured != null) planets_owned.add(planet_captured);
    }

    public static List<Planet> getPlanets_owned() {
        return planets_owned;
    }

    public static List<Planet> getPlanets() {
        return planets;
    }
}
