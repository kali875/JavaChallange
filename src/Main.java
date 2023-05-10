import Bot.Controll;
import RestAPI.API.Send;
import RestAPI.Model.Parameter;
import RestAPI.Properties.Config;
import RestAPI.Response.GameID;
import Utils.UILogger;
import WebSocket.WebSocketCommunication;
import challenge.game.rest.GameKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.swing.*;
import javax.websocket.DeploymentException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    ObjectMapper mapper = new ObjectMapper();
    private JsonMapper jsonMapper= new JsonMapper();
    private JButton createGameKeyButton;
    private JButton createGameButton;
    private JTextField GameKeytextField;
    private JTextArea logTextArea;
    private JPanel container;
    private JTabbedPane tabbedPane;
    private JPanel outer_container;
    private JPanel inner_container;
    private JLabel heartbeat;
    private JButton websocketButton;
    private JButton startGameButton;
    private JPanel GamePlace;
    private JButton stopGameButton;
    private GameID gameID;
    private GameKey game;

    private UILogger logger = new UILogger();

    private Controll controll = new Controll();
    public Main()
    {
        //{"httpStatusCode":200,"key":"229c4353-b059-4204-8673-dc65ed0ef0cd","message":"The game key has been successfully generated."}
        createGameKeyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<Parameter> Parameters = new ArrayList<Parameter>();
                Config conf = null;
                try {
                    conf = new Config();
                    Parameters.add(new Parameter("email", conf.GetProperty("Email").toString()));
                    Parameters.add(new Parameter("team", conf.GetProperty("Team").toString()));
                    Parameters.add(new Parameter("ts", String.valueOf(System.currentTimeMillis())));

                    //conf.GetProperty("BaseURL").toString()
                    Send send = new Send(Parameters,conf.GetProperty("BaseURL").toString());

                    send.GetGameKey();


                    /*Thread t = new Thread(send);
                    t.start();

                    t.join();*/

                    if(send.response != null)//responsecode == 200
                    {
                        String bodystring = send.response.body().toString();
                        game = jsonMapper.readValue(send.response.body().toString(), GameKey.class);
                        GameKeytextField.setText(game.getKey());
                    }

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        createGameButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Config conf = null;
                    conf = new Config();

                    String url = conf.GetProperty("CreateGame").toString()+GameKeytextField.getText()+"?disableInactivityWaves=false";
                    Send send = new Send(url,GameKeytextField.getText());
                    send.CreateGame("json");
                    if(send.response != null) {
                        gameID = jsonMapper.readValue(send.response.body(), GameID.class);
                    }
                } catch (IOException ex)
                {

                    throw new RuntimeException(ex);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        websocketButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // ws://javachallenge.loxon.eu:8081/game?gameId=340bbe66-b1ab-4469-8b07-5bad3e022fb5&gameKey=cb5bc49e-3029-470b-b863-eb56bf6ad8cc&connectionType=visualization
                    String websocket_uri_string = "ws://javachallenge.loxon.eu:8081/game?gameId=" + gameID.getGameId() + "&gameKey=" + GameKeytextField.getText() + "&connectionType=control";
                    UILogger.log_string("Websocket URI was created:\n" + websocket_uri_string);
                    URI websocket_uri = null;
                    websocket_uri = new URI(websocket_uri_string);
                    WebSocketCommunication.connect(websocket_uri);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                } catch (DeploymentException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Send send = new Send("http://javachallenge.loxon.eu:8081/game/start/" + gameID.getGameId() + "/" + game.getKey(), "");
                    send.startGame();
                    WebSocketCommunication.StartGame = true;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        stopGameButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Send send = null;
                try {
                    send = new Send("http://javachallenge.loxon.eu:8081/game/stop/" + gameID.getGameId() + "/" + game.getKey(), "");
                    send.stopGame();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
    }
    public static void main(String[] args)
    {
        Main main = new Main();
        main.heartbeat.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));

        JFrame frame = new JFrame("App");
        frame.setContentPane(main.tabbedPane);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                FileWriter writer = null;
                try {
                    writer = new FileWriter("log.txt");
                    writer.write(main.logTextArea.getText());
                    writer.close();
                } catch (IOException ex) {
                    System.out.println("Couldn't write data to the log file");
                }
                super.windowClosing(e);
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setSize(1000,1000);
        frame.setVisible(true);

        Timer logger_heartbeat = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.logger.log(main.logTextArea, main.heartbeat);
            }
        });

        logger_heartbeat.start();
    }
}
