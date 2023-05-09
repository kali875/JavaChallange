package WebSocket;

import Utils.UILogger;

import java.io.IOException;
import java.net.URI;
import javax.websocket.*;

@ClientEndpoint
public class WebSocketCommunication {
    @OnOpen
    public void onOpen() {
        UILogger.log_string("Connected to WebSocket server!");
    }

    @OnMessage
    public void onMessage(String message) {
        UILogger.log_string("Received message: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        UILogger.log_string("Disconnected from WebSocket server: " + closeReason);
    }

    public static void connect(URI websocket_uri) throws IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(WebSocketCommunication.class, websocket_uri);
    }
}
