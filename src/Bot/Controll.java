package Bot;

import GameData.Actions;
import GameData.JavalessWonders;
import GameData.OnGoingMBHShots;
import GameData.Planets;
import DSS.InitialDataAnalysis;
import Utils.UILogger;
import challenge.game.event.action.ErectShieldAction;
import challenge.game.event.action.GameAction;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.event.actioneffect.ActionEffectType;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.model.Game;
import challenge.game.model.GravityWaveCause;
import challenge.game.model.Planet;
import challenge.game.settings.GameSettings;
import org.glassfish.grizzly.utils.Pair;

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
    public static List<challenge.game.model.WormHole> wormHoles = new ArrayList<>();

    private static long shieldTimer = 0;
    private static long lastShieldAttempt = 0;

    public static boolean gameStarted = false;
    public static int doingSomething = 0;
    private static double whatToDoMultiplier = 2.75;
    private static long inactivity_timer = 0;
    private static long lastInactivityCheck = 0;

    public static void onGameStarted(Game game_data) {
        game = game_data;
        InitialDataAnalysis initialDataAnalysis = new InitialDataAnalysis(game);
        List<Map.Entry<Planet, List<Planet>>> clusters = initialDataAnalysis.getClusters();
        for (int i = 0; i < game.getSettings().getMaxWormHolesPerPlayer(); i++) {
            WormHole.sendWormHole(Planets.basePlanet.getX() + i -1, Planets.basePlanet.getY() + 1,
                                    clusters.get(i).getKey().getX() + 1, clusters.get(i).getKey().getY());
            challenge.game.model.WormHole wh = new challenge.game.model.WormHole();
            wh.setPlayer(JavalessWonders.getCurrentPlayer().getId());
            wh.setId(-1);
            wh.setX((int)Planets.basePlanet.getX() + i -1);
            wh.setY((int)Planets.basePlanet.getY() + 1);
            wh.setXb((int)clusters.get(i).getKey().getX() + 1);
            wh.setYb((int)clusters.get(i).getKey().getY());
            wormHoles.add(wh);
        }
        for (int i = 0; i < game.getSettings().getMaxConcurrentActions(); i++)
            doSomething();
        System.out.println("Map size: height: " + game.getSettings().getHeight() + " - width: " + game.getSettings().getWidth());
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
        if (actionEffect.getCause() == GravityWaveCause.EXPLOSION)
            Planets.onPlanetDestroyed(actionEffect.getSourceId());

        if (actionEffect.getInflictingPlayer() == JavalessWonders.getCurrentPlayer().getId()) {
            double threshold = calculateDefThreshold(((int) Controll.game.getWorld().getWidth()),(int)Controll.game.getWorld().getHeight(),Controll.game.getWorld().getPlanets().size(),Controll.game.getPlayers().size(),Planets.inhabitable_planets.size(),Planets.getPlanets_owned().size(),Controll.game.getWorld().getPlanets().size()- Planets.getPlanets().size(),Controll.game.getSettings().getPointsPerDerstroyedHostilePlanets());
            //System.out.println(threshold);
            MyDataAnalysis.setFrequencyLimit((int) threshold);
            MyDataAnalysis.analData(actionEffect);
        };
        checkInactivity();
        doSomething();
    }


    public static void onActionEffect(ActionEffect actionEffect) {
        UILogger.log_string("Regular Action Effect happened :)");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:" + actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Object (id): " + actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: " + actionEffect.getTime());
        UILogger.log_string(".............................................");

        if (actionEffect.getEffectChain().contains(ActionEffectType.SHIELD_DESTROYED)
            || actionEffect.getEffectChain().contains(ActionEffectType.SHIELD_TIMEOUT)
        ) {
            Planets.planetsShielded.removeIf(p -> p.getId() == actionEffect.getAffectedMapObjectId());
            shieldTimer = 0;
            lastShieldAttempt = 0;
        }
        if (actionEffect.getInflictingPlayer() == JavalessWonders.getCurrentPlayer().getId()) MyDataAnalysis.analData(actionEffect);
        checkInactivity();
    }

    private static void doneSomething() {
        doingSomething--;
        inactivity_timer = 0;
        lastInactivityCheck = Calendar.getInstance().getTimeInMillis();
    }

    private static void checkInactivity() {
        long timeNow = Calendar.getInstance().getTimeInMillis();
        inactivity_timer = inactivity_timer + (timeNow - lastInactivityCheck);
        lastInactivityCheck = timeNow;
        if (inactivity_timer > game.getSettings().getPassivityTimeTreshold() * 0.3)
            if (Actions.getRemainingActionCount() - doingSomething > 1) {
                System.out.println("INACTIVITY! " + inactivity_timer);
                inactivity_timer = 0;
                lastInactivityCheck = Calendar.getInstance().getTimeInMillis();
                doSomething();
            }
    }

    public static void doSomething() {
        doingSomething++;
        if (Actions.getRemainingActionCount() < 1) {
            doneSomething();
            return;
        }
        if (Actions.getRemainingActionCount() == 1 && EnemyDataAnalysis.isThereEnemyWithHighChance()) {
            doneSomething();
            return;
        }
        if (Actions.getRemainingActionCount() > 1) {
            Planet p = EnemyDataAnalysis.GetEnemyPlanet();
            if (p != null) {
                for (int i = 0; i < 2; i++) {
                    Planet q = Planets.findClosestOwnedPlanetToTarget(p);
                    if(q != null)
                        Bot.MBH.sendMBH(q.getId(), p.getId());
                }
                UILogger.log_string("Possible Enemy planet shot! -> " + p.getId());
                doneSomething();
                return;
            }
        }
        // Nincs 2 action fix pusztításhoz vagy nincs enemy planet
        // megpróbál védekezni
        if (shieldTimer <= 0 && Planets.planetsShielded.size() < 2) {
            Planet endangeredPlanet = MyDataAnalysis.getDefPlanet();
            if (endangeredPlanet != null) {
                ErectShieldAction shieldAction = Shield.erectShield(endangeredPlanet);
                if (shieldAction == null) {
                    doingSomething--;
                    System.out.println("0");
                    return;
                }
                Planets.planetsShielded.add(endangeredPlanet);
                shieldTimer = game.getSettings().getTimeToBuildShild() + game.getSettings().getShildDuration();
                lastShieldAttempt = Calendar.getInstance().getTimeInMillis();
                UILogger.log_string("Shield erected! -> " + endangeredPlanet.getId());
                doneSomething();
                return;
            }
        } else {
            long timeNow = Calendar.getInstance().getTimeInMillis();
            shieldTimer = shieldTimer - (timeNow - lastShieldAttempt);
            lastShieldAttempt = timeNow;
        }
        // nem járt még le a pajzs vagy nincs még veszélyeztetett bolygó
        // küldetés próbál küldeni
        boolean isLateGamePhase = (double) Planets.destroyed_planets.size() / Planets.numberOfAllPlanets > 0.8;
        Pair<Double, Pair<Planet, Planet>> closestPlanet = Planets.findClosestPlanets(isLateGamePhase);
        Pair<Pair<Double, Pair<Planet, Planet>>, List<Integer>> closestPlanetWH = Planets.findClosestPlanetsWH(isLateGamePhase);
        if (closestPlanet != null) OnGoingMBHShots.possibleTarget(closestPlanet.getSecond().getSecond());
        if (!isLateGamePhase) {
            Pair<Double, Pair<Planet, Planet>> closestInhabitablePlanet = Planets.findClosestUnhabitablePlanet();
            if (closestInhabitablePlanet != null) OnGoingMBHShots.possibleTarget(closestInhabitablePlanet.getSecond().getSecond());

            if(closestPlanet != null && closestPlanetWH != null){
                if (closestPlanet.getFirst() <= closestPlanetWH.getFirst().getFirst()) {
                    Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst(), closestPlanet.getSecond().getSecond());
                    UILogger.log_string("Space Mission Sent! -> " + closestPlanet.getSecond().getSecond().getId());
                }else if(closestPlanetWH.getSecond().size() == 2 && closestPlanetWH.getSecond().get(0) != -1 && closestPlanetWH.getSecond().get(1) != -1){
                    Bot.SpaceMission.sendSpaceMissionThroughWH(closestPlanetWH.getFirst().getSecond().getFirst(), closestPlanetWH.getFirst().getSecond().getSecond(), closestPlanetWH.getSecond().get(0), closestPlanetWH.getSecond().get(1));
                    UILogger.log_string("Space Mission Wormhole Sent! -> " + closestPlanetWH.getFirst().getSecond().getSecond().getId());
                }
                doneSomething();
                return;
            }
            if(closestInhabitablePlanet != null){
                if (closestPlanet == null) {
                    Bot.MBH.sendMBH(closestInhabitablePlanet.getSecond().getFirst().getId(), closestInhabitablePlanet.getSecond().getSecond().getId());
                    OnGoingMBHShots.onShot(closestInhabitablePlanet.getSecond().getSecond());
                    UILogger.log_string("Unhabitable planet shot! -> " + closestInhabitablePlanet.getSecond().getSecond().getId());
                    doneSomething();
                    return;
                }
                if (closestInhabitablePlanet.getFirst() * whatToDoMultiplier > closestPlanet.getFirst()) {
                    Bot.SpaceMission.sendSpaceMission(closestPlanet.getSecond().getFirst(), closestPlanet.getSecond().getSecond());
                    UILogger.log_string("Space Mission Sent! -> " + closestPlanet.getSecond().getSecond().getId());
                    doneSomething();
                } else {
                    Bot.MBH.sendMBH(closestInhabitablePlanet.getSecond().getFirst().getId(), closestInhabitablePlanet.getSecond().getSecond().getId());
                    OnGoingMBHShots.onShot(closestInhabitablePlanet.getSecond().getSecond());
                    UILogger.log_string("Inhabitable planet shot! -> " + closestInhabitablePlanet.getSecond().getSecond().getId());
                    doneSomething();
                }
            }
        } else if(closestPlanet != null){
            Bot.MBH.sendMBH(closestPlanet.getSecond().getFirst().getId(), closestPlanet.getSecond().getSecond().getId());
            UILogger.log_string("Unknown planet shot! -> " + closestPlanet.getSecond().getSecond().getId());
            doneSomething();
        }
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
        return threshold / 10;
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
