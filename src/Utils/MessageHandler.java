package Utils;

import Bot.Controll;
import GameData.Actions;
import GameData.JavalessWonders;
import GameData.OnGoingSpaceMissions;
import GameData.Planets;
import challenge.game.event.ConnectionResult;
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
import challenge.game.model.Planet;
import challenge.game.model.WormHole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Calendar;

public class MessageHandler {
    public MessageHandler() {}
    private GameEvent gameEvent;
    private JsonMapper jsonMapper= new JsonMapper();

    private int playerID = -1;
    public void handleMessage(String message) {
        try {
            gameEvent = jsonMapper.readValue(message, GameEvent.class);
            //Controll.onGameEvent(gameEvent);

            if (gameEvent.getEventType() == EventType.CONNECTION_RESULT) {
                playerID = gameEvent.getConnectionResult().getPlayerId();
            } else if (gameEvent.getEventType() == EventType.GAME_STARTED) {
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
                if (gameEvent.getActionEffect() instanceof GravityWaveCrossing gw) {
                    handleGravityWave(gw);
                } else if (gameEvent.getActionEffect() instanceof WormHoleBuiltEffect wb) {
                    handleWormholeBuilt(wb);
                } else {
                    handleActionEffect(gameEvent.getActionEffect());
                }
            } else {
                // None - maybe it is possible that the server sends an unexpected GameEvent object?
                System.out.println(message);
            }
        } catch (JsonProcessingException e) {
            UILogger.log_string(message);
        }
    }

    private void handleGravityWave(GravityWaveCrossing gravityWaveCrossing) {
        Controll.onGravityWaveCrossingActionEffect(gravityWaveCrossing);
    }

    private void handleWormholeBuilt(WormHoleBuiltEffect wormHoleBuiltEffect) {
        WormHole temp = null;
        for (WormHole wh : Controll.wormHoles){
            if(wh.getId() == -1) temp = wh;
        }
        if(temp != null){
            Controll.wormHoles.remove(temp);
            temp.setId(wormHoleBuiltEffect.getWormHoleId());
            Controll.wormHoles.add(temp);
        }

        Controll.onActionEffect(wormHoleBuiltEffect);
    }

    private void handleActionEffect(ActionEffect actionEffect) {
        if (actionEffect.getEffectChain().contains(ActionEffectType.SPACE_MISSION_SUCCESS)) {
            Planets.onPlanetCaptured(actionEffect.getAffectedMapObjectId());
        }
        Controll.onActionEffect(actionEffect);
    }

    private void handleGameStarted(Game game) {
        JavalessWonders.setPlayerFromConnectionURLid(playerID, game.getPlayers());
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
                int newActionCount = Integer.parseInt(value);

                if (newActionCount > Actions.getRemainingActionCount() && Controll.gameStarted) {
                    Actions.onActionAttributeChange(newActionCount);

                    Controll.handleReplenishedAction();
                }
                if (newActionCount == 0 && !Controll.gameStarted) Controll.gameStarted = true;

                Planet target = OnGoingSpaceMissions.onActionReplenished(Calendar.getInstance().getTimeInMillis());
                if (target != null) {
                    if (new AttributeChangeChecker(attributeChanges.getChanges()).contains("numOfOwnedPlanets").equals("false")) {
                        if (!Planets.ownedPlanetsContains(target)) {
                            Planets.planetIsUnhabitable(target);
                        }
                    }
                }
            }

            // Something else implement that it later here
            UILogger.log_string(attributeChanges.toString());
        }
    }
}
