package Utils;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class GameWorldTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Color getBackground() {
        String steak = getText();
        if (steak.startsWith("?")) {
            return Color.YELLOW;
        } else if (steak.startsWith("O")) {
            return Color.GREEN;
        } else if (steak.startsWith("x")) {
            return Color.RED;
        } else if (steak.startsWith("SM")) {
            return Color.CYAN;
        } else if (steak.startsWith("MB")) {
            return Color.PINK;
        } else if (steak.startsWith("u")) {
            return Color.ORANGE;
        } else if (steak.startsWith("WHA")) {
            return Color.GRAY;
        }else if (steak.startsWith("WHB")) {
            return Color.BLACK;
        } else if (steak.equals("")) {
            return Color.WHITE;
        }

        return super.getBackground();
    }
}
