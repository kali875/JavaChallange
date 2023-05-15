package Bot;

import GameData.Planets;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.ErectShieldAction;
import challenge.game.model.Planet;

import java.util.Random;

public class Shield
{
    public Shield()
    {

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
