package WebSocket;

import Bot.Controll;
import RestAPI.Response.GameID;
import Utils.UILogger;
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
    public static  boolean StartGame = false;
    @OnOpen
    public void onOpen() {
        UILogger.log_string("Connected to WebSocket server!");
    }

    @OnMessage
    public void onMessage(String message)
    {
        try
        {
            if(StartGame)
            {
                Controll.game = jsonMapper.readValue(message, Game.class);
                UILogger.log_string(message);
            }
            else
            {
                UILogger.log_string("Received message: " + message);
            }

            StartGame = false;
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
