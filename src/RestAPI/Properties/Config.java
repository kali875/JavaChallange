package RestAPI.Properties;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
public class Config
{
    //C:\\Users\\andra\\Desktop\\verseny\\private.key
    Reader rd = new FileReader("src/RestAPI/Properties/Config.txt");

     Hashtable<String, String> Properties = new Hashtable<String,String>();
    public Config() throws IOException
    {
        Fill_Properties();
    }
    public void Fill_Properties() throws IOException {
        Properties p = new Properties();
        p.load(rd);
        p.forEach((k, v) -> {
            Properties.put(k.toString(),v.toString());
        });
    }
    public String GetProperty(String Key) throws IOException
    {
        Set<String> setOfKeys = Properties.keySet();

        for (String key : setOfKeys)
        {
            if(key.equals(Key))
            {
                return Properties.get(key);
            }
        }
        return null;
    }
}
