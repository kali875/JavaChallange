package Bot;

import Utils.UILogger;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.model.Game;
import challenge.game.settings.GameSettings;

public class Controll
{
    public static Game game;
    private MBH MBH;
    private  Shield Shield;
    private SpaceMission SpaceMission;
    private  WormHole WormHole;

    public static final GameSettings gameSettings = null;

    public void StartStrategy()
    {

    }
    public void ChangeStrategy()
    {

    }

    public static void onActionEffect(ActionEffect actionEffect) {
        UILogger.log_string("ACTION_EFFECT happened:");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:");
        UILogger.log_int(actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Planet (id): ");
        UILogger.log_int(actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: ");
        UILogger.log_long(actionEffect.getTime());
        UILogger.log_string(".............................................");
    }
}
