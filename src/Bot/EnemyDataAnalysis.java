package Bot;

import DSS.InitialDataAnalysis;
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
    static int frequencyLimit = 5;
    static double StepLength = 1.0;

    private static double toStandardSystem(double degree) {
        return (360 - (degree - 90)) % 360;
    }
    //GravityWaveCrossing
    public static void setFrequencyLimit(int frequencyLimit) {
        EnemyDataAnalysis.frequencyLimit = frequencyLimit;
    }
    public static void analyzeData(GravityWaveCrossing gravityWaveCrossing)
    {

        /**
         * Azon bolygó megkeresése, amely a Gravitációs hullám kiváltó oka volt
         * 0 id statement
         */

        Optional<Planet> result = Controll.game.getWorld().getPlanets().stream()
                .filter(planet -> planet.getId() == gravityWaveCrossing.getAffectedMapObjectId())
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
        } else if (gravityWaveCrossing.getCause() == GravityWaveCause.EXPLOSION)
        {
            Optional<Planet> exploded_planet = Controll.game.getWorld().getPlanets().stream()
                    .filter(p -> p.getId() == gravityWaveCrossing.getSourceId())
                    .findFirst();
            Planet p;
            if (exploded_planet.isPresent()) {
                p = exploded_planet.get();
                EnemyPlanets.removePlanet(p);
            }
        } else {
            getPossiblePlanets(gravityWaveCrossing, planet, Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
        }

    }

    private static void getPossiblePlanets(GravityWaveCrossing gravityWaveCrossing, Planet affected_planet, int precision) {
        double StartDegree = toStandardSystem(Math.toDegrees(gravityWaveCrossing.getDirection()) - (360.0/100)*precision);
        double EndDegree = toStandardSystem(Math.toDegrees(gravityWaveCrossing.getDirection()) + (360.0/100)*precision);

        for(Planet p : InitialDataAnalysis.world.getPlanets()){
            double angle = InitialDataAnalysis.calculateAngle(affected_planet, p);
            if (angle >= StartDegree && angle <= EndDegree){
                if(!Planets.ownedPlanetsContains(p)){
                    EnemyPlanets.putEnemyPlanet(p);
                }
            }
        }
    }

    public static Planet GetEnemyPlanet()
    {
        if (EnemyPlanets.isEmpty()) return null;
        EnemyPlanets.syncOwnedPlanets();
        if (EnemyPlanets.isEmpty()) return null;
        //EnemyPlanets.logContainer();
        if (EnemyPlanets.getTheHighestKey() < frequencyLimit) return null;
        Planet p = EnemyPlanets.getHighestValuedEnemyPlanet();
        EnemyPlanets.removePlanet(p);
        return p;
    }

    public static boolean isThereEnemyWithHighChance() {
        if (EnemyPlanets.isEmpty()) return false;
        if (EnemyPlanets.getTheHighestKey() >= frequencyLimit * 1.5) return true;
        return false;
    }
}
