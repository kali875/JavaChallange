package WebSocket;

import Bot.Controll;
import GameData.JavalessWonders;
import GameData.Planets;
import RestAPI.Response.GameID;
import Utils.UILogger;
import challenge.game.event.EventType;
import challenge.game.event.GameEvent;
import challenge.game.event.action.GameAction;
import challenge.game.event.actioneffect.ActionEffectType;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.event.actioneffect.WormHoleBuiltEffect;
import challenge.game.event.attibute.AttributeChange;
import challenge.game.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import javax.websocket.*;

@ClientEndpoint
public class WebSocketCommunication
{
    int planets_destroyed = 0;
    private JsonMapper jsonMapper= new JsonMapper();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Session websocket_session;
    @OnOpen
    public void onOpen(Session session) {
        websocket_session = session;
        UILogger.log_string("Connected to WebSocket server!");
    }

    @OnMessage
    public void onMessage(String message)
    {
        try
        {
            GameEvent gameEvent = jsonMapper.readValue(message, GameEvent.class);
            if (gameEvent.getEventType() == EventType.GAME_STARTED) {
                JavalessWonders.setPlayerFromTeams(gameEvent.getGame().getPlayers());
                Planets.setPlanets(gameEvent.getGame().getWorld().getPlanets());

                UILogger.log_string("Game started :) - Game setting: AFK");
                UILogger.log_string(".............................................");

                Controll.onGameStarted(gameEvent.getGame());
            } else if (gameEvent.getEventType() == EventType.ACTION_EFFECT) {
                if (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.SHIELD_DESTROYED)) {
                    System.out.println(message);
                    UILogger.log_string(message);
                }
                if (gameEvent.getActionEffect() instanceof WormHoleBuiltEffect) {
                    UILogger.log_string("Felépült egy féreglik (nincs lekezelve :( )");
                    UILogger.log_string("lyuk ID: " + ((WormHoleBuiltEffect) gameEvent.getActionEffect()).getWormHoleId());
                    UILogger.log_string(".............................................");
                } else if (gameEvent.getActionEffect() instanceof GravityWaveCrossing) {
                    Controll.onGravityWaveCrossingActionEffect((GravityWaveCrossing) gameEvent.getActionEffect());
                    if (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.SPACE_MISSION_GRAWITY_WAVE_PASSING)
                            && gameEvent.getActionEffect().getInflictingPlayer() == JavalessWonders.getCurrentPlayer().getId()) {
                        Planets.planetIsUnhabitable(gameEvent.getActionEffect().getAffectedMapObjectId());
                    } else if (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.MBH_HIT_GRAWITY_WAVE_START)
                    || (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.MBH_HIT_GRAWITY_WAVE_PASSING) &&
                            gameEvent.getActionEffect().getInflictingPlayer() == JavalessWonders.getCurrentPlayer().getId())) {
                        planets_destroyed += 1;
                    }
                } else {
                    // sima ActionEffect
                    if (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.SPACE_MISSION_SUCCESS)) {
                        Planets.onPlanetCaptured(gameEvent.getActionEffect().getAffectedMapObjectId());
                    }
                    Controll.onActionEffect(gameEvent.getActionEffect());
                }
            } else if (gameEvent.getEventType() == EventType.ATTRIBUTE_CHANGE) {
                System.out.println(gameEvent.getChanges().getChanges());
                if (gameEvent.getChanges().getChanges().contains(new AttributeChange("destroyed", "true"))) {
                    Planets.onPlanetDestroyed(gameEvent.getChanges().getAffectedId());
                }
            } else {
                UILogger.log_string(message);
            }
        } catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        UILogger.log_string("Disconnected from WebSocket server: " + closeReason);

    }

    public static void connect(URI websocket_uri) throws IOException, DeploymentException
    {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(WebSocketCommunication.class, websocket_uri);
    }

    public static void sendGameAction(GameAction gameAction) {
        try {
            String message = objectMapper.writeValueAsString(gameAction);
            websocket_session.getAsyncRemote().sendText(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
