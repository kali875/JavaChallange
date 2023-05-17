package DSS;

import Bot.Controll;
import GameData.Planets;
import challenge.game.model.Game;
import challenge.game.model.Planet;
import challenge.game.model.World;
import challenge.game.settings.GameSettings;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class InitialDataAnalysis {
    public static Game gameData;
    public static GameSettings gameSettings;
    /* Base Related */
    public static int height;
    public static int width;
    public static int numOfActions;
    public static int unitTime;
    public static double unitSpeed;
    /* Wormhole Related */
    public static int maxWormHoles;
    public static int wormHoleBuildTime;
    /* Passivity Related */
    public static int passivityThreshHoldTime;
    /* Shield Related */
    public static int shieldBuildTime;
    public static int shieldDurationTime;
    public static double shieldEfficiency;
    /* World Related */
    public static  World world;
    public static int numOfPlanets;
    public static int numOfWormHoles;
    public static double planetDensity;

    public InitialDataAnalysis(Game _game) {
        if (_game != null) {
            this.gameData = _game;
            this.gameSettings = gameData.getSettings();
            /* Base Related */
            this.height = gameSettings.getHeight();
            this.width = gameSettings.getWidth();
            this.numOfActions = gameSettings.getMaxConcurrentActions();
            this.unitTime = gameSettings.getTimeOfOneLightYear();
            if (unitTime != 0)
                this.unitSpeed = (double) 1 / unitTime;
            /* Wormhole Related */
            this.maxWormHoles = gameSettings.getMaxWormHolesPerPlayer();
            this.wormHoleBuildTime = gameSettings.getTimeOfBuildWormHole();
            /* Passivity Related */
            this.passivityThreshHoldTime = gameSettings.getPassivityTimeTreshold();
            /* Shield Related */
            this.shieldBuildTime = gameSettings.getTimeToBuildShild();
            this.shieldDurationTime = gameSettings.getShildDuration();
            this.shieldEfficiency = (double) shieldDurationTime / shieldBuildTime; //Átgondolandó
            /* World Related */
            this.world = gameData.getWorld();
            this.numOfPlanets = world.getPlanets().size();
            if (world.getWormHoles() == null) this.numOfWormHoles = 0;
            else this.numOfWormHoles = world.getWormHoles().size();
            this.planetDensity = (double) numOfPlanets / height * width; //Lehet nem kell
        }
    }

    private Planet maxDistance(Planet from, List<Planet> source) {
        Planet max = from;
        for (Planet p : source) {
            if (from.distance(p) > from.distance(max)) max = p;
        }
        return max;
    }

    private Planet minDistance(Planet from, List<Planet> source) {
        Planet min = source.get(0);
        for (Planet p : source) {
            if (from.distance(p) < from.distance(min)) min = p;
        }
        return min;
    }

    public static double calculateAngle(Planet p1, Planet p2) {
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        double angleRadians = Math.atan2(deltaY, deltaX);
        double angleDegrees = Math.toDegrees(angleRadians);

        // Az atan2 függvény -180 és +180 fok közötti értéket ad vissza,
        // ezért ha negatív az irányszög, hozzáadunk 360 fokot a pozitív értékhez
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        return angleDegrees;
    }

    public boolean isAngleInRange(double angle, double startAngle, double endAngle) {
        // Normalizáljuk az irányszögeket 0 és 360 fok közé
        angle = normalizeAngle(angle);
        startAngle = normalizeAngle(startAngle);
        endAngle = normalizeAngle(endAngle);

        // Ellenőrizzük, hogy az irányszög a szögtartományba esik-e
        if (startAngle <= endAngle) {
            return angle >= startAngle && angle <= endAngle;
        } else {
            return angle >= startAngle || angle <= endAngle;
        }
    }

    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public Planet suggestedPoint(List<Planet> owned, List<Planet> source, int angleThreshold) {
        Planet suggested = null;
        if (owned.size() > 0) {
            int middleX = 0,
                    middleY = 0;
            for (Planet p : owned) {
                middleX += p.getX();
                middleY += p.getY();
            }
            middleX /= owned.size();
            middleY /= owned.size();
            Planet middle = new Planet();
            middle.setX(middleX);
            middle.setY(middleY);
            double aveAngle = 0.0;
            for (Planet p : owned) {
                aveAngle += calculateAngle(middle, p);
            }
            aveAngle /= owned.size();
            if (aveAngle == 0.0) {
                suggested = maxDistance(middle, source);
            } else {
                suggested = middle;
                for (Planet p : source) {
                    if (middle.distance(p) > middle.distance(suggested)) {
                        if (isAngleInRange(calculateAngle(middle, p), aveAngle - angleThreshold, aveAngle + angleThreshold))
                            suggested = p;
                        else if (isAngleInRange(calculateAngle(middle, p), aveAngle + 180 - angleThreshold, aveAngle + 180 + angleThreshold))
                            suggested = p;
                    }
                }
            }
        }
        return suggested;
    }

    public List<Map.Entry<Planet, List<Planet>>> PointClustering(double radius, List<Planet> points) {
        int w = this.width;
        int h = this.height;

        // Csoportosítás sugárok mentén
        Map<Planet, List<Planet>> clusters = new HashMap<>();
        for (Planet p : points) {
            for (Planet q : points) {
                double distanceSquared = Math.pow(q.getX() - p.getX(), 2) + Math.pow(q.getY() - p.getY(), 2);
                double radiusSquared = Math.pow(radius, 2);

                if (distanceSquared <= radiusSquared && p != q) {
                    if (!clusters.containsKey(p)) {
                        clusters.put(p, new ArrayList<>());
                    }
                    clusters.get(p).add(q);
                }
            }
        }

        // Csoportok rendezése pontok száma szerint
        List<Map.Entry<Planet, List<Planet>>> sortedClusters = new ArrayList<>(clusters.entrySet());
        sortedClusters.sort((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()));
        return sortedClusters;
    }

    public Map.Entry<Planet, List<Planet>> getNearestCluster(Planet targetPoint, List<Map.Entry<Planet, List<Planet>>> clusters) {
        double minDistance = Double.MAX_VALUE;
        Map.Entry<Planet, List<Planet>> nearestCluster = null;

        for (Map.Entry<Planet, List<Planet>> cluster : clusters) {
            Planet center = cluster.getKey();
            double distance = targetPoint.distance(center);
            if ((nearestCluster == null || nearestCluster.getValue().size() / minDistance < cluster.getValue().size() / distance)
                    && distance < minDistance
                    && cluster.getValue().size() > (clusters.get(0).getValue().size() / 2)) {
                minDistance = distance;
                nearestCluster = cluster;
            }
        }

        return nearestCluster;
    }

    public List<Map.Entry<Planet, List<Planet>>> mergedClusters(double radius, List<Map.Entry<Planet, List<Planet>>> source) {
        Map<Planet, List<Planet>> clusters = new HashMap<>();
        for (int i = 0; i < source.size(); ) {
            List<Planet> temp = new ArrayList<>();
            for (int j = 0; j < source.size(); j++) {
                if (source.get(i).getKey().distance(source.get(j).getKey()) <= radius && source.get(i).getKey() != source.get(j).getKey()) {
                    for (Planet p : source.get(i).getValue()) {
                        if (!temp.contains(p)) temp.add(p);
                    }
                    for (Planet p : source.get(j).getValue()) {
                        if (!temp.contains(p)) temp.add(p);
                    }
                    source.remove(j);
                }
            }
            clusters.put(source.get(i).getKey(), temp);
            source.remove(i);
        }
        List<Map.Entry<Planet, List<Planet>>> result = new ArrayList<>(clusters.entrySet());
        result.sort((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()));
//        // Legnagyobb csoportok kiírása
//        int k = 10; // kiírandó csoportok száma
//        for (int i = 0; i < Math.min(k, result.size()); i++) {
//            Map.Entry<Planet, List<Planet>> entry = result.get(i);
//            Planet center = entry.getKey();
//            List<Planet> pointsInCluster = entry.getValue();
//            System.out.println("Center: " + center.toString() + ", Size: " + pointsInCluster.size() + ", Points: " + pointsInCluster.toString());
//        }
        return result;
    }

    public List<Map.Entry<Planet, List<Planet>>> getClusters() {
        Planet start = Planets.basePlanet;
        //Pont javaslatok jobb lefedettséghez
        List<Planet> suggestedAnchors = new ArrayList<>();
        suggestedAnchors.add(start);
        for (int i = 0; i < this.maxWormHoles; i++) {
            suggestedAnchors.add(suggestedPoint(suggestedAnchors, Planets.getPlanets(), 30));
        }

        double unitLength = Math.sqrt(((double) (this.width * this.height) / this.numOfPlanets));

        //Csomópontok meghatározása és optimalizálása
        List<Map.Entry<Planet, List<Planet>>> mergedClusters =
                mergedClusters(unitLength, PointClustering(unitLength, Planets.getPlanets()));

        List<Map.Entry<Planet, List<Planet>>> bestClusters = new ArrayList<>();

        for(Planet p : suggestedAnchors){
            bestClusters.add(getNearestCluster(p, mergedClusters));
        }

        bestClusters.sort((o1, o2) -> Double.compare(start.distance(o2.getKey()), start.distance(o1.getKey())));

//        StdDraw.setCanvasSize(this.width, this.height);
//        StdDraw.setXscale(0, this.width);
//        StdDraw.setYscale(0, this.height);
//        StdDraw.setPenColor(Color.WHITE);
//        StdDraw.filledRectangle(this.width/2.0, this.height/2.0, this.width/2.0, this.height/2.0);
//        StdDraw.setPenRadius(0.001);
//        StdDraw.setPenColor(Color.BLACK);
//        for (Planet p : Planets.getPlanets()) {
//            StdDraw.point(p.getX(), p.getY());
//        }
//
//        List<Color> clr = new ArrayList<>();
//        clr.add(Color.GREEN);
//        clr.add(Color.YELLOW);
//        clr.add(Color.RED);
//        clr.add(Color.ORANGE);
//        clr.add(Color.BLUE);
//
//        int i = 0;
//        for (Map.Entry<Planet, List<Planet>> p : bestClusters) {
//            if(i == 5) break;
//            StdDraw.setPenColor(clr.get(i));
//            StdDraw.point(p.getKey().getX(), p.getKey().getY());
//            for(Planet q : p.getValue()){
//                StdDraw.point(q.getX(), q.getY());
//            }
//            i++;
//        }
//
//        StdDraw.setPenColor(Color.MAGENTA);
//        for (Planet p : suggestedAnchors) {
//            StdDraw.point(p.getX(), p.getY());
//        }
//        StdDraw.setPenColor(Color.CYAN);
//        StdDraw.point(start.getX(), start.getY());
//
//        StdDraw.save(System.currentTimeMillis() + ".jpg");

        return bestClusters;
    }
}