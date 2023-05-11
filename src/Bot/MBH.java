package Bot;

import WebSocket.WebSocketCommunication;
import challenge.game.event.action.ShootMBHAction;

import java.util.Random;

public class MBH
{
    public MBH()
    {

    }

    public static ShootMBHAction sendMBH(int start_planet_id, int target_planet_id) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        ShootMBHAction action = new ShootMBHAction();
        action.setTargetId(target_planet_id);
        action.setOriginId(start_planet_id);
        action.setRefId(randomNumber);
        Controll.Commands.add(action);
        WebSocketCommunication.sendGameAction(action);
        return action;
    }
}
