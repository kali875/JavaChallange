import java.util.TimerTask;

public class MyTask extends TimerTask {
    private Main main;

    public MyTask(Main main) {
        this.main = main;
    }

    @Override
    public void run()
    {
        //unhabitable_planets
        main.MyPlanets();
        main.DestoryPlanets();
    }
}