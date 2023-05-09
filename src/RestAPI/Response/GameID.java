package RestAPI.Response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import challenge.game.rest.GameConfig;

@Data
public class GameID {
    @ApiModelProperty(value = "ID of the newly created game")
    private String gameId;

    @ApiModelProperty(value = "Game config")
    private GameConfig gameConfig;
}
