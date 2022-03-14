import java.awt.*;

/**
 * Třída vytváří barvy pro mapu.
 */
public class ColorPalette {
    /** výchozí barvy */
    public Color[] defaultColors = new Color[] {Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED, new Color(102, 51, 0)};

    /** nejnižší hodnota v mapě */
    private int minVal;

    /** paleta barev */
    public Color[] palette;

    /**
     * Vytvoří paletu barev
     */
    public void createPalette() {
        findMinVal();

        int numOfColors = getNumOfColors();
        palette = new Color[numOfColors];
        float[] blending = getBlending(numOfColors);

        for (int i = 0; i < numOfColors; i++) {
            int index = (int) blending[i];

            Color color1 = defaultColors[index];
            Color color2;

            try {
                color2 = defaultColors[index + 1];

            } catch (IndexOutOfBoundsException e) {
                color1 = defaultColors[index - 1];
                color2 = defaultColors[index];
            }

            float ratio = 1 - (blending[i] % 1);

            palette[i] = blend(color1, color2, ratio);
        }
    }

    /**
     * Lineární interpolace dvour barev
     * @param color1 první barva
     * @param color2 druhá barva
     * @param ratio poměr první ku druhé
     * @return výsledná barva
     */
    public static Color blend (Color color1, Color color2, double ratio) {
        float r  = (float) ratio;
        float ir = (float) 1.0 - r;

        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];

        color1.getColorComponents (rgb1);
        color2.getColorComponents (rgb2);

        Color color = new Color (rgb1[0] * r + rgb2[0] * ir,
                rgb1[1] * r + rgb2[1] * ir,
                rgb1[2] * r + rgb2[2] * ir);

        return color;
    }

    /**
     * Vytvoří pole pro výpočet lineární interpolace barev.
     * @param numOfColor počet barev
     * @return pole
     */
    private float[] getBlending(int numOfColor) {
        float[] colors = new float[numOfColor];
        float value = 0;
        float step;

        if (numOfColor > 1) {
            step = (float) (defaultColors.length - 1) / (numOfColor - 1);
        } else {
            step = 0;
        }

        for (int i = 0; i < numOfColor; i++) {
            colors[i] = value;
            value += step;
        }

        if (colors[numOfColor - 1] >= defaultColors.length - 1) {
            colors[numOfColor - 1] = defaultColors.length - 1;
            colors[numOfColor - 1] -= 0.000001;
        }

        return colors;
    }

    /**
     * Vypočte kolik barev je potřeba pro mapu.
     * @return počet barev
     */
    private int getNumOfColors() {
        int step = 50;

        int min = minVal / step;
        min *= step;

        int max = Main.maxValue / step;
        max *= step;

        if (Main.maxValue % step != 0) {
            max += step;
        }

        int tmp = (max - min) / step;

        if (tmp < 1) {
            tmp = 1;
        }

        return tmp;
    }

    /**
     * Najde nejmenší hodnotu v mapě.
     */
    private void findMinVal() {
        int minValue = Integer.MAX_VALUE;

        for(int y = 0; y < Main.data.length; ++y) {
            for (int x = 0; x < Main.data[y].length; ++x) {
                if (Main.data[y][x] < minValue) {

                    minValue = Main.data[y][x];
                }
            }
        }

        this.minVal = minValue;
    }

    /**
     * Vrací paletu.
     * @return paleta
     */
    public Color[] getColors() {
        return palette;
    }

    /**
     * Vrací nejmenší hodnotu.
     * @return nejmenší hodnota
     */
    public int getMinVal() {
        return minVal;
    }
}
