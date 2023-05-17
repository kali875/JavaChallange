package Utils;

import RestAPI.API.Send;
import RestAPI.Model.Parameter;
import RestAPI.Properties.Config;
import RestAPI.Response.GameID;
import WebSocket.WebSocketCommunication;
import challenge.game.rest.GameKey;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.swing.*;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler {
    private final JsonMapper jsonMapper= new JsonMapper();
    private GameID gameID = new GameID();
    private GameKey gameKey = new GameKey();
    private GameKey generateKey(JTextField GameKeyTextField) {
        try {
            List<Parameter> Parameters = new ArrayList<Parameter>();
            Config conf = null;
            conf = new Config();
            Parameters.add(new Parameter("email", conf.GetProperty("Email")));
            Parameters.add(new Parameter("team", conf.GetProperty("Team")));
            Parameters.add(new Parameter("ts", String.valueOf(System.currentTimeMillis())));

            //conf.GetProperty("BaseURL").toString()
            Send send = new Send(Parameters, conf.GetProperty("BaseURL"));
            send.GetGameKey();
            GameKey game_key = null;
            if(send.response != null)//responsecode == 200
            {
                game_key = jsonMapper.readValue(send.response.body(), GameKey.class);
                GameKeyTextField.setText(game_key.getKey());
            }
            return game_key;
        } catch (Exception ex) {
            System.out.println("There was an error during game key generation: " + ex);
            return null;
        }
    }

    private GameID createGame(JTextField gameIDTextField) {
        // csak azért kell, hogy a catchbe a ExistingGameIDParser tudja használni
        String responseBody = "";
        GameID game_id = null;
        try {
            Config conf = new Config();

            String url = conf.GetProperty("CreateGame") + gameKey.getKey()+"?disableInactivityWaves=false";
            Send send = new Send(url, gameKey.getKey());
            send.CreateGame("json");

            if(send.response != null) {
                responseBody = send.response.body();
                game_id = jsonMapper.readValue(send.response.body(), GameID.class);
                gameIDTextField.setText(game_id.getGameId());
            }
            return game_id;
        } catch (Exception ex) {
            try {
                String gameID = new ExistingGameIDParser().parseResponseBody(responseBody);
                game_id = new GameID();
                game_id.setGameId(gameID);
                gameIDTextField.setText(game_id.getGameId());
                return game_id;
            } catch (Exception exception) {
                System.out.println("There was an error during game creation: " + ex);
                return null;
            }
        }
    }

    private boolean connectToWebsocket(String playerId, String game_key, String game_id) {
        try {
            if (!game_key.isEmpty()) gameKey.setKey(game_key);
            if (!game_id.isEmpty()) gameID.setGameId(game_id);
            String websocket_uri_string;
            if (playerId.isEmpty()) websocket_uri_string = "ws://javachallenge.loxon.eu:8081/game?gameId=" + gameID.getGameId() + "&gameKey=" + gameKey.getKey() + "&connectionType=control";
            else websocket_uri_string = "ws://javachallenge.loxon.eu:8081/game?gameId=" + gameID.getGameId() + "&gameKey=" + gameKey.getKey() + "&connectionType=control";
            // ws://javachallenge.loxon.eu:8081/game?gameId=340bbe66-b1ab-4469-8b07-5bad3e022fb5&gameKey=cb5bc49e-3029-470b-b863-eb56bf6ad8cc&connectionType=visualization
            UILogger.log_string("Websocket URI was created:\n" + websocket_uri_string);
            URI websocket_uri = new URI(websocket_uri_string);
            return WebSocketCommunication.connect(websocket_uri);
        } catch (URISyntaxException | DeploymentException | IOException ex) {
            System.out.println("There was an error while connecting to the websocket: " + ex);
            return true;
        }
    }

    private void startGame() {
        try {
            new Send("http://javachallenge.loxon.eu:8081/game/start/" + gameID.getGameId() + "/" + gameKey.getKey(), "").startGame();
        } catch (Exception ex) {
            System.out.println("There was an error during game creation: " + ex);
        }
    }

    public void stopGame() {
        try {
            new Send("http://javachallenge.loxon.eu:8081/game/stop/" + gameID.getGameId() + "/" + gameKey.getKey(), "").stopGame();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    };

    public void handleGameStart(JTextField GameKeyTextField) {
        startGame();
    }

    public void handleWebsocketConnection(String playerID, String game_key, String game_id) {
        connectToWebsocket(playerID, game_key, game_id);
    }

    public void handleGameCreation(JTextField GameKeyTextField, JTextField gameIDTextField) {
        gameKey = generateKey(GameKeyTextField);
        if (gameKey == null) return;
        gameID = createGame(gameIDTextField);
    }

    public void handleReconnect(JTextField GameKeyTextField, JTextField gameIDTextField, String playerID,  String game_key, String game_id) {
        gameKey = generateKey(GameKeyTextField);
        if (gameKey == null) return;
        gameID = createGame(gameIDTextField);
        if (gameID == null) return;
        connectToWebsocket(playerID, game_key, game_id);
    }

    public void handleEmergencyStop(JTextField GameKeyTextField, JTextField gameIDTextField) {
        gameKey = generateKey(GameKeyTextField);
        if (gameKey == null) return;
        gameID = createGame(gameIDTextField);
        if (gameID == null) return;
        stopGame();
    }
}
