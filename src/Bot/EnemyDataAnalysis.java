package Bot;

import challenge.game.event.actioneffect.GravityWaveCrossing;
import challenge.game.model.GravityWaveCause;
import challenge.game.model.Planet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;
import java.util.Map;

public class EnemyDataAnalysis
{
    static List<Planet> EnemyPlanets = new ArrayList<Planet>();
    // Maybe Dynamic
    static int frequencyLimit= 2;
    static double StepLength = 1.0;
    //GravityWaveCrossing
    private static double RadianConverter(int degree)
    {
        return Math.toRadians( (360/100)*degree);
    }
    public static void DataAnalys(GravityWaveCrossing GravityWaveCrossing)
    {
        Planet planet= null;
        for (Planet it : Controll.game.getWorld().getPlanets()) {
            if (it.getId() == GravityWaveCrossing.getAffectedMapObjectId())
            {
                planet = it;
            }
        }

        if(GravityWaveCrossing.getCause() == GravityWaveCause.EXPLOSION)
        {
            double StartDegree  =    GravityWaveCrossing.getDirection() - RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            double EndDegree    =    GravityWaveCrossing.getDirection() + RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            List<int[]> Data = gyujtsErintettCellakat(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),planet,EndDegree,StartDegree,StepLength);
            SelectPlanet(Data);

        }
        else if( GravityWaveCrossing.getCause() == GravityWaveCause.SPACE_MISSION)
        {
            double StartDegree  =    GravityWaveCrossing.getDirection() - RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            double EndDegree    =    GravityWaveCrossing.getDirection() + RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            List<int[]> Data = gyujtsErintettCellakat(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),planet,EndDegree,StartDegree,StepLength);
            SelectPlanet(Data);
        }
        else if( GravityWaveCrossing.getCause() == GravityWaveCause.WORMHOLE)
        {
            double StartDegree  =    GravityWaveCrossing.getDirection() - RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            double EndDegree    =    GravityWaveCrossing.getDirection() + RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            List<int[]> Data = gyujtsErintettCellakat(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),planet,EndDegree,StartDegree,StepLength);
            SelectPlanet(Data);
        }
        else if(GravityWaveCrossing.getCause() == GravityWaveCause.PASSIVITY)
        {
            double StartDegree  =    GravityWaveCrossing.getDirection() - RadianConverter(Controll.game.getSettings().getPassivityFleshPrecision());
            double EndDegree    =    GravityWaveCrossing.getDirection() + RadianConverter(Controll.game.getSettings().getGravityWaveSourceLocationPrecision());
            List<int[]> Data = gyujtsErintettCellakat(Controll.game.getWorld().getWidth(),Controll.game.getWorld().getHeight(),planet,EndDegree,StartDegree,StepLength);
            SelectPlanet(Data);
        }
    }
    public static List<int[]> gyujtsErintettCellakat(long width, long height, Planet planet, double StartDegree, double EndDegree, double StepLength)
    {
        List<int[]> HitCell = new ArrayList<>();
        // X és Y komponensek kiszámítása a kezdő és végpont irányszögek alapján
        double kezdoXKomponens = Math.cos(StartDegree);
        double kezdoYKomponens = Math.sin(StartDegree);

        double vegXKomponens = Math.cos(EndDegree);
        double vegYKomponens = Math.sin(EndDegree);

        // Aktuális pozíció inicializálása
        double aktualisSor = planet.getX();
        double aktualisOszlop = planet.getY();

        // Lépkedés a rácsban
        while (aktualisSor >= 0 && aktualisSor < width && aktualisOszlop >= 0 && aktualisOszlop < height)
        {
            // Aktuális cella hozzáadása a listához
            HitCell.add(new int[]{(int) aktualisSor, (int) aktualisOszlop});

            // Lépés az x és y komponensekkel
            aktualisSor += kezdoYKomponens * StepLength;
            aktualisOszlop += kezdoXKomponens * StepLength;
            if (iranyValtozas(kezdoXKomponens, kezdoYKomponens, vegXKomponens, vegYKomponens, aktualisSor, aktualisOszlop)) {
                break;
            }
        }
     return HitCell;
    }
    public static boolean iranyValtozas(double kezdoXKomponens, double kezdoYKomponens, double vegXKomponens, double vegYKomponens, double aktualisSor, double aktualisOszlop) {
        // Ellenőrzés, hogy az irány megváltozott-e
        double aktualisIranyXKomponens = Math.cos(Math.atan2(aktualisSor, aktualisOszlop));
        double aktualisIranyYKomponens = Math.sin(Math.atan2(aktualisSor, aktualisOszlop));

        return Math.abs(aktualisIranyXKomponens - vegXKomponens) < 1e-9 && Math.abs(aktualisIranyYKomponens - vegYKomponens) < 1e-9;
    }

    public static void SelectPlanet(List<int[]> cells)
    {
        List<Planet> temp =  new ArrayList<Planet>();
        for (Planet planet: Controll.game.getWorld().getPlanets())
        {
            for (int[] cel :cells)
            {
                if(planet.getY() == cel[1] && planet.getX() == cel[0])
                {
                    EnemyPlanets.add(planet);
                }
            }
        }
    }
    public static Planet CheckMaybeEnemy()
    {
        Planet Palnet = null;
        // Csoportok tárolására Map
        Map<Integer, List<Planet>> groups = new HashMap<>();

        // Objektumok csoportokba rendezése az azonosító szerint
        for (Planet obj : EnemyPlanets)
        {
            int id = obj.getId();

            // Ellenőrizd, hogy van-e már ilyen azonosítóval rendelkező csoport
            if (!groups.containsKey(id)) {
                // Ha nincs, hozz létre új csoportot
                groups.put(id, new ArrayList<>());
            }

            // Fűzd hozzá az objektumot a csoport elemeihez
            groups.get(id).add(obj);
        }

        // Csoportok elemek számának megszámlálása
        for (Map.Entry<Integer, List<Planet>> entry : groups.entrySet())
        {
            int id = entry.getKey();
            List<Planet> group = entry.getValue();
            int count = group.size();
            if(count >frequencyLimit)
            {
                return group.get(0);
            }
        }

        return new Planet();
    }
}
