import Bot.Controll;
import GameData.OnGoingMBHShots;
import GameData.OnGoingSpaceMissions;
import GameData.Planets;
import challenge.game.model.Planet;
import challenge.game.model.WormHole;

import javax.swing.*;
import java.util.TimerTask;

public class MyTask extends TimerTask {
    private JTable GameWorld;

    public MyTask(JTable gameWorld) {
        this.GameWorld = gameWorld;
    }

    @Override
    public void run()
    {
        MyPlanets();
        UnhabitablePlanets();
        DestroyedPlanets();
        OnGoingShots();
        OnGoingMissions();
        WormHoleBuilt();
    }

    public void MyPlanets()
    {
        for (Planet planet: Planets.getPlanets_owned())
        {
            GameWorld.getModel().setValueAt("O",(int)planet.getX(),(int)planet.getY());
        }
    }
    public void UnhabitablePlanets()
    {
        for (Planet planet : Planets.unhabitable_planets)
        {
            GameWorld.getModel().setValueAt("u",(int)planet.getX(),(int)planet.getY());
        }
    }

    public void DestroyedPlanets()
    {
        for (Planet planet : Planets.destroyed_planets)
        {
            GameWorld.getModel().setValueAt("x",(int)planet.getX(),(int)planet.getY());
        }
    }

    public void OnGoingShots()
    {
        for (Planet planet : OnGoingMBHShots.shots)
        {
            GameWorld.getModel().setValueAt("MB",(int)planet.getX(),(int)planet.getY());
        }
    }

    public void OnGoingMissions()
    {
        for (Planet planet : OnGoingSpaceMissions.getSpaceMissionTargets())
        {
            GameWorld.getModel().setValueAt("SM",(int)planet.getX(),(int)planet.getY());
        }
    }

    public void WormHoleBuilt()
    {
        for (WormHole wh : Controll.wormHoles)
        {
            GameWorld.getModel().setValueAt("WHA",(int)wh.getX(),(int)wh.getY());
            GameWorld.getModel().setValueAt("WHB",(int)wh.getXb(),(int)wh.getYb());
        }
    }
}