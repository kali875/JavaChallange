package RestAPI.API;

import RestAPI.Authentication.Authentication;
import RestAPI.Model.Parameter;
import RestAPI.Properties.Config;
import Utils.UILogger;
import challenge.game.model.Game;
import challenge.game.rest.GameConfig;
import challenge.game.rest.GameType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpClient.newHttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
public class Send
{
    private JsonMapper jsonMapper= new JsonMapper();
    private List<Parameter> Parameters;
    private static Authentication auth = new Authentication();
    public HttpClient client;
    public HttpRequest request;
    public HttpResponse<String> response;
    private Config conf = new Config();
    private File Privatefile;
    private Signature sig = Signature.getInstance("SHA256withRSA");
    public String URL;
    public String BaseURL;
    public String GameKey;
    public String Sign;
    public Send (List<Parameter> Param,String BaseURL) throws Exception
    {
        this.BaseURL = BaseURL;
        Parameters = Param;
        client = newHttpClient();
        Privatefile = new File(conf.GetProperty("PrivateKeyPath").toString());
        System.setProperty("http.version", "HTTP/1.1");
        RSAPrivateKey privkey = auth.readPrivateKey(Privatefile);
        sig.initSign(privkey);
        UrlEncodes();
        CreateUrl();
        Signature();
        CreateUrl();
    }
    public Send (String BaseURL,String GameKey) throws Exception
    {
        this.GameKey = GameKey;
        this.BaseURL = BaseURL;
    }

    public void GetGameKey()
    {
        try
        {
            String test= null;
            client = newHttpClient();
                test = BaseURL;
                request = HttpRequest.newBuilder().uri(URI.create(conf.GetProperty("BaseURL").toString()+URL)).GET().build();
/*                client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response ->
                {
                    this.response = response;
                    System.out.println(response.statusCode());
                    return response;
                }).thenApply(HttpResponse::body).thenAccept(System.out::println);*/
                System.out.println("request sent");
                this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("got response");
                UILogger.log_int(response.statusCode());
                UILogger.log_string(response.body());
                //challenge.game.rest.GameKey gameAction = jsonMapper.readValue(response.body(), challenge.game.rest.GameKey.class);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void CreateGame(String json)
    {
        try
        {
            //{"gameConfig": {"bots": [0],"gameType": "SINGLE_PLAYER"},"gameId": "string"}
            //{"gameId":"96e8ae54-1f15-43ac-a55a-f30d33d1ee6e","gameConfig":{"gameType":"SINGLE_PLAYER","bots":[0]}}
            //"{ \"bots\": [ 0 ], \"gameType\": \"SINGLE_PLAYER\"}"

            json = "{\"bots\": [1],\"gameType\": \"SINGLE_PLAYER\"}";

            // TODO: KI NE KOMMENTELD CSAK HA MEGBESZÉLTÜK
            //json = "{\"bots\": [],\"gameType\": \"QUALIFYING\"}";

            //json = "{gameConfig: {bots: [0],gameType: SINGLE_PLAYER}";
            String test= json;
            client = newHttpClient();
            request = HttpRequest.newBuilder().uri(URI.create(this.BaseURL)).header("accept", "*/*").header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            /*client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response ->
            {
                System.out.println(response.statusCode());
                return response;
            }).thenApply(HttpResponse::body).thenAccept(System.out::println);*/
            this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
            UILogger.log_int(response.statusCode());
            UILogger.log_string(response.body());

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void startGame() throws URISyntaxException, IOException, InterruptedException {
        BaseURL = BaseURL;
        client = newHttpClient();
        request = HttpRequest.newBuilder().uri(URI.create(this.BaseURL)).header("accept", "*/*").header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("")).build();
        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());


        UILogger.log_int(response.statusCode());
        UILogger.log_string(response.body());
    }
    public void stopGame() throws URISyntaxException, IOException, InterruptedException {
        BaseURL = BaseURL;
        client = newHttpClient();
        request = HttpRequest.newBuilder().uri(URI.create(this.BaseURL)).header("accept", "*/*").header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("")).build();
        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
        UILogger.log_int(response.statusCode());
        UILogger.log_string(response.body());
    }

    public void CreateGame2(String json)
    {
        try {
            java.net.URL url = new java.net.URL("http://javachallenge.loxon.eu:8081/game/create/"+this.GameKey+"?disableInactivityWaves=false");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");

            String input = "{\"bots\": [0],\"gameType\": \"SINGLE_PLAYER\"}";
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void  Signature() throws SignatureException, UnsupportedEncodingException {
        sig.update(URL.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = sig.sign();
        Base64 encoder = new Base64();
        Sign = encoder.encodeToString(signatureBytes);
        Parameters.add(new Parameter("signature",auth.UrlEncode(Sign)));
    }
    public void CreateUrl()
    {
        final int[] i = {0};
        Parameters.forEach((n) -> {
            if(i[0] == 0)
            {
                URL= n.Name+"="+n.Value;
                i[0] = +1;
            }
            else
            {
                URL+= "&"+n.Name+"="+n.Value;
            }
        });
    }
    public void UrlEncodes()
    {
        Parameters.forEach((n) -> {
            try
            {
                n.Value = (auth.UrlEncode(n.Value));
                //Parameters.replace(k, v);
            } catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }

        });
    }
}
