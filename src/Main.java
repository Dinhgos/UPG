import org.jfree.chart.ChartPanel;
import org.jfree.svg.SVGGraphics2D;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Trida Main se stará o spuštění programu.
 */
public class Main {

	/** Typ souboru PGM */
	public static String fileType = "";

	/** Výška obrázku */
	public static int height = -1;

	/** Šířka obrázku */
	public static int width = -1;

	/** Maximální hodnota v PGM */
	public static int maxValue = -1;

	/** PGM data */
	public static int[][] data;

	/** Cesta k souboru */
	public static String filePath;

	/**
	 * Zavolá metodu DataLoader, která načte data ze vstupního souboru.
	 * Dále vytvoří okno, do kterého se vykreslí obrázek.
	 * @param args vstupní parametr
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			filePath = args[0];
		} else {
			System.out.println("Default scenario: plzen");
			filePath = "bin/data/plzen.pgm";
		}

		DataLoader loader = new DataLoader();
		loader.loadData();

		JFrame win = new JFrame();
		win.setTitle("Xuan Toan DINH, A19B0027P");
//		win.setMinimumSize(new Dimension(800, 600));

		addWindowComponents(win);
		win.pack();

		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.setLocationRelativeTo(null);
		win.setVisible(true);
	}

	/**
	 * Přidá komponenty do okna.
	 * @param win Jframe okno
	 */
	private static void addWindowComponents(JFrame win) {
		DrawingPanel panel = new DrawingPanel();
		win.add(panel, BorderLayout.CENTER);

		Legend legend = new Legend();
		win.add(legend, BorderLayout.EAST);

		JPanel controllerPanel = new JPanel();
		win.add(controllerPanel, BorderLayout.SOUTH);

		JButton hisBtn = new JButton("Histogram");
		controllerPanel.add(hisBtn);
		hisBtn.addActionListener(e -> showHis());

		JButton boxBtn = new JButton("BoxPlot");
		controllerPanel.add(boxBtn);
		boxBtn.addActionListener(e -> showBox());

		JButton btnPrint = new JButton("Print");
		controllerPanel.add(btnPrint);
		btnPrint.addActionListener(e -> print(panel));

		JButton svgPrint = new JButton("SVG");
		controllerPanel.add(svgPrint);
		svgPrint.addActionListener(e -> printSVG(panel));

		JButton pngBtn = new JButton("PNG");
		controllerPanel.add(pngBtn);
		pngBtn.addActionListener(e -> printPNG(panel));

		JButton asciiBtn = new JButton("ASCII");
		controllerPanel.add(asciiBtn);
		asciiBtn.addActionListener(e -> panel.createASCII());

		JButton moveBtn = new JButton("Move");
		controllerPanel.add(moveBtn);
		moveBtn.addActionListener(e -> move(panel));
	}

	/**
	 * Vytvoří okno s tlačítky pro pohyb.
	 * @param panel plátno
	 */
	private static void move(DrawingPanel panel) {
		JFrame win = new JFrame();
		win.setTitle("Controlls");
		win.setMinimumSize(new Dimension(200, 60));

		JPanel controllerPanel = new JPanel();
		win.add(controllerPanel);

		JButton upBtn = new JButton("up");
		controllerPanel.add(upBtn);
		upBtn.addActionListener(e -> panel.moveUp());

		JButton downBtn = new JButton("down");
		controllerPanel.add(downBtn);
		downBtn.addActionListener(e -> panel.moveDown());

		JButton leftBtn = new JButton("left");
		controllerPanel.add(leftBtn);
		leftBtn.addActionListener(e -> panel.moveLeft());

		JButton rightBtn = new JButton("right");
		controllerPanel.add(rightBtn);
		rightBtn.addActionListener(e -> panel.moveRight());

		JButton zoomInBtn = new JButton("+");
		controllerPanel.add(zoomInBtn);
		zoomInBtn.addActionListener(e -> panel.zoomIn());

		JButton zoomOutBtn = new JButton("-");
		controllerPanel.add(zoomOutBtn);
		zoomOutBtn.addActionListener(e -> panel.zoomOut());

		JButton resetBtn = new JButton("reset");
		controllerPanel.add(resetBtn);
		resetBtn.addActionListener(e -> panel.reset());

		win.pack();
		win.setLocationRelativeTo(null);
		win.setVisible(true);
	}

	/**
	 * Vytvoří PNG soubor o dané velikosti.
	 * @param drawingPanel plátno
	 */
	private static void printPNG(DrawingPanel drawingPanel) {
		JTextField width = new JTextField(5);
		JTextField height = new JTextField(5);
		JPanel panel = new JPanel();
		panel.add(new JLabel("Sirka:"));
		panel.add(width);
		panel.add(Box.createHorizontalStrut(15));
		panel.add(new JLabel("Vyska:"));
		panel.add(height);

		int confirmDialog = JOptionPane.showConfirmDialog(null, panel, "Zadejte rozliseni", JOptionPane.OK_CANCEL_OPTION);

		if (confirmDialog == 0) {
			try {
				int w = Integer.parseInt(width.getText());
				int h = Integer.parseInt(height.getText());
				BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D g2 = bufferedImage.createGraphics();
				drawingPanel.drawPng(g2,w,h);

				File file = new File("myPNGimage.png");
				ImageIO.write(bufferedImage, "png", file);
				System.out.println("Successfully created PNG image.");
			} catch (Exception e) {
				System.out.println("Wrong number input.");
			}
		}
	}

	/**
	 * Vytiskne plátno.
	 * @param panel plátno
	 */
	private static void print(DrawingPanel panel) {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (job.printDialog()) {
			job.setPrintable(panel);
			try {
				job.print();
			} catch (PrinterException printerException) {
				printerException.printStackTrace();
			}
		}
	}

	/**
	 * Exportujte vrstevnice a šipky do SVG.
	 * @param panel plátno
	 */
	private static void printSVG(DrawingPanel panel) {
		SVGGraphics2D svg = new SVGGraphics2D(1920, 1080);
		panel.svg(svg);

		try {
			FileWriter myWriter = new FileWriter("mySVGimage.svg");
			myWriter.write(svg.getSVGElement());
			myWriter.close();
			System.out.println("Successfully created SVG image.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	/**
	 * Vytvoří okno s tukey box
	 */
	private static void showBox() {
		JFrame win = new JFrame();
		win.setTitle("Box plot");
		win.setMinimumSize(new Dimension(800, 600));

		Chart chart = new Chart();
		chart.createTukeyBox();

		ChartPanel chartPanel = new ChartPanel(chart.getTukeyBox());
		win.add(chartPanel);

		win.pack();
		win.setLocationRelativeTo(null);
		win.setVisible(true);
	}

	/**
	 * Vytvoří okno s histogramem.
	 */
	private static void showHis() {
		JFrame win = new JFrame();
		win.setTitle("Histogram");
		win.setMinimumSize(new Dimension(800, 600));

		Chart chart = new Chart();
		chart.createHistogram();

		ChartPanel chartPanel = new ChartPanel(chart.getHistogram());
		win.add(chartPanel);

		win.pack();
		win.setLocationRelativeTo(null);
		win.setVisible(true);
	}
}
