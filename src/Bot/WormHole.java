package Bot;

import GameData.OnGoingSpaceMissions;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.BuildWormHoleAction;
import challenge.game.event.action.SpaceMissionAction;
import challenge.game.model.Planet;

import java.util.Random;

public class WormHole
{
    public WormHole()
    {

    }

    public static BuildWormHoleAction sendWormHole(long xa, long ya, long xb, long yb) {
        BuildWormHoleAction action = new BuildWormHoleAction();
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        action.setRefId(randomNumber);
        action.setXa(xa);
        action.setXb(xb);
        action.setYa(ya);
        action.setYb(yb);
        WebSocketCommunication.sendGameAction(action);
        return action;
    }
}
