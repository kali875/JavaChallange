package Bot;

import GameData.OnGoingSpaceMissions;
import GameData.Planets;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.SpaceMissionAction;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.Random;

public class SpaceMission
{
    public SpaceMission()
    {

    }
    private void PlanetDensity()
    {

    }

    public SpaceMissionAction planSpaceMission() {
        Pair<Double, Pair<Planet, Planet>> closestPlanet = Planets.findClosestPlanets(false);
        if (closestPlanet == null) throw new RuntimeException("There's no planet left to send a space mission...");
        return Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst(), closestPlanet.getSecond().getSecond());
    }

    public static SpaceMissionAction sendSpaceMission(Planet originalPlanet, Planet targetPlanet) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        SpaceMissionAction action = new SpaceMissionAction();
        action.setOriginId(originalPlanet.getId());
        action.setRefId(randomNumber);
        action.setTargetId(targetPlanet.getId());

        OnGoingSpaceMissions.onSpaceMission(originalPlanet, targetPlanet);
        WebSocketCommunication.sendGameAction(action);

        return action;
    }
}
