import java.awt.*;

/**
 * Třída pro vykreslení legendy.
 */
public class Legend extends DrawingPanel{
    /** šířka */
    private int width = 200;

    /** okraje */
    private int offset = 10;

    /** výška jednoho prvku */
    private int barH = 40;

    /** mezera mezi prvky */
    private int step = 50;

    /** Grafický kontext */
    private Graphics2D g2;

    /**
     * Konstruktor třídy
     */
    public Legend() {
        this.setPreferredSize(new Dimension(width, 600));
    }

    /**
     * Hlavní metoda pro vykreslení legendy
     * @param g Grafický kontext
     */
    @Override
    public void paint(Graphics g) {
        this.g2 = (Graphics2D) g;

        drawLegend();
    }

    /**
     * Vykreslí legendu
     */
    private void drawLegend() {
        int width = 20;
        int space = 10;

        g2.translate(offset, offset);
        int num1 = (minVal / step) * step;
        int num2 = num1 + step;

        for (Color color : palette) {
            String str = num1 + " - " + num2;

            num1 += step;
            num2 += step;

            g2.setColor(color);
            g2.fillRect(0, 0, width, barH);
            g2.setColor(Color.BLACK);
            g2.drawString(str, width + space, barH / 2);
            g2.translate(0, barH + space);
        }
    }
}
