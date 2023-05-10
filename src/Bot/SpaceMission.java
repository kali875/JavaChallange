package Bot;

import WebSocket.WebSocketCommunication;
import challenge.game.event.action.SpaceMissionAction;

import java.util.Random;

public class SpaceMission
{
    public SpaceMission()
    {

    }
    private void PlanetDensity()
    {

    }

    public static SpaceMissionAction sendSpaceMission(int start_planet_id, int target_planet_id) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        SpaceMissionAction action = new SpaceMissionAction();
        action.setOriginId(start_planet_id);
        action.setRefId(randomNumber);
        action.setTargetId(target_planet_id);
        WebSocketCommunication.sendGameAction(action);
        return action;
    }
}
