package Bot;

import GameData.Actions;
import GameData.OnGoingMBHShots;
import GameData.Planets;
import Utils.UILogger;
import challenge.game.event.action.GameAction;
import challenge.game.event.action.ShootMBHAction;
import challenge.game.model.Game;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DecisionHandler {
    private final MBH mbh = new MBH();
    private final Shield shield = new Shield();
    private final SpaceMission spaceMission = new SpaceMission();
    private boolean isLateGamePhase = false;
    public static int DefAndAttackLimit;
    // Pair<origin, target>
    private List<Pair<Planet, Planet>> defAndAttackLimitList = new ArrayList<>();

    private double UninhabitablePercentage = 0.3;

    private int PlayerPlanetsAroundNumber = 0;

    private boolean isActionHandled = false;

    public boolean isDecisionHandlerHandlingAnAction() {return isActionHandled;}

    public List<GameAction> handle() {
        isActionHandled = true;
        if (Actions.getRemainingActionCount() < 1) {
            isActionHandled = false;
            return null;
        }
        List<GameAction> actions = new ArrayList<>();

        if (!isLateGamePhase)
        {
            /** EarlyGame**/
            if(Planets.colonised_planet_count + 1 > PlayerPlanetsAroundNumber)
            {
                /** We have around Planet number limit**/
                //DefendAttack  First High priority enemyplanet
                //DefAndAttackMethod()
                if(Planets.getPlanets_owned().size() == DefAndAttackLimit)
                {
                    List<GameAction> _actions = defAndAttack();
                    if (_actions == null) return null;
                    isActionHandled = false;
                    return _actions;
                }
                else
                {
                    if (!defAndAttackLimitList.isEmpty()) defAndAttackLimitList = new ArrayList<>();
                    // double shooting and Def - More info in the function itself
                    List<GameAction> _actions = shootAndDef();
                    if (_actions == null) return null;
                    isActionHandled = false;
                    return _actions;
                }
            }
            else
            {
                /** SpaceMission,ShootPlanet**/
                // If an enemy is detected (it's detected more than the set frequencyLimit)
                if (!EnemyPlanets.enemyPlanets.isEmpty())
                    if (EnemyPlanets.getTheHighestKey() >= EnemyDataAnalysis.frequencyLimit) {
                        if ((Actions.getRemainingActionCount() > 1) || (Actions.getRemainingActionCount() > 0 && Controll.game.getSettings().getMaxConcurrentActions() <= 3)) {
                            /** Shoot a planet that is highly possible being an enemy planet
                             * We need at least 1 or 2 action points for this in most cases
                             */
                            try {
                                actions.addAll(mbh.shootOnBestPossibleTarget(false));
                            } catch (RuntimeException e) {
                                // "Not enough action point to shoot on possible enemy planet"
                                // "There's no planet left"
                                // "There's no unhabitable planet left to shot on..."
                                UILogger.log_string(e.toString());
                            }
                        } else {
                            // Returns so next time we will have enough action point to execute this
                            isActionHandled = false;
                            return null;
                        }
                    }

                // If a planet is endangered then try to shield that
                /** Automaticly detects the best possible shield target
                 *  This needs only 1 action point
                 */
                if (Actions.getRemainingActionCount() < 1) {
                    isActionHandled = false;
                    return null;
                }
                if (!DefensePlanets.defPlanets.isEmpty())
                    if (DefensePlanets.getTheHighestKey() >= MyDataAnalysis.frequencyLimit) {
                        try {
                            if (shield.isShieldReady(false))
                                actions.add(shield.handleShield(-1));
                        } catch (RuntimeException e) {
                            // "An another shield was built in the meantime"
                            UILogger.log_string(e.toString());
                        }
                    }

                // SpaceMission instead
                /** Automaticly detects the best possible mission target
                 *  This needs only 1 action point
                 */
                if (Actions.getRemainingActionCount() < 1) {
                    isActionHandled = false;
                    return null;
                }
                try {
                    actions.add(spaceMission.planSpaceMission());
                } catch (RuntimeException e) {
                    // "There's no planet left to send a space mission..."
                    UILogger.log_string(e.toString());
                }
            }
        }
        else
        {
            /** LateGame**/
            if(Planets.getPlanets_owned().size() > DefAndAttackLimit)
            {
                // double shooting and Def - More info in the function itself
                List<GameAction> _actions = shootAndDef();
                if (_actions == null) return null;
                isActionHandled = false;
                return _actions;
            }
            else if(Planets.getPlanets_owned().size() == DefAndAttackLimit)
            {
                List<GameAction> _actions = defAndAttack();
                if (_actions == null) return null;
                isActionHandled = false;
                return _actions;
            }
            else
            {
                if (!defAndAttackLimitList.isEmpty()) defAndAttackLimitList = new ArrayList<>();
                /** Only few planets remain
                 *  Try to shield our most endangered planets
                 *  Or shoot on a target
                 *  This will take 1 action point or 2 for enemy planet destruction
                 */
                try {
                    if (shield.isShieldReady(true))
                        actions.add(shield.handleShield(-1));
                    else actions.addAll(mbh.shootOnBestPossibleTarget(true));
                } catch (Exception e) {
                    UILogger.log_string(e.toString());
                }
            }
        }

        isActionHandled = false;
        if (actions.isEmpty()) return null;
        return actions;
    }

    private List<GameAction> shootAndDef() {
        // double shooting and Def
        List<GameAction> actions = new ArrayList<>();
        if ((Actions.getRemainingActionCount() > 2) || (Actions.getRemainingActionCount() > 1 && Controll.game.getSettings().getMaxConcurrentActions() <= 3)) {
            /** Shoot a planet (in case we shoot a planet we also need to shield it)
             * We need at least 2 or 3 action points for this in most cases
             */
            try {
                actions.addAll(mbh.shootOnBestPossibleTarget(false));
                actions.add(shield.handleShield(((ShootMBHAction) actions.get(0)).getOriginId()));
            } catch (RuntimeException e) {
                // "Not enough action point to shoot on possible enemy planet"
                // "There's no planet left"
                // "There's no unhabitable planet left to shot on..."
                UILogger.log_string(e.toString());
            }
        }

        if (actions.isEmpty()) return null;
        return actions;
    }

    public void reachedLateGamePhase() {
        isLateGamePhase = true;
    }

    public void resetShieldData(int planet_id) {
        shield.resetShieldTimer(planet_id);
    }

    public void CalculatePlayerPlanets(int totalPlanets, int numPlayers)
    {
        int inhabitablePlanets = (int) (totalPlanets * UninhabitablePercentage);
        PlayerPlanetsAroundNumber = inhabitablePlanets / numPlayers;
    }

    public boolean isLateGamePhase() {return isLateGamePhase;}

    public void calculateDefAndAttackLimit(Game game) {
        DefAndAttackLimit = MyDataAnalysis.NecessaryPlanetNumber(game.getSettings().getTimeToBuildShild(),game.getSettings().getShildDuration(),game.getSettings().getMaxConcurrentActions());
    }

    private List<GameAction> defAndAttack() {
        List<GameAction> actions = new ArrayList<>();

        if (defAndAttackLimitList.isEmpty()) getDefAndAttackMethodList();
        if (Actions.getRemainingActionCount() > 1) {
            Pair<Planet, Planet> queue_element = null;
            while (queue_element == null || !defAndAttackLimitList.isEmpty()) {
                queue_element = defAndAttackLimitList.get(0);
                if (Planets.getPlanetByID(queue_element.getFirst().getId()) == null
                        || Planets.getPlanetByID(queue_element.getSecond().getId()) == null) {
                    queue_element = null;
                }
                defAndAttackLimitList.remove(0);
            }
            if (queue_element == null) return null;
            OnGoingMBHShots.possibleTarget(queue_element.getSecond());
            actions.add(mbh.sendMBH(queue_element.getFirst().getId(), queue_element.getSecond().getId()));
            OnGoingMBHShots.onShot(queue_element.getSecond());
            actions.add(shield.handleShield(queue_element.getFirst().getId()));
        }

        return actions;
    }

    private void getDefAndAttackMethodList()
    {
        if (Planets.unhabitable_planets.isEmpty()) return;
        for (Planet planet : Planets.unhabitable_planets) {
            Planet p = Planets.findClosestOwnedPlanetToTarget(planet);
            if (p == null) break;
            defAndAttackLimitList.add(new Pair<>(p, planet));
        }
    }
}
