package Bot;

import GameData.Actions;
import GameData.JavalessWonders;
import GameData.Planets;
import Utils.UILogger;
import challenge.game.event.action.GameAction;
import challenge.game.event.action.ShootMBHAction;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.model.Game;
import challenge.game.model.GravityWaveCause;
import challenge.game.model.Planet;
import challenge.game.model.Player;
import challenge.game.settings.GameSettings;
import org.glassfish.grizzly.utils.Pair;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class Controll
{
    public static Game game;
    private MBH MBH;
    private  Shield Shield;
    private SpaceMission SpaceMission;
    private  WormHole WormHole;
    private static Pair<GameAction, Planet> lastAction;

    public static final GameSettings gameSettings = null;

    public static List<GameAction> Commands = new ArrayList<GameAction>();

    public void StartStrategy()
    {

    }
    public void ChangeStrategy()
    {

    }

    public static void onGameStarted(Game game_data) {
        game = game_data;
        doSomething();
    }

    public static void onGravityWaveCrossingActionEffect(GravityWaveCrossing actionEffect) {
        UILogger.log_string("Gravity Wave Crossing Action Effect happened :)");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:" + actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Planet (id): " + actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: " + actionEffect.getTime());
        UILogger.log_string("Cause: " + actionEffect.getCause());
        UILogger.log_string("source (id): " + actionEffect.getSourceId());
        UILogger.log_string("direction: " + Math.toDegrees( actionEffect.getDirection() ));
        UILogger.log_string(".............................................");

        if (actionEffect.getInflictingPlayer() != JavalessWonders.getCurrentPlayer().getId())
            EnemyDataAnalysis.analyzeData(actionEffect);
        else if (actionEffect.getCause() == GravityWaveCause.EXPLOSION) {
            Planets.onPlanetDestroyed(actionEffect.getSourceId());
        }
        doSomething();
    }

    public static void onActionEffect(ActionEffect actionEffect) {

        UILogger.log_string("Regular Action Effect happened :)");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:" + actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Planet (id): " + actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: " + actionEffect.getTime());
        UILogger.log_string(".............................................");
    }

    public static Pair<GameAction, Planet> getLastAction() {return lastAction;}

    public static void setLastAction(GameAction action, Planet planet) {lastAction = new Pair<>(action, planet);}

    private static void doSomething() {
        if (Actions.getRemainingActionCount() < 1) return;
        if (Actions.getRemainingActionCount() > 1) {
            Planet p = EnemyDataAnalysis.GetEnemyPlanet();
            if (p != null) {
                GameAction action = null;
                for (int i = 0; i < 2; i++)
                    action = Bot.MBH.sendMBH(Planets.getPlanets_owned().get(0).getId(), p.getId());
                setLastAction(action, p);
                return;
            }
        }
        // Nincs 2 action fix pusztításhoz vagy nincs enemy planet, csináljon valamit random inkább
        Pair<Double, Pair<Planet, Planet>> closestPlanet = Planets.findClosestPlanets();
        if (new Random().nextInt(3) == 0 || closestPlanet == null) {
            Planet planet = null;
            while (planet == null) {
                if (Planets.unhabitable_planets.size() == 0) break;
                planet = Planets.getPlanetByID(Planets.unhabitable_planets.get(0));
                if (planet == null) Planets.unhabitable_planets.remove(0);
            }
            if (planet == null) return;
            GameAction action = Bot.MBH.sendMBH(Planets.getPlanets_owned().get(0).getId(), planet.getId());
            setLastAction(action, planet);
        }
        else {
            GameAction action = Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst().getId(), closestPlanet.getSecond().getSecond().getId());
            setLastAction(action, closestPlanet.getSecond().getSecond());
        }
    }
}
