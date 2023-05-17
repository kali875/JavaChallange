package Bot;

import GameData.Actions;
import GameData.OnGoingMBHShots;
import GameData.Planets;
import Utils.UILogger;
import WebSocket.WebSocketCommunication;
import challenge.game.event.action.ShootMBHAction;
import challenge.game.model.Planet;
import org.glassfish.grizzly.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MBH
{
    public MBH()
    {

    }

    public List<ShootMBHAction> shootOnBestPossibleTarget(boolean isLateGamePhase) {
        List<ShootMBHAction> actions = new ArrayList<>();

        Planet p = EnemyDataAnalysis.GetEnemyPlanet();
        if (p != null) {
            if (!isLateGamePhase || Controll.game.getSettings().getMaxConcurrentActions() <= 3)
                actions.add(sendMBH(Planets.findClosestOwnedPlanetToTarget(p).getId(), p.getId()));
            if (Actions.getRemainingActionCount() < 2) throw new RuntimeException("Not enough action point to shoot on possible enemy planet");
            for (int i = 0; i < 2; i++)
                actions.add(sendMBH(Planets.findClosestOwnedPlanetToTarget(p).getId(), p.getId()));
            return actions;
        }

        if (isLateGamePhase) {
            Pair<Double, Pair<Planet, Planet>> closestPlanet = Planets.findClosestPlanets(false);
            // We may want to shoot again on a planet that is already shot on (OnGoingMBHShots)
            if (closestPlanet == null) throw new RuntimeException("There's no planet left");
            OnGoingMBHShots.possibleTarget(closestPlanet.getSecond().getSecond());
            actions.add(sendMBH(closestPlanet.getSecond().getFirst().getId(), closestPlanet.getSecond().getSecond().getId()));
            OnGoingMBHShots.onShot(closestPlanet.getSecond().getSecond());
        } else {
            Pair<Double, Pair<Planet, Planet>> closestUnhabitablePlanet = Planets.findClosestUnhabitablePlanet();
            if (closestUnhabitablePlanet != null) throw new RuntimeException("There's no unhabitable planet left to shot on...");
            OnGoingMBHShots.possibleTarget(closestUnhabitablePlanet.getSecond().getSecond());
            actions.add(sendMBH(closestUnhabitablePlanet.getSecond().getFirst().getId(), closestUnhabitablePlanet.getSecond().getSecond().getId()));
            OnGoingMBHShots.onShot(closestUnhabitablePlanet.getSecond().getSecond());
        }

        return actions;
    }

    public ShootMBHAction sendMBH(int start_planet_id, int target_planet_id) {
        Random random = new Random();
        int randomNumber = random.nextInt(89999) + 10000;
        ShootMBHAction action = new ShootMBHAction();
        action.setTargetId(target_planet_id);
        action.setOriginId(start_planet_id);
        action.setRefId(randomNumber);
        WebSocketCommunication.sendGameAction(action);
        return action;
    }
}
