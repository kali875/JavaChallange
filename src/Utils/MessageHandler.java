package Utils;

import Bot.Controll;
import GameData.Actions;
import GameData.JavalessWonders;
import GameData.Planets;
import challenge.game.event.EventType;
import challenge.game.event.GameEvent;
import challenge.game.event.action.ActionResponse;
import challenge.game.event.action.GameActionType;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.event.actioneffect.ActionEffectType;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.event.actioneffect.WormHoleBuiltEffect;
import challenge.game.event.attibute.AttributeChanges;
import challenge.game.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class MessageHandler {
    public MessageHandler() {}
    private GameEvent gameEvent;
    private JsonMapper jsonMapper= new JsonMapper();
    public void handleMessage(String message) {
        try {
            gameEvent = jsonMapper.readValue(message, GameEvent.class);

            if (gameEvent.getEventType() == EventType.GAME_STARTED) {
                handleGameStarted(gameEvent.getGame());
            } else if (gameEvent.getEventType() == EventType.GAME_ENDED) {
                UILogger.log_string(message);
            } else if (gameEvent.getEventType() == EventType.CONNECTION_RESULT) {
                UILogger.log_string(message);
            } else if (gameEvent.getEventType() == EventType.ACTION) {
                handleActionResponse(gameEvent.getAction());
            } else if (gameEvent.getEventType() == EventType.ATTRIBUTE_CHANGE) {
                handleAttributeChange(gameEvent.getChanges());
            } else if (gameEvent.getEventType() == EventType.ACTION_EFFECT) {
                if (gameEvent.getActionEffect() instanceof GravityWaveCrossing) {
                    handleGravityWave((GravityWaveCrossing) gameEvent.getActionEffect());
                } else if (gameEvent.getActionEffect() instanceof WormHoleBuiltEffect) {
                    handleWormholeBuilt((WormHoleBuiltEffect) gameEvent.getActionEffect());
                } else {
                    handleActionEffect(gameEvent.getActionEffect());
                }
            } else {
                // None - maybe it is possible that the server sends an unexpected GameEvent object?
                System.out.println(message);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleGravityWave(GravityWaveCrossing gravityWaveCrossing) {
        Controll.onGravityWaveCrossingActionEffect(gravityWaveCrossing);
    }

    private void handleWormholeBuilt(WormHoleBuiltEffect wormHoleBuiltEffect) {
        // TODO: handle wormhole
        Controll.onActionEffect(wormHoleBuiltEffect);
    }

    private void handleActionEffect(ActionEffect actionEffect) {
        if (actionEffect.getEffectChain().contains(ActionEffectType.SPACE_MISSION_SUCCESS)) {
            Planets.onPlanetCaptured(actionEffect.getAffectedMapObjectId());
        }
        Controll.onActionEffect(actionEffect);
    }

    private void handleGameStarted(Game game) {
        JavalessWonders.setPlayerFromTeams(game.getPlayers());
        Planets.setPlanets(game.getWorld().getPlanets());
        Actions.onActionAttributeChange(game.getSettings().getMaxConcurrentActions());

        UILogger.log_string("Game started :) - Game setting: AFK");
        UILogger.log_string(".............................................");

        Controll.onGameStarted(game);
    }

    private void handleActionResponse(ActionResponse actionResponse) {
        // TODO: Implement this
        UILogger.log_string(actionResponse.toString() + "targetid" + actionResponse.getAction().getTargetId());
    }

    private void handleAttributeChange(AttributeChanges attributeChanges) {
        if (new AttributeChangeChecker(attributeChanges.getChanges()).contains("destroyed", "true")) {
            Planets.onPlanetDestroyed(attributeChanges.getAffectedId());
        } else {
            String value = new AttributeChangeChecker(attributeChanges.getChanges()).contains("numberOfRemainingActions");
            if (!value.equals("false")) {
                Actions.onActionAttributeChange(Integer.valueOf(value));
                if (Controll.getLastAction().getFirst().getType() == GameActionType.SPACE_MISSION) {
                    if (!Planets.getPlanets_owned().contains(Controll.getLastAction().getSecond())) {
                        Planets.planetIsUnhabitable(Controll.getLastAction().getSecond().getId());
                    }
                }
            }

            // Something else implement that it later here
            UILogger.log_string(attributeChanges.toString());
        }
    }
}
