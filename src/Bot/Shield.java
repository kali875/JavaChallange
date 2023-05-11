package Bot;

import WebSocket.WebSocketCommunication;
import challenge.game.event.action.ErectShieldAction;

import java.util.Random;

public class Shield
{
    public Shield()
    {

    }

    public static ErectShieldAction erectShield(int target_planet_id) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        ErectShieldAction erectShieldAction = new ErectShieldAction();
        erectShieldAction.setTargetId(target_planet_id);
        erectShieldAction.setRefId(randomNumber);
        WebSocketCommunication.sendGameAction(erectShieldAction);
        return erectShieldAction;
    }
}
