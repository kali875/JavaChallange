package Bot;

import GameData.Planets;
import Utils.UILogger;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.ErectShieldAction;
import challenge.game.model.Planet;

import java.util.Calendar;
import java.util.Random;

public class Shield
{
    private long shieldTimer = 0;
    private long lastShieldAttempt = 0;
    public Shield()
    {

    }

    public void resetShieldTimer(int planet_id) {
        shieldTimer = 0;
        lastShieldAttempt = 0;
        Planets.planetsShielded.removeIf(p -> p.getId() == planet_id);
    }

    public boolean isShieldReady(boolean isLateGamePhase) {
        long timeNow = Calendar.getInstance().getTimeInMillis();
        shieldTimer = shieldTimer - (timeNow - lastShieldAttempt);
        lastShieldAttempt = timeNow;

        if (isLateGamePhase) {
            return shieldTimer <= 0 || Planets.planetsShielded.size() < 2;
        } else {
            return shieldTimer <= 0 && Planets.planetsShielded.size() < 2;
        }
    }

    public ErectShieldAction handleShield(int planet_id) {
        if (planet_id == -1) {
            Planet endangeredPlanet = MyDataAnalysis.getDefPlanet();
            if (endangeredPlanet != null) {
                return getErectShieldAction(endangeredPlanet);
            }
        } else {
            Planet planet = Planets.getPlanetByID(planet_id);
            if (planet == null) throw new RuntimeException("Error, target planet does not exists");
            return getErectShieldAction(planet);
        }
        return null;
    }

    private ErectShieldAction getErectShieldAction(Planet planet) {
        ErectShieldAction shieldAction = Shield.erectShield(planet);
        if (shieldAction != null) {
            Planets.planetsShielded.add(planet);
            shieldTimer = Controll.game.getSettings().getTimeToBuildShild() + Controll.game.getSettings().getShildDuration();
            lastShieldAttempt = Calendar.getInstance().getTimeInMillis();
            return shieldAction;
        }
        throw new RuntimeException("An another shield was built in the meantime");
    }

    public static ErectShieldAction erectShield(Planet target) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        ErectShieldAction erectShieldAction = new ErectShieldAction();
        erectShieldAction.setTargetId(target.getId());
        erectShieldAction.setRefId(randomNumber);
        if (Planets.planetsShielded.size() >= 2) return null;
        WebSocketCommunication.sendGameAction(erectShieldAction);
        return erectShieldAction;
    }
}
