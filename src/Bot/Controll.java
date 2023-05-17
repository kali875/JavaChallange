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

    public static boolean gameStarted = false;
    public static int doingSomething = 0;
    boolean isLateGamePhase = (double) Planets.destroyed_planets.size() / Planets.numberOfAllPlanets > 0.75;
    private static DecisionHandler decisionHandler = new DecisionHandler();

    public static void onGameStarted(Game game_data) {
        game = game_data;
        InitialDataAnalysis initialDataAnalysis = new InitialDataAnalysis(game);
        List<Map.Entry<Planet, List<Planet>>> clusters = initialDataAnalysis.getClusters();
        decisionHandler.CalculatePlayerPlanets(Controll.game.getWorld().getPlanets().size(),Controll.game.getPlayers().size());
        decisionHandler.calculateDefAndAttackLimit(game);
        for (int i = 0; i < game.getSettings().getMaxWormHolesPerPlayer(); i++) {
            WormHole.sendWormHole(Planets.basePlanet.getX() + i - 1, Planets.basePlanet.getY() + 1,
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
        for (int i = 0; i < game.getSettings().getMaxConcurrentActions() - game.getSettings().getMaxWormHolesPerPlayer(); i++)
            decisionHandler.handle();
        System.out.println("Map size: height: " + game.getSettings().getHeight() + " - width: " + game.getSettings().getWidth());
    }

    private static void checkForActionStuck() {
        // it is possible that handler waits for action replenishment but stuck and become passive
        if (Actions.getRemainingActionCount() > 2 || (game.getSettings().getMaxConcurrentActions() <= 3 && Actions.getRemainingActionCount() > 1))
            handleReplenishedAction();
    }

    public static void onGravityWaveCrossingActionEffect(GravityWaveCrossing actionEffect)
    {
        if (!decisionHandler.isLateGamePhase())
            if ((double) Planets.destroyed_planets.size() / Planets.numberOfAllPlanets > 0.75)
                decisionHandler.reachedLateGamePhase();

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
            double threshold = calculateDefThreshold(((int) Controll.game.getWorld().getWidth()),(int)Controll.game.getWorld().getHeight(),Controll.game.getWorld().getPlanets().size(),Controll.game.getPlayers().size(),Planets.unhabitable_planets.size(),Planets.getPlanets_owned().size(),Controll.game.getWorld().getPlanets().size()- Planets.getPlanets().size(),Controll.game.getSettings().getPointsPerDerstroyedHostilePlanets());
            MyDataAnalysis.setFrequencyLimit((int) threshold);
            MyDataAnalysis.analData(actionEffect);
        };

        checkForActionStuck();
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
            decisionHandler.resetShieldData(actionEffect.getAffectedMapObjectId());
        }
        if (actionEffect.getInflictingPlayer() == JavalessWonders.getCurrentPlayer().getId()) MyDataAnalysis.analData(actionEffect);
    }

    public static void handleReplenishedAction() {
        while (decisionHandler.isDecisionHandlerHandlingAnAction()) continue;
        if (Actions.getRemainingActionCount() >= 1)
            decisionHandler.handle();
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
