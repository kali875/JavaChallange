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
    static int StepRange = 20;
    public static void setFrequencyLimit(int frequencyLimit) {
        EnemyDataAnalysis.frequencyLimit = frequencyLimit;
    }

    public static List<Planet> FindPlanet(GameEvent gameEvent)
    {
        int idx = 0;
        List<Planet> temp = new ArrayList<>();

        if (gameEvent.getActionEffect() instanceof GravityWaveCrossing gw)
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
            if(value.getTargetId() == gameEvent.getActionEffect().getAffectedMapObjectId())
                return addPlanetsToList(temp, value, idx);
            idx++;
        }
        return null;
    }
    public static void analData(GameEvent gameEvent)
    {
        /**
         * Azon saját bolygónk mérlegelése az kimenő adatok alapján
         */
        temp = FindPlanet(gameEvent);

        if (temp == null) return;
        if(temp.get(0) != null && temp.get(1) != null)
        {
            Planet TargetPlanet = temp.get(0);
            Planet SrcPlanet = temp.get(1);
            temp = null;
            //double rad = DefinesDirection(SrcPlanet,TargetPlanet); /////////////

            for ( ActionEffectType type : gameEvent.getActionEffect().getEffectChain())
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
        }

    }
    private static void getPossiblePlanets(Planet src_planet, int precision)
    {

        CopyOnWriteArrayList<Planet> difflist = new CopyOnWriteArrayList<>(Planets.getPlanets());
        for (Planet planet: Planets.getPlanets_owned())
        {
            difflist.removeIf(p -> p.getId() == planet.getId());
        }
        for (Planet planet: Planets.unhabitable_planets)
        {
            difflist.removeIf(p -> p.getId() == planet.getId());
        }

        List<Planet> PotentialEnemyPlanet = findPointsInCircle(src_planet,radius,difflist);

        for (Planet planets :PotentialEnemyPlanet)
        {
            double rad = DefinesDirection(src_planet,planets);
            double StartDegree  =    rad - RadianConverter(precision);
            double EndDegree    =    rad + RadianConverter(precision);
            List<int[]> cells = findPossibleCells(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),planets,EndDegree,StartDegree);
            SelectEndangeredPlanets(cells);
        }
    }
    private static double RadianConverter(int degree)
    {
        return Math.toRadians( (360.0/100)*degree);
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
    }

    public static void SelectEndangeredPlanets(List<int[]> cells)
    {

        for (Planet planet: Planets.getPlanets())
        {

            for (int[] cel :cells)
            {
                if(planet.getY() == cel[1] && planet.getX() == cel[0] && !planet.isDestroyed() && planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId() )
                {
                    DefensePlanets.putEndangeredPlanet(planet);
                }
            }
        }
    }
    public static double DefinesDirection(Planet SrcPlanet, Planet TargetPlanet) {
        long horizontalDistance = TargetPlanet.getX() - SrcPlanet.getX();
        long verticalDistance = TargetPlanet.getY() - SrcPlanet.getY();

        return Math.atan2(verticalDistance, horizontalDistance);
    }

    static List<Planet> findPointsInCircle(Planet srcPlanet, int radius, List<Planet> points) {
        List<Planet> pointsInCircle = new ArrayList<>();

        for (Planet point : points) {
            // Pont távolsága a középponttól
            int distance = (int) Math.sqrt(Math.pow(point.getX() - srcPlanet.getX(), 2) + Math.pow(point.getY() - srcPlanet.getY(), 2));

            if (distance <= radius)
            {
                pointsInCircle.add(point);
            }
        }

        return pointsInCircle;
    }

    private static List<Planet> addPlanetsToList(List<Planet> temp, GameAction action, int idx)
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
    }
    public static boolean checkForDirectionChange(double endPosition_X, double endPosition_Y, double current_row, double current_column) {
        // Ellenőrzés, hogy az irány megváltozott-e
        double direction_X = Math.cos(Math.atan2(current_row, current_column));
        double direction_Y = Math.sin(Math.atan2(current_row, current_column));

        return Math.abs(direction_X - endPosition_X) < 1e-9 && Math.abs(direction_Y - endPosition_Y) < 1e-9;
    }

    public static Planet getDefPlanet()
    {
        if (DefensePlanets.defPlanets.isEmpty()) return null;
        DefensePlanets.logContainer();
        //System.out.println("DefensePlanets.getTheHighestKey(): " + DefensePlanets.getTheHighestKey());
        if(DefensePlanets.getTheHighestKey() > frequencyLimit)
        {
            return DefensePlanets.getHighestValuedEndangeredPlanet();
        }
        return null;
    }

}