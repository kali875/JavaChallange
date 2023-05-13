import javax.swing.*;
import java.awt.*;

public class Grid extends JPanel
{
    int CellSize = 5;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ShowGrid(g);
    }

    private void ShowGrid(Graphics g) {
        g.setColor(Color.BLACK);
        // Vízszintes vonalak
        for (int i = 0; i < 142 + 1; i++) {
            g.drawLine(20, 20 + i * CellSize, 20 + 142 * CellSize, 20 + i * CellSize);
        }
    }
}
