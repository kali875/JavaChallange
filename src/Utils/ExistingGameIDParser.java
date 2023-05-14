package Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExistingGameIDParser {
    // Invalid request. (Each team is allowed to execute only one game at a time. Team Javaless Wonders has a running game with gameId: 27bc1d41-bb34-43ec-8ec7-f1cec622688d  Stop it if you are finished!)
    public String parseResponseBody(String responseBody) {
        Pattern pattern = Pattern.compile("gameId: ([\\w-]+)");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException(responseBody);
        }
    }
}
