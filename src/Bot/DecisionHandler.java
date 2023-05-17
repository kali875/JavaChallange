package Bot;

import GameData.Actions;
import Utils.UILogger;
import challenge.game.event.action.GameAction;
import challenge.game.event.action.ShootMBHAction;

import java.util.ArrayList;
import java.util.List;

public class DecisionHandler {
    private MBH mbh;
    private Shield shield;
    private SpaceMission spaceMission;

    private boolean isLateGamePhase = false;

    public List<GameAction> handle() {
        if (Actions.getRemainingActionCount() < 1) return null;

        List<GameAction> actions = new ArrayList<>();
        /** Shield
         */
        try {
            if (shield.isShieldReady(isLateGamePhase))
                actions.add(shield.handleShield(-1));
        } catch (RuntimeException e) {
            // "An another shield was built in the meantime"
            UILogger.log_string(e.toString());
            if (Actions.getRemainingActionCount() < 1) return null;
        }
        if (!actions.isEmpty()) return actions;

        /** Space mission
         */
        if (!isLateGamePhase) {
            try {
                actions.add(spaceMission.planSpaceMission());
            } catch (RuntimeException e) {
                // "There's no planet left to send a space mission..."
                UILogger.log_string(e.toString());
                if (Actions.getRemainingActionCount() < 1) return null;
            }
        }
        if (!actions.isEmpty()) return actions;

        /** Shoot a planet (in case we shoot a planet we also need to shield it)
         * We need at least 2 but 3 action points for this in most cases
        */
        if ((Actions.getRemainingActionCount() > 2) || (Actions.getRemainingActionCount() > 1 && Controll.game.getSettings().getMaxConcurrentActions() <= 3)) {
            try {
                actions.addAll(mbh.shootOnBestPossibleTarget(isLateGamePhase));
                actions.add(shield.handleShield(((ShootMBHAction) actions.get(0)).getOriginId()));
            } catch (RuntimeException e) {
                UILogger.log_string(e.toString());
            }
        }

        if (!actions.isEmpty()) return actions;
        return null;
    }

    public void reachedLateGamePhase() {
        isLateGamePhase = true;
    }

}
