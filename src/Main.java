import RestAPI.API.Send;
import RestAPI.Model.Parameter;
import RestAPI.Properties.Config;
import challenge.game.model.Game;
import challenge.game.rest.GameConfig;
import challenge.game.rest.GameCreated;
import challenge.game.rest.GameKey;
import challenge.game.rest.GameType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    ObjectMapper mapper = new ObjectMapper();
    private JsonMapper jsonMapper= new JsonMapper();
    ;
    private javax.swing.JPanel JPanel;
    private JButton createGameKeyButton;
    private JButton createGameButton;
    private JTextField GameKeytextField;

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
                        GameKey game = jsonMapper.readValue(send.response.body().toString(), GameKey.class);
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
                    /*List<Parameter> Parameters = new ArrayList<Parameter>();
                    Config conf = null;
                    conf = new Config();

                    String Test = conf.GetProperty("CreateGame").toString()+GameKeytextField.getText();
                    Send send = new Send(conf.GetProperty("CreateGame").toString(),GameKeytextField.getText());

                    Thread t = new Thread(send);
                    t.start();

                    t.join();
                    if(send.response != null)//responsecode == 200
                    {
                        String bodystring = send.response.body().toString();
                        Game game = jsonMapper.readValue(send.response.body().toString(), Game.class);
                        //GameKeytextField.setText(game.getKey());
                    }*/

                    /*GameConfig gametype = new GameConfig();

                    List<Integer> bots = new ArrayList<>();
                    bots.add(1);
                    gametype.setGameType(GameType.SINGLE_PLAYER);
                    gametype.setBots(bots);
                    String json = jsonMapper.writeValueAsString(gametype);
                    HttpRequest.BodyPublisher bodyPublisher;

                    String test = conf.GetProperty("CreateGame").toString()+GameKeytextField.getText();
                    CloseableHttpClient client = HttpClientBuilder.create().build();
                    HttpPost httpPost = new HttpPost(conf.GetProperty("CreateGame").toString()+GameKeytextField.getText());
                    httpPost.addHeader("Content-Type", "application/json");



                    HttpEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    httpPost.setEntity(requestEntity);

                    CloseableHttpResponse response = client.execute(httpPost);
                    HttpEntity responseEntity = response.getEntity();
                    String responseString = EntityUtils.toString(responseEntity, "UTF-8");*/


                    /*Game game = jsonMapper.readValue(responseString, Game.class);
                    String asd = null;*/

                    //+"?disableInactivityWaves=true"
                    String url = conf.GetProperty("CreateGame").toString()+GameKeytextField.getText()+"?disableInactivityWaves=false";
                    //String url = conf.GetProperty("CreateGame").toString()+GameKeytextField.getText();
                    /*URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    // request method
                    con.setRequestMethod("POST");

                    // request headers
                    con.setRequestProperty("accept", "*");
                    /*con.setRequestProperty("Content-Type", "application/json");*/

                    /*GameCreated gc = new GameCreated();
                    gc.setGameId(GameKeytextField.getText());
                    GameConfig gameconfig = new GameConfig();
                    List<Integer> bots = new ArrayList<>();
                    bots.add(0);
                    GameType gm = GameType.SINGLE_PLAYER;
                    gameconfig.setGameType(gm);
                    gameconfig.setBots(bots);
                    gc.setGameConfig(gameconfig);
                    //String json = jsonMapper.writeValueAsString(gc);
                    String json =  mapper.writeValueAsString(gc);*/
                    Send send = new Send(url,GameKeytextField.getText());
                    send.CreateGame("json");

                    //{"gameConfig": {"bots": [0],"gameType": "SINGLE_PLAYER"},"gameId": "string"}
                    //{"gameId":"96e8ae54-1f15-43ac-a55a-f30d33d1ee6e","gameConfig":{"gameType":"SINGLE_PLAYER","bots":[0]}}
                    //{"gameConfig": {"bots": [0],"gameType": "SINGLE_PLAYER"},"gameId": "string"}
                    // request body
                    //String requestBody = "{ \"bots\": [ 0 ], \"gameType\": \"SINGLE_PLAYER\"}";;
                    /*CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpPost httpPost = new HttpPost(url);
                    //httpPost.setHeader("Accept", "*");
                    /*httpPost.setHeader("Content-type", "application/json");
                    StringEntity stringEntity = new StringEntity(json);
                    httpPost.setEntity(stringEntity);

                    System.out.println("Executing request " + httpPost.getRequestLine());
                    String wat = httpPost.getRequestLine().toString();
                    CloseableHttpResponse responseBody = httpclient.execute(httpPost);
                    HttpEntity responseEntity = responseBody.getEntity();
                    String responseString = EntityUtils.toString(responseEntity, "UTF-8");
                    String asd= null;*/
                } catch (IOException ex)
                {

                    throw new RuntimeException(ex);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("App");
        frame.setContentPane(new Main().JPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setSize(1000,1000);
        frame.setVisible(true);
    }
}
