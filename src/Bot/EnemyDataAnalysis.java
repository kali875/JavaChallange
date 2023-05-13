package Bot;

import GameData.JavalessWonders;
import GameData.Planets;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.model.GravityWaveCause;
import challenge.game.model.Planet;
import challenge.game.model.Player;
import org.glassfish.grizzly.utils.Pair;

import java.util.*;
import java.lang.Math;
import java.util.Map;

import Bot.EnemyPlanets;

public class EnemyDataAnalysis
{
    static EnemyPlanets enemyPlanets = new EnemyPlanets();
    static int frequencyLimit= 2;
    static double StepLength = 1.0;
    //GravityWaveCrossing
    private static double RadianConverter(int degree)
    {
        return Math.toRadians( (360/100)*degree);
    }
    public static void analyzeData(GravityWaveCrossing gravityWaveCrossing)
    {

        /**
         * Azon bolygó megkeresése, amely a Gravitációs hullám kiváltó oka volt
         * 0 id statement
         */
        if (gravityWaveCrossing.getSourceId() != 0) {

            Optional<Planet> result = Controll.game.getWorld().getPlanets().stream()
                    .filter(planet -> planet.getId() == gravityWaveCrossing.getSourceId())
                    .findFirst();
            Planet planet = null;
            if (result.isPresent()) planet = result.get();
            else return;
            /**
             * Ha a bolygó elpusztult (MBH robbanás)
             * Ha űrmisszió történt (Általunk küldött misszió esetén ez azt jelenti, hogy sikertelen volt a missziónk)
             * Ha féreglyuk épült
             * Ha a hullám passzivitás miatt érkezett
             * Feldolgozás:
             * Az irány radiánban érkezik, a szórás mértéke százalékos, ez a fok kivonandó illetve hozzáadandó az irányhoz
             * Így kapunk egy relatív "körszeletet", amelyet tartalmazó bolgyók egyike volt a hullámot kiváltó bolygó
             * Majd a lehetséges bolygók felkerülnek az ellenséges bolygók listájára
             */

            if (gravityWaveCrossing.getCause() == GravityWaveCause.PASSIVITY) {
                getPossiblePlanets(gravityWaveCrossing, planet, Controll.game.getSettings().getPassivityFleshPrecision());
            } else {
                getPossiblePlanets(gravityWaveCrossing, planet, Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            }

            if (gravityWaveCrossing.getCause() == GravityWaveCause.EXPLOSION)
            {
                EnemyPlanets.removePlanet(planet);
            }

        } else {
            // itt le kéne kezelni a space mission-öket, idk
        }
    }

    private static void getPossiblePlanets(GravityWaveCrossing gravityWaveCrossing, Planet affected_planet, int precision) {
        double StartDegree  =    gravityWaveCrossing.getDirection() - RadianConverter(precision);
        double EndDegree    =    gravityWaveCrossing.getDirection() + RadianConverter(precision);
        List<int[]> cells = findPossibleCells(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),affected_planet,EndDegree,StartDegree);
        SelectEnemyPlanets(cells);
    }

    public static List<int[]> findPossibleCells(long width, long height, Planet planet, double startDegree, double endDegree)
    {
        List<int[]> possibleCells = new ArrayList<>();
        /** X és Y komponensek kiszámítása a kezdő és végpont irányszögek alapján
         * Position: (x,y) pair
         */
        Pair<Double, Double> startPosition = new Pair<>();
        startPosition.setFirst(Math.cos(startDegree));
        startPosition.setSecond(Math.sin(startDegree));

        Pair<Double, Double> endPosition = new Pair<>();
        endPosition.setFirst(Math.cos(endDegree));
        endPosition.setSecond(Math.sin(endDegree));

        // Aktuális pozíció inicializálása
        double current_row = planet.getX();
        double current_column = planet.getY();

        // Lépkedés a rácsban
        while (current_row >= 0 && current_row < width && current_column >= 0 && current_column < height)
        {
            // Aktuális cella hozzáadása a listához
            possibleCells.add(new int[]{(int) current_row, (int) current_column});

            // Lépés az x és y komponensekkel
            current_row += startPosition.getSecond() * StepLength;
            current_column += startPosition.getFirst() * StepLength;
            if (checkForDirectionChange(endPosition.getFirst(), endPosition.getSecond(), current_row, current_column)) {
                break;
            }
        }
     return possibleCells;
    }
    public static boolean checkForDirectionChange(double endPosition_X, double endPosition_Y, double current_row, double current_column) {
        // Ellenőrzés, hogy az irány megváltozott-e
        double direction_X = Math.cos(Math.atan2(current_row, current_column));
        double direction_Y = Math.sin(Math.atan2(current_row, current_column));

        return Math.abs(direction_X - endPosition_X) < 1e-9 && Math.abs(direction_Y - endPosition_Y) < 1e-9;
    }

    public static void SelectEnemyPlanets(List<int[]> cells)
    {
        for (Planet planet: Planets.getPlanets())
        {
            for (int[] cel :cells)
            {
                if(planet.getY() == cel[1] && planet.getX() == cel[0] && !planet.isDestroyed() && planet.getPlayer() != JavalessWonders.getCurrentPlayer().getId() )
                {
                    if (Planets.getPlanetByID(planet.getId()) != null)
                        EnemyPlanets.putEnemyPlanet(planet);
                }
            }
        }
    }
    public static Planet GetEnemyPlanet()
    {
        if (EnemyPlanets.isEmpty()) return null;
        if (EnemyPlanets.getTheHighestKey() < frequencyLimit) return null;
        Planet p = EnemyPlanets.getHighestValuedEnemyPlanet();
        EnemyPlanets.removePlanet(p);
        return p;
    }
}
