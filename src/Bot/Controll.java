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

    public static void onGravityWaveCrossingActionEffect(GravityWaveCrossing actionEffect)
    {
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
        {
            //int totalPlanets,                 int numPlayers,             int nonHabitablePlanets,        int ownedPlanets, int destroyedPlanets, int destroyedPlanetScore
            EnemyDataAnalysis.setFrequencyLimit((int)EnemyPlanetSequenceLimit(Controll.game.getWorld().getPlanets().size(),Controll.game.getPlayers().size(),Planets.unhabitable_planets.size(),Planets.getPlanets_owned().size(),Controll.game.getWorld().getPlanets().size()- Planets.getPlanets().size(),Controll.game.getSettings().getPointsPerDerstroyedHostilePlanets()));
            EnemyDataAnalysis.analyzeData(actionEffect);
        }
        else
        {
              //setFrequencyLimit                      //int width,                               int length,                              int totalPlanets,                               int numPlayers,                         int nonHabitablePlanets,         int ownedPlanets, int occupiedPlanets, int destroyedPlanetScore
            double threshold = calculateDefThreshold(((int) Controll.game.getWorld().getWidth()),(int)Controll.game.getWorld().getHeight(),Controll.game.getWorld().getPlanets().size(),Controll.game.getPlayers().size(),Planets.unhabitable_planets.size(),Planets.getPlanets_owned().size(),Controll.game.getWorld().getPlanets().size()- Planets.getPlanets().size(),Controll.game.getSettings().getPointsPerDerstroyedHostilePlanets());
            MyDataAnalysis.setFrequencyLimit(normalizeValue(threshold, 0, 1, 1, 100));
        }
        if (actionEffect.getCause() == GravityWaveCause.EXPLOSION)
        {

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

    private static void doSomething()
    {
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
    public static double EnemyPlanetSequenceLimit(int totalPlanets, int numPlayers, int nonHabitablePlanets, int ownedPlanets, int destroyedPlanets, int destroyedPlanetScore)
    {
        // Fuzzy freq limit calculation
        double habitablePlanetsRatio = (double) (totalPlanets - nonHabitablePlanets - destroyedPlanets) / totalPlanets;
        double ownedPlanetsRatio = (double) ownedPlanets / totalPlanets;

        // Fuzzy rule def
        double lowRatio = Math.min(habitablePlanetsRatio, ownedPlanetsRatio);
        double mediumRatio = Math.max(0, Math.min(habitablePlanetsRatio, 1 - ownedPlanetsRatio));
        double highRatio = Math.max(0, Math.min(1 - habitablePlanetsRatio, 1 - ownedPlanetsRatio));

        // Fuzzy freq limit values cal
        double lowLimit = lowRatio * numPlayers;
        double mediumLimit = mediumRatio * numPlayers;
        double highLimit = highRatio * numPlayers;

        // get Score the destroy planet
        double scoreFactor = 1 + (destroyedPlanets * destroyedPlanetScore);

        // median calculation
        double scatterLimit = (lowLimit + mediumLimit + highLimit) / 3 * scoreFactor;

        // between 1-100
        scatterLimit = Math.max(1, Math.min(100, scatterLimit));

        return scatterLimit;
    }
    public static double calculateDefThreshold(int width, int length, int totalPlanets, int numPlayers, int nonHabitablePlanets, int ownedPlanets, int occupiedPlanets, int destroyedPlanetScore)
    {
        double maxPossibleOwned = (double) totalPlanets / numPlayers;
        double maxPossibleOccupied = (double) totalPlanets - nonHabitablePlanets;

        double ownedMembership = calculateMembership(ownedPlanets, 0, (int) maxPossibleOwned, width, length);
        double occupiedMembership = calculateMembership(occupiedPlanets, 0, (int) maxPossibleOccupied, width, length);
        double destroyedMembership = calculateMembership(destroyedPlanetScore, 0, totalPlanets, width, length);

        double threshold = Math.max(Math.max(ownedMembership, occupiedMembership), destroyedMembership);
        return threshold;
    }

    public static double calculateMembership(int value, int min, int max, int width, int length) {
        double membership = 0.0;
        double scaleFactor = Math.sqrt(width * width + length * length);
        if (value <= min) {
            membership = 0.0;
        } else if (value >= max) {
            membership = 1.0;
        } else {
            membership = (double) (value - min) / (max - min) * scaleFactor;
        }
        return membership;
    }

    public static int normalizeValue(double value, double minValue, double maxValue, int normalizedMin, int normalizedMax) {
        double normalizedValue = ((value - minValue) / (maxValue - minValue)) * (normalizedMax - normalizedMin) + normalizedMin;
        return (int) Math.round(normalizedValue);
    }
}
