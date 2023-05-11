package GameData;

import challenge.game.model.Player;

import java.util.List;
import java.util.Optional;

public class JavalessWonders {
    private static Player player;

    public static void setPlayerFromTeams(List<Player> players) {
        Optional<Player> optional_javaless_wonders = players.stream()
                .filter(player -> player.getTeamName().equals("Javaless Wonders"))
                .findFirst();
        optional_javaless_wonders.ifPresent(value -> player = value);
    }

    public static Player getCurrentPlayer() {
        return player;
    }
}
