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
import java.util.stream.Stream;

import static Bot.DefensePlanets.DefPlanets;

public class MyDataAnalysis
{
    static List<Planet> temp = new ArrayList<Planet>();
    static int frequencyLimit= 5;
    static double StepLength = 1.0;
    //GravityWaveCrossing
    private static double RadianConverter(int degree)
    {
        return Math.toRadians( (360/100)*degree);
    }
    public static double DefinesDirection(Planet SrcPlanet, Planet TargetPlanet) {
        long Horitontaldistance = TargetPlanet.getX() - SrcPlanet.getX();
        long VerticalDistance = TargetPlanet.getY() - SrcPlanet.getY();
        double rad = Math.atan2(VerticalDistance, Horitontaldistance);

        return rad;
    }
    public static List<Planet> FindPlanet(GameEvent GameEvent)
    {
        List<Planet> temp = new ArrayList<Planet>();
        Planet SourcePlanet = null;
        Planet TargetPlanet = null;

        for (GameAction value:Controll.Commands)
        {
            if(value.getTargetId() == GameEvent.getActionEffect().getAffectedMapObjectId())
            {
                if(value instanceof ShootMBHAction)
                {

                    ShootMBHAction shoot = (ShootMBHAction)value;
                    temp.add(Controll.game.getWorld().getPlanets().stream().filter(Planet -> Planet.getId() == shoot.getTargetId()).findFirst().orElse(null));
                    temp.add(Planets.getPlanets_owned().stream().filter(Planet -> Planet.getId() == ((ShootMBHAction) value).getOriginId()).findFirst().orElse(null));

                    return temp;
                }
                else if(value instanceof SpaceMissionAction)
                {
                    SpaceMissionAction mission = (SpaceMissionAction)value;
                    temp.add( Controll.game.getWorld().getPlanets().stream().filter(Planet -> Planet.getId() == mission.getTargetId()).findFirst().orElse(null));
                    temp.add( Planets.getPlanets_owned().stream().filter(Planet -> Planet.getId() == ((SpaceMissionAction) value).getOriginId()).findFirst().orElse(null));

                    return temp;
                }
            }
        }
        return null;
    }
    public static void analData(GameEvent GameEvent)
    {
        /**
         * Azon saját bolygónk mérlegelése az kimenő adatok alapján
         */
        temp = FindPlanet(GameEvent);

        if(temp != null)
        {
            Planet TargetPlanet = temp.get(0);
            Planet SrcPlanet = temp.get(1);
            double rad = DefinesDirection(SrcPlanet,TargetPlanet);

            for ( ActionEffectType type:GameEvent.getActionEffect().getEffectChain())
            {
                switch (type)
                {
                    case INACTIVITY_FLARE_START:
                        getPossiblePlanets(rad,TargetPlanet,Controll.game.getSettings().getPassivityFleshPrecision());
                        break;
                    case SPACE_MISSION_GRAWITY_WAVE_START:
                        getPossiblePlanets(rad,TargetPlanet,Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
                        break;
                    case SPACE_MISSION_DESTROYED:
                        getPossiblePlanets(rad,TargetPlanet,Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
                        break;
                    case WORM_HOLE_BUILT_GRAWITY_WAVE_START:
                        getPossiblePlanets(rad,TargetPlanet,Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
                        break;
                }
            }
            System.out.println("Gyüjtött palnéták száma: "+ DefPlanets.size());
            //System.out.println(Planets.getPlanets_owned().size());
        }
        else
        {
            System.out.println("null src, targetid ");
        }

    }

    private static void getPossiblePlanets(double rad, Planet affected_planet, int precision) {
        double StartDegree  =    rad - RadianConverter(precision);
        double EndDegree    =    rad + RadianConverter(precision);
        List<int[]> cells = findPossibleCells(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),affected_planet,EndDegree,StartDegree);
        SelectEndangeredPlanets(cells);
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

    public static void SelectEndangeredPlanets(List<int[]> cells)
    {
        for (Planet planet: Planets.getPlanets())
        {
            for (int[] cel :cells)
            {
                if(planet.getY() == cel[1] && planet.getX() == cel[0] && !planet.isDestroyed() && planet.getPlayer() == JavalessWonders.getCurrentPlayer().getId() )
                {
                    DefensePlanets.putDefPlanet(planet);
                }
            }
        }
    }
    public static Planet GetDefPlanet()
    {
        if (DefensePlanets.isEmpty()) return null;

        if(DefensePlanets.getTheHighestKey() > frequencyLimit)
        {
            return DefensePlanets.getHighestValuedDefPlanet();
        }
        return null;
    }
    
}
