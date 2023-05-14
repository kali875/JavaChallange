import Bot.Controll;
import GameData.Planets;
import RestAPI.API.Send;
import RestAPI.Model.Parameter;
import RestAPI.Properties.Config;
import RestAPI.Response.GameID;
import Utils.ConnectionHandler;
import Utils.ExistingGameIDParser;
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
    private JTextField GameKeyTextField;
    private JTextArea logTextArea;
    private JPanel container;
    private JTabbedPane tabbedPane;
    private JPanel outer_container;
    private JPanel inner_container;
    private JLabel heartbeat;
    private JButton startGameButton;
    private JPanel GamePlace;
    private JButton stopGameButton;
    private JButton emergencyStopButton;
    private JButton reconnectButton;

    private final ConnectionHandler connectionHandler = new ConnectionHandler();

    private final UILogger logger = new UILogger();

    private final Controll controll = new Controll();
    public Main()
    {
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {connectionHandler.handleGameStart(GameKeyTextField);}
        });
        stopGameButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {connectionHandler.stopGame();}
        });

        reconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {connectionHandler.handleReconnect(GameKeyTextField);}
        });

        emergencyStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {connectionHandler.handleEmergencyStop(GameKeyTextField);}
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
