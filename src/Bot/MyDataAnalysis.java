package Bot;

import GameData.JavalessWonders;
import GameData.Planets;
import challenge.game.event.GameEvent;
import challenge.game.event.action.GameAction;
import challenge.game.event.action.ShootMBHAction;
import challenge.game.event.action.SpaceMissionAction;
import challenge.game.event.actioneffect.ActionEffect;
import challenge.game.event.actioneffect.ActionEffectType;
import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.event.actioneffect.WormHoleBuiltEffect;
import challenge.game.model.GravityWaveCause;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class MyDataAnalysis
{
    static List<Planet> temp = new ArrayList<>();
    static int frequencyLimit= 5;
    static double stepLength = 1.0;
    //GravityWaveCrossing
    static  int radius= 10;
    static int StepRange = 10;
    public static void setFrequencyLimit(int frequencyLimit) {
        EnemyDataAnalysis.frequencyLimit = frequencyLimit;
    }

    /*public static List<Planet> FindPlanet(ActionEffect actionEffect)
    {
        int idx = 0;
        List<Planet> temp = new ArrayList<>();

        if (actionEffect instanceof GravityWaveCrossing gw)
        {
            if( gw.getCause() == GravityWaveCause.EXPLOSION)
            {

                for (GameAction value : Controll.Commands)
                {
                    if(value instanceof ShootMBHAction)
                    {
                        if(gw.getSourceId() == value.getTargetId() && gw.getAffectedMapObjectId() == ((ShootMBHAction) value).getOriginId())
                        {
                            //System.out.println("Valszeg a mi lővésünkre válasz: " + gw.getSourceId() + " " + ((ShootMBHAction) value).getOriginId());
                            return addPlanetsToList(temp, value, idx);
                        }
                    }
                    idx++;
                }
            }
        }
        idx=0;
        for (GameAction value : Controll.Commands)
        {
            if(value.getTargetId() == actionEffect.getAffectedMapObjectId())
                return addPlanetsToList(temp, value, idx);
            idx++;
        }
        return null;
    }*/
    public static void analData(ActionEffect actionEffect)
    {
        Planet planet = Planets.getPlanetByID(actionEffect.getAffectedMapObjectId());
        if (planet == null) return;
        findPointsInCircle(planet, radius, Planets.getPlanets_owned());
        /**
         * Azon saját bolygónk mérlegelése az kimenő adatok alapján
         */
        /*temp = FindPlanet(actionEffect);

        if (temp == null) return;
        if(temp.get(0) != null && temp.get(1) != null)
        {
            Planet TargetPlanet = temp.get(0);
            Planet SrcPlanet = temp.get(1);
            temp = null;
            //double rad = DefinesDirection(SrcPlanet,TargetPlanet); /////////////

            for ( ActionEffectType type : actionEffect.getEffectChain())
            {
                switch (type) {
                    case INACTIVITY_FLARE_START ->
                            getPossiblePlanets(SrcPlanet, Controll.game.getSettings().getPassivityFleshPrecision());
                    case SPACE_MISSION_GRAWITY_WAVE_START, SPACE_MISSION_DESTROYED, WORM_HOLE_BUILT_GRAWITY_WAVE_START ->
                            getPossiblePlanets(SrcPlanet, Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
                }
            }
            // System.out.println("Gyüjtött palnéták száma: "+ DefPlanets.size());
            //System.out.println(Planets.getPlanets_owned().size());
        }
        else
        {
            //System.out.println("null src, targetid ");
        }*/

    }
/*    private static void getPossiblePlanets(Planet src_planet, int precision)
    {
        // System.out.println("srcplanet:"+ src_planet.getY()+"-"+src_planet.getX());
        List<Planet> PotentialEndangeredMyPlanet = findPointsInCircle(src_planet,radius,Planets.getPlanets_owned());


        List<int[]> cells = new ArrayList<>();
        for (Planet planets : PotentialEndangeredMyPlanet)
            cells.add(new int[]{(int)planets.getX(),(int)planets.getY()});
        // SelectEndangeredPlanets(cells);
        cells.clear();
    }*/
/*    private static double RadianConverter(int degree)
    {
        return Math.toRadians( (360.0/100)*degree);
    }*/
/*    public static List<int[]> findPossibleCells(long width, long height, Planet planet, double startDegree, double endDegree)
    {
        List<int[]> possibleCells = new ArrayList<>();
        *//** X és Y komponensek kiszámítása a kezdő és végpont irányszögek alapján
         * Position: (x,y) pair
         *//*
        Pair<Double, Double> startPosition = new Pair<>();
        startPosition.setFirst(Math.cos(startDegree));
        startPosition.setSecond(Math.sin(startDegree));

        Pair<Double, Double> endPosition = new Pair<>();
        endPosition.setFirst(Math.cos(endDegree));
        endPosition.setSecond(Math.sin(endDegree));

        // Aktuális pozíció inicializálása
        double current_row = planet.getX();
        double current_column = planet.getY();
        int count = 0;
        // Lépkedés a rácsban
        while (current_row >= 0 && current_row < width && current_column >= 0 && current_column < height)
        {
            if(count == StepRange)
            {
                break;
            }
            // Aktuális cella hozzáadása a listához
            possibleCells.add(new int[]{(int) current_row, (int) current_column});

            // Lépés az x és y komponensekkel
            current_row += startPosition.getSecond() * stepLength;
            current_column += startPosition.getFirst() * stepLength;
            if (checkForDirectionChange(endPosition.getFirst(), endPosition.getSecond(), current_row, current_column))
            {
                break;
            }
            count ++;
        }
        return possibleCells;
    }*/

/*    public static void SelectEndangeredPlanets(List<int[]> cells)
    {

        for (Planet planet: Planets.getPlanets())
        {
            for (int[] cel :cells)
            {
                if(planet.getY() == cel[1] && planet.getX() == cel[0] && !planet.isDestroyed() && planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId() )
                {
                    DefensePlanets.putEndangeredPlanet(planet);
                    // System.out.println(planet.getId());
                }
            }
        }
    }*/
/*    public static double DefinesDirection(Planet SrcPlanet, Planet TargetPlanet)
    {
        // Irányszög számítása a B pontból az A pontba
        double iranyszogBA = Math.atan2(TargetPlanet.getY() - SrcPlanet.getY(), (TargetPlanet.getX() * -1) - (SrcPlanet.getX() * -1));

        // Radianból fokba alakítás
        double iranyszogBAFok = Math.toDegrees(iranyszogBA);

        // Negatív szögnegyedet pozitívvá alakítás
        double iranyszogNegyed = iranyszogBAFok;
        if (iranyszogNegyed < 0)
        {
            iranyszogNegyed += 360;
        }

        return Math.toRadians(iranyszogNegyed);
    }*/

    static void findPointsInCircle(Planet srcPlanet, int radius, List<Planet> points) {

        // Pont távolsága a középponttól
        for (Planet point : points)
            if (srcPlanet.distance(point) <= radius)
                DefensePlanets.putEndangeredPlanet(point);
    }

/*    private static List<Planet> addPlanetsToList(List<Planet> temp, GameAction action, int idx)
    {
        temp.add(Controll.game.getWorld().getPlanets().stream().filter(Planet -> Planet.getId() == action.getTargetId()).findFirst().orElse(null));
        temp.add(Planets.getPlanets_owned().stream()
                .filter(
                        planet -> {
                            if (action instanceof SpaceMissionAction) return planet.getId() == ((SpaceMissionAction) action).getOriginId();
                            return planet.getId() == ((ShootMBHAction) action).getOriginId();
                        }
                ).findFirst().orElse(null));
        Controll.Commands.remove(idx);
        return temp;
    }*/
/*    public static boolean checkForDirectionChange(double endPosition_X, double endPosition_Y, double current_row, double current_column) {
        // Ellenőrzés, hogy az irány megváltozott-e
        double direction_X = Math.cos(Math.atan2(current_row, current_column));
        double direction_Y = Math.sin(Math.atan2(current_row, current_column));

        return Math.abs(direction_X - endPosition_X) < 1e-9 && Math.abs(direction_Y - endPosition_Y) < 1e-9;
    }*/

    public static Planet getDefPlanet()
    {
        if (DefensePlanets.defPlanets.isEmpty()) return null;
        //DefensePlanets.logContainer();
        //System.out.println("DefensePlanets.getTheHighestKey(): " + DefensePlanets.getTheHighestKey());
        if(DefensePlanets.getTheHighestKey() > frequencyLimit)
        {
            return DefensePlanets.getHighestValuedEndangeredPlanet();
        }
        return null;
    }

}