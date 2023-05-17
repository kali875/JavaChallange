package Bot;

import GameData.OnGoingSpaceMissions;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.EntryPointIndex;
import challenge.game.event.action.SpaceMissionAction;
import challenge.game.model.Planet;

import java.util.Random;

public class SpaceMission
{
    public SpaceMission()
    {

    }
    private void PlanetDensity()
    {

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
