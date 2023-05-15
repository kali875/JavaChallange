package GameData;

import Bot.Controll;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class OnGoingSpaceMissions {
    private static HashMap<BigDecimal, Pair<Planet, Planet>> missions = new HashMap<>();

    public static void onSpaceMission(Planet origin, Planet target) {
        BigDecimal estimatedTime = BigDecimal.valueOf(Calendar.getInstance().getTimeInMillis()).add(BigDecimal.valueOf(2 * origin.distance(target) * Controll.game.getSettings().getTimeOfOneLightYear()));
        missions.put(estimatedTime, new Pair<>(origin, target));
    }

    public static Planet onActionReplenished(long time) {
        int minimum = Integer.MAX_VALUE;
        BigDecimal key = null;
        for (BigDecimal bigDecimal : missions.keySet()) {
            int timeDif = Integer.parseInt(bigDecimal.toBigInteger().subtract(BigInteger.valueOf(time)).toString());
            if (timeDif < minimum) {
                minimum = timeDif;
                key = bigDecimal;
            }
        }
        // nem space mission volt valszeg
        if (key == null) return null;
        if (minimum > 500) return null;
        Planet target = missions.get(key).getSecond();
        missions.remove(key);
        return target;
    }

    public static boolean isOngoingSpaceMissionToTarget(Planet target) {
        if (missions.isEmpty()) return false;
        for (Pair<Planet, Planet> mission : missions.values())
            if (mission.getSecond().getId() == target.getId())
                return true;
        return false;
    }

    public static List<Planet> getSpaceMissionTargets() {
        List<Planet> targets = new ArrayList<>();
        for (Pair<Planet, Planet> space_missions : missions.values())
            targets.add(space_missions.getSecond());
        return targets;
    }
}
