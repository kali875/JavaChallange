package WebSocket;

import Bot.Controll;
import RestAPI.Response.GameID;
import Utils.UILogger;
import challenge.game.event.EventType;
import challenge.game.event.GameEvent;
import challenge.game.event.actioneffect.ActionEffectType;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.event.actioneffect.WormHoleBuiltEffect;
import challenge.game.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import javax.websocket.*;

@ClientEndpoint
public class WebSocketCommunication
{
    private JsonMapper jsonMapper= new JsonMapper();
    @OnOpen
    public void onOpen() {
        UILogger.log_string("Connected to WebSocket server!");
    }

    @OnMessage
    public void onMessage(String message)
    {
        try
        {
            GameEvent gameEvent = jsonMapper.readValue(message, GameEvent.class);
            if (gameEvent.getEventType() == EventType.GAME_STARTED) {
                Controll.game = gameEvent.getGame();
                UILogger.log_string("Game started :) - Game setting: AFK");
                UILogger.log_string(".............................................");
            } else if (gameEvent.getEventType() == EventType.ACTION_EFFECT) {
                if (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.WORM_HOLE_BUILT)) {
                    UILogger.log_string("Felépült egy féreglik (nincs lekezelve :( )");
                    UILogger.log_string("lyuk ID: " + ((WormHoleBuiltEffect) gameEvent.getActionEffect()).getWormHoleId());
                    UILogger.log_string(".............................................");
                } else if (gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.MBH_HIT_GRAWITY_WAVE_PASSING)
                        || gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.WORM_HOLE_BUILT_GRAWITY_WAVE_START)
                        || gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.WORM_HOLE_BUILT_GRAWITY_WAVE_PASSING)
                        || gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.SPACE_MISSION_GRAWITY_WAVE_PASSING)
                        || gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.SPACE_MISSION_GRAWITY_WAVE_START)
                        || gameEvent.getActionEffect().getEffectChain().contains(ActionEffectType.MBH_HIT_GRAWITY_WAVE_START)) {
                    Controll.onGravityWaveCrossingActionEffect((GravityWaveCrossing) gameEvent.getActionEffect());
                } else {
                    // sima ActionEffect
                    Controll.onActionEffect(gameEvent.getActionEffect());
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
}
