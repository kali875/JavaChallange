package Bot;

import GameData.Actions;
import GameData.JavalessWonders;
import GameData.OnGoingMBHShots;
import GameData.Planets;
import Utils.UILogger;
import challenge.game.event.EventType;
import challenge.game.event.GameEvent;
import challenge.game.event.action.GameAction;
import challenge.game.event.action.ShootMBHAction;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.event.actioneffect.ActionEffectType;
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
import java.math.BigDecimal;
import java.util.*;

public class Controll
{
    public static Game game;
/*    private MBH MBH;
    private Shield Shield;
    private SpaceMission SpaceMission;
    private  WormHole WormHole;*/

    public static final GameSettings gameSettings = null;

    public static List<GameAction> Commands = new ArrayList<GameAction>();

    private static long shieldTimer = 0;
    private static long lastShieldAttempt = 0;

    public static boolean gameStarted = false;
    public static int doingSomething = 0;

    public void StartStrategy()
    {

    }
    public void ChangeStrategy()
    {

    }

    public static void onGameStarted(Game game_data) {
        game = game_data;
        for (int i = 0; i < game.getSettings().getMaxConcurrentActions(); i++)
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

        if (actionEffect.getInflictingPlayer() != JavalessWonders.getCurrentPlayer().getId()) {
            EnemyDataAnalysis.analyzeData(actionEffect);
        }
        /*if (actionEffect.getInflictingPlayer() != JavalessWonders.getCurrentPlayer().getId())
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
        }*/

        if (actionEffect.getCause() == GravityWaveCause.EXPLOSION)
            Planets.onPlanetDestroyed(actionEffect.getSourceId());
    }

    public static void onGameEvent(GameEvent gameEvent) {
        if (gameEvent.getEventType() == EventType.ACTION_EFFECT)
            MyDataAnalysis.analData(gameEvent);
/*        if (actionEffect.getInflictingPlayer() != JavalessWonders.getCurrentPlayer().getId())
        {
            //int totalPlanets,                 int numPlayers,             int nonHabitablePlanets,        int ownedPlanets, int destroyedPlanets, int destroyedPlanetScore
            EnemyDataAnalysis.setFrequencyLimit((int)EnemyPlanetSequenceLimit(Controll.game.getWorld().getPlanets().size(),Controll.game.getPlayers().size(),Planets.unhabitable_planets.size(),Planets.getPlanets_owned().size(),Controll.game.getWorld().getPlanets().size()- Planets.getPlanets().size(),Controll.game.getSettings().getPointsPerDerstroyedHostilePlanets()));
            EnemyDataAnalysis.analyzeData(actionEffect);
        }*/
        /*
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
        doSomething();*/
    }

    public static void onActionEffect(ActionEffect actionEffect) {
        UILogger.log_string("Regular Action Effect happened :)");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:" + actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Planet (id): " + actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: " + actionEffect.getTime());
        UILogger.log_string(".............................................");

        if (actionEffect.getEffectChain().contains(ActionEffectType.SHIELD_DESTROYED)) shieldTimer = 0;
    }

    public static void doSomething() {
        doingSomething++;
        if (Actions.getRemainingActionCount() < 1) {
            doingSomething--;
            return;
        }
        if (Actions.getRemainingActionCount() == 1 && EnemyDataAnalysis.isThereEnemyWithHighChance()) {
            doingSomething--;
            return;
        }
        if (Actions.getRemainingActionCount() > 1) {
            Planet p = EnemyDataAnalysis.GetEnemyPlanet();
            if (p != null) {
                for (int i = 0; i < 2; i++)
                    Bot.MBH.sendMBH(Planets.findClosestOwnedPlanetToTarget(p).getId(), p.getId());
                UILogger.log_string("Possible Enemy planet shot! -> " + p.getId());
                doingSomething--;
                return;
            }
        }
        // Nincs 2 action fix pusztításhoz vagy nincs enemy planet
        // megpróbál védekezni
        if (shieldTimer <= 0) {
            Planet endangeredPlanet = MyDataAnalysis.getDefPlanet();
            if (endangeredPlanet != null) {
                Shield.erectShield(endangeredPlanet.getId());
                shieldTimer = game.getSettings().getTimeToBuildShild() + game.getSettings().getShildDuration();
                lastShieldAttempt = Calendar.getInstance().getTimeInMillis();
                UILogger.log_string("Shield erected! -> " + endangeredPlanet.getId());
                doingSomething--;
                return;
            }
        } else {
            long timeNow = Calendar.getInstance().getTimeInMillis();
            shieldTimer = shieldTimer - (lastShieldAttempt - timeNow);
            lastShieldAttempt = timeNow;
        }
        // nem járt még le a pajzs vagy nincs még veszélyeztetett bolygó
        // küldetés próbál küldeni
        Pair<Double, Pair<Planet, Planet>> closestPlanet = Planets.findClosestPlanets();
        Pair<Double, Pair<Planet, Planet>> closestUnhabitablePlanet = Planets.findClosestUnhabitablePlanet();
        if (closestPlanet == null && closestUnhabitablePlanet == null) {
            UILogger.log_string("doSomething() - There was nothing i could do");
            UILogger.log_string("Stats:");
            UILogger.log_string("Number of planets left: " + Planets.getPlanets().size());
            UILogger.log_string("Own planet count: " + Planets.getPlanets_owned().size());
            UILogger.log_string("Unhabitable planets count: " + Planets.unhabitable_planets.size());
            doingSomething = 10;
            Actions.onActionAttributeChange(-10);
            return;
        }
        if (closestPlanet != null && closestUnhabitablePlanet == null) {
            Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst(), closestPlanet.getSecond().getSecond());
            UILogger.log_string("Space Mission Sent! -> " + closestPlanet.getSecond().getSecond().getId());
            doingSomething--;
            return;
        }
        if (closestPlanet == null) {
            Bot.MBH.sendMBH(closestUnhabitablePlanet.getSecond().getFirst().getId(), closestUnhabitablePlanet.getSecond().getSecond().getId());
            OnGoingMBHShots.onShot(closestUnhabitablePlanet.getSecond().getSecond());
            UILogger.log_string("Unhabitable planet shot! -> " + closestUnhabitablePlanet.getSecond().getSecond().getId());
            doingSomething--;
            return;
        }
        if (closestUnhabitablePlanet.getFirst() * 2.5 > closestPlanet.getFirst()) {
            Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst(), closestPlanet.getSecond().getSecond());
            UILogger.log_string("Space Mission Sent! -> " + closestPlanet.getSecond().getSecond().getId());
        } else {
            Bot.MBH.sendMBH(closestUnhabitablePlanet.getSecond().getFirst().getId(), closestUnhabitablePlanet.getSecond().getSecond().getId());
            OnGoingMBHShots.onShot(closestUnhabitablePlanet.getSecond().getSecond());
            UILogger.log_string("Unhabitable planet shot! -> " + closestUnhabitablePlanet.getSecond().getSecond().getId());
        }
        doingSomething--;
    }

    public static double calculateScatterLimit(int totalPlanets, int numPlayers, int nonHabitablePlanets, int ownedPlanets, int destroyedPlanets)
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

        // median
        double scatterLimit = (lowLimit + mediumLimit + highLimit) / 3;

        // between 1-100
        scatterLimit = Math.max(1, Math.min(100, scatterLimit));

        return scatterLimit;
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
