package GameData;

import challenge.game.model.Planet;

import java.util.*;

public class OnGoingMBHShots {
    public static List<Planet> shots = new ArrayList<>();
    public static List<Planet> maybe_will_shot = new ArrayList<>();

    public static void possibleTarget(Planet planet) {
        maybe_will_shot.add(planet);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                maybe_will_shot.remove(planet);
            }
        }, 1000);
    }

    public static void onShot(Planet planet) {
        shots.add(planet);
    }

    public static void onPlanetExploded(int planet_id) {
        shots.removeIf(planet -> planet.getId() == planet_id);
    }

    public static boolean isOngoingMBHShotToTarget(int planet_id) {
        if (shots.isEmpty() && maybe_will_shot.isEmpty()) return false;
        boolean match_possible_targets = maybe_will_shot.stream().filter(Objects::nonNull).anyMatch(planet -> planet.getId() == planet_id);
        if (match_possible_targets) return true;
        return shots.stream().filter(Objects::nonNull).anyMatch(planet -> planet.getId() == planet_id);
    }
}
