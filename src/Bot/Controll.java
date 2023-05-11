package Bot;

import GameData.Planets;
import Utils.UILogger;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.model.Game;
import challenge.game.model.Planet;
import challenge.game.model.Player;
import challenge.game.settings.GameSettings;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class Controll
{
    public static Game game;
    private MBH MBH;
    private  Shield Shield;
    private SpaceMission SpaceMission;
    private  WormHole WormHole;
    private static Planet base_planet;
    private static Player javaless_wonders;

    public static final GameSettings gameSettings = null;

    public void StartStrategy()
    {

    }
    public void ChangeStrategy()
    {

    }

    public static void onGameStarted(Game game_data) {
        game = game_data;
        SortedMap<Float, Planet> planetMap = new TreeMap<>(new Comparator<Float>() {
            @Override
            public int compare(Float f1, Float f2) {
                return Float.compare(f1, f2);
            }
        });


        Optional<Player> optional_javaless_wonders = game.getPlayers().stream()
                .filter(player -> player.getTeamName().equals("Javaless Wonders"))
                .findFirst();

        if (optional_javaless_wonders.isPresent()) {
            javaless_wonders = optional_javaless_wonders.get();
        } else {
            throw new RuntimeException("Very big baj nem vagyunk benne a játékban xd");
        }

        Optional<Planet> optional_base_planet = game.getWorld().getPlanets().stream()
                .filter(planet -> planet.getPlayer() == javaless_wonders.getId())
                .findFirst();

        if (optional_base_planet.isPresent()) {
            base_planet = optional_base_planet.get();
        } else {
            throw new RuntimeException("Very big baj nincs bolygónk xd");
        }

        for (Planet p : game.getWorld().getPlanets()) {
            float distance = (float) Math.sqrt(Math.pow(p.getX() - base_planet.getX(), 2) + Math.pow(p.getY() - base_planet.getY(), 2));
            planetMap.put(distance, p);
        }

        for (int i = 0; i < game.getSettings().getMaxConcurrentActions() - 1; i++) {
            Bot.SpaceMission.sendSpaceMission(base_planet.getId(), planetMap.get(planetMap.firstKey()).getId());
            planetMap.remove(planetMap.firstKey());
        }

        //Bot.MBH.sendMBH(base_planet.getId(), base_planet.getId());
    }

    public static void onGravityWaveCrossingActionEffect(GravityWaveCrossing actionEffect) {
        UILogger.log_string("Gravity Wave Crossing Action Effect happened :)");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:" + actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Planet (id): " + actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: " + actionEffect.getTime());
        UILogger.log_string("Cause: " + actionEffect.getCause());
        UILogger.log_string("source (id): " + actionEffect.getSourceId());
        UILogger.log_string("direction: " + Math.toDegrees( actionEffect.getDirection() ));
        UILogger.log_string(".............................................");
        EnemyDataAnalysis.analyzeData(actionEffect);
        Planet p = EnemyDataAnalysis.GetEnemyPlanet();
        if (p != null) {
            Bot.MBH.sendMBH(Planets.getPlanets_owned().get(0).getId(), p.getId());
        }
    }

    public static void onActionEffect(ActionEffect actionEffect) {
        UILogger.log_string("Regular Action Effect happened :)");
        UILogger.log_string("Type: ");
        UILogger.log_actionEffectType_arraylist(actionEffect.getEffectChain());
        UILogger.log_string("Player:" + actionEffect.getInflictingPlayer());
        UILogger.log_string("Affected Planet (id): " + actionEffect.getAffectedMapObjectId());
        UILogger.log_string("Time at: " + actionEffect.getTime());
        UILogger.log_string(".............................................");
    }

    public static Player getCurrentPlayer() {
        return javaless_wonders;
    }
}
