package Bot;

import GameData.OnGoingSpaceMissions;
import GameData.Planets;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.EntryPointIndex;
import challenge.game.event.action.SpaceMissionAction;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.List;
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
        Pair<Pair<Double, Pair<Planet, Planet>>, List<Integer>> closestPlanetWH = Planets.findClosestPlanetsWH(false);
        if (closestPlanet == null && closestPlanetWH == null) throw new RuntimeException("There's no planet left to send a space mission...");
        if (closestPlanet.getFirst() <= closestPlanetWH.getFirst().getFirst()) {
            return Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst(), closestPlanet.getSecond().getSecond());
        } else if (closestPlanetWH.getSecond().size() == 2){
            return Bot.SpaceMission.sendSpaceMissionThroughWH(closestPlanetWH.getFirst().getSecond().getFirst(), closestPlanetWH.getFirst().getSecond().getSecond(), closestPlanetWH.getSecond().get(0), closestPlanetWH.getSecond().get(1));
        }
        throw new RuntimeException("Couldn't start the space mission, unexpected behaviour");
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

    public static SpaceMissionAction sendSpaceMissionThroughWH(Planet originalPlanet, Planet targetPlanet, int wormHoleId, int epi) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        SpaceMissionAction action = new SpaceMissionAction();
        action.setOriginId(originalPlanet.getId());
        action.setRefId(randomNumber);
        action.setTargetId(targetPlanet.getId());
        action.setWormHoleId(wormHoleId);
        action.setEntryPointIndex(epi == 0 ? EntryPointIndex.A : EntryPointIndex.B);

        OnGoingSpaceMissions.onSpaceMission(originalPlanet, targetPlanet);
        WebSocketCommunication.sendGameAction(action);

        return action;
    }
}
