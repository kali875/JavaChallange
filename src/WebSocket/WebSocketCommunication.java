package WebSocket;

import Bot.Controll;
import Utils.MessageHandler;
import GameData.Actions;
import Utils.UILogger;
import challenge.game.event.action.GameAction;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Session websocket_session;
    @OnOpen
    public void onOpen(Session session) {
        websocket_session = session;
        UILogger.log_string("Connected to WebSocket server!");
    }

    @OnMessage
    public void onMessage(String message)
    {
        new MessageHandler().handleMessage(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        UILogger.log_string("Disconnected from WebSocket server: " + closeReason);

    }

    public static boolean connect(URI websocket_uri) throws IOException, DeploymentException
    {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        return container.connectToServer(WebSocketCommunication.class, websocket_uri).isOpen();
    }

    public static void sendGameAction(GameAction gameAction) {
        try {
            String message = objectMapper.writeValueAsString(gameAction);
            websocket_session.getAsyncRemote().sendText(message);
            Actions.onActionUsed();
            Controll.Commands.add(gameAction);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
