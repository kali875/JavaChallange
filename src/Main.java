import Bot.Controll;
import GameData.OnGoingMBHShots;
import GameData.Planets;
import RestAPI.API.Send;
import RestAPI.Model.Parameter;
import RestAPI.Properties.Config;
import RestAPI.Response.GameID;
import Utils.ConnectionHandler;
import Utils.ExistingGameIDParser;
import Utils.GameWorldTableRenderer;
import Utils.UILogger;
import WebSocket.WebSocketCommunication;
import challenge.game.model.Planet;
import challenge.game.rest.GameKey;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main
{
    private JTextField GameKeyTextField;
    private ScheduledExecutorService executor;
    private MyTask task;
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
    private JButton startGameButton;
    private JPanel GamePlace;
    private JButton stopGameButton;
    private JButton emergencyStopButton;
    private JButton reconnectButton;

    private final ConnectionHandler connectionHandler = new ConnectionHandler();
    private JTable GameWorld;
    private JScrollPane JtableScroll;
    private GameID gameID;
    private GameKey game;

    private final UILogger logger = new UILogger();

    private final Controll controll = new Controll();
    public Main()
    {
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectionHandler.handleGameStart(GameKeyTextField);
                try {
                    FillPlanet();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        GamePlace = new JPanel(null);
        GameWorld.setModel(GenerateWorld(112,63));
        JScrollPane pane = new JScrollPane(GameWorld);
        JtableScroll.getViewport().add (GameWorld);
        GameWorld.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        GameWorld.getColumnModel().getColumn(0).setPreferredWidth(5);
        SetWidth(GameWorld);

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
        init();
        logTextArea.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
    }
    public static void main(String[] args)
    {
        Main main = new Main();
        main.heartbeat.setFont(new Font("Arial Unicode MS", Font.PLAIN, 9));

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


        Timer logger_heartbeat = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.logger.log(main.logTextArea, main.heartbeat);
            }
        });

        logger_heartbeat.start();
        frame.setVisible(true);

    }
    public DefaultTableModel GenerateWorld(long width,long height)
    {
        //"width": 112,
        // "height": 63,
        DefaultTableModel model = new DefaultTableModel();
        for( int i = 0; i < height +1;i++)
        {
            model.addColumn("row"+i);
        }
        for (int i = 0; i < width +1;i++)
        {
            model.addRow(new Object[] { });

        }
        return model;
    }
    public void SetWidth(JTable table)
    {
        for (int i= 0; i < table.getColumnCount();i++)
        {
            table.getColumnModel().getColumn(i).setPreferredWidth(10);

        }
    }
    public void FillPlanet() throws InterruptedException {
        Thread.sleep(1000);
        for (Planet planet:Controll.game.getWorld().getPlanets())
        {
            GameWorld.getModel().setValueAt("?",(int)planet.getX(),(int)planet.getY());
        }
        // DefaultTableCellRenderer létrehozása
    }

    private void init()
    {
        // ...
        executor = Executors.newScheduledThreadPool(1);
        task = new MyTask(GameWorld);
        GameWorld.setDefaultRenderer(Object.class, new GameWorldTableRenderer());
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        // ...
    }
}
