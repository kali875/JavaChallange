package GameData;

public class Actions {
    private static int actionCount = 0;

    public static void onActionAttributeChange(int newActionCount) {actionCount = newActionCount;}

    public static void onActionUsed() {actionCount = actionCount - 1;}

    public static int getRemainingActionCount() {return actionCount;}
}
