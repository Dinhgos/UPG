import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Třída, která vytváří grafy.
 */
public class Chart {
    /** histogram */
    private JFreeChart histogram;

    /** tukey box */
    private JFreeChart tukeyBox;

    /**
     * Vytvoří histogram
     */
    public void createHistogram() {
        int[] vals = Stream.of(Main.data).flatMapToInt(IntStream::of).toArray();
        double[] data = Arrays.stream(vals).asDoubleStream().toArray();

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("key", data, 50);

        this.histogram = ChartFactory.createHistogram(
                "Histogram převýšení", "Nadmořská výška",
                "Počet výskytu", dataset
        );
    }

    /**
     * Vytvoří tukey box
     */
    public void createTukeyBox() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        int[] data = Stream.of(Main.data).flatMapToInt(IntStream::of).toArray();
        ArrayList<Integer> boxData = new ArrayList<>();
        String name = Main.filePath;

        for (int i : data) {
            boxData.add(i);
        }

        dataset.add(boxData, name, name);

        this.tukeyBox = ChartFactory.createBoxAndWhiskerChart(
                "Počet převýšení","Mapa", "Nadmořská výška",
                dataset, false
        );
    }

    /**
     * Vrací histogram
     * @return graf
     */
    public JFreeChart getHistogram() {
        return histogram;
    }

    /**
     * Vrací tukey box
     * @return graf
     */
    public JFreeChart getTukeyBox() {
        return tukeyBox;
    }
}
