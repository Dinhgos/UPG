import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Stará se o vzkreslení obrázku do okna.
 */
public class DrawingPanel extends JPanel implements Printable {

    /** Obrázek, který se vzkresli do okna */
    public BufferedImage image;

    /** Hodnota pro škálování obrázku */
    private double scale = 1;

    /** Grafický kontext */
    private Graphics2D g2;

    /** paleta barev */
    protected Color[] palette;

    /** nejnižší hodnota v datech */
    protected int minVal;

    /** bod kliknutí */
    private Point point;

    /** posun x */
    private int offsetX = 0;

    /** posun y */
    private int offsetY = 0;

    /** zmenšení/zvětšení */
    private double zoom = 1;

    /** Construktor */
    public DrawingPanel() {
        this.setPreferredSize(new Dimension(800, 600));
        ColorPalette palette = new ColorPalette();
        palette.createPalette();
        this.palette = palette.getColors();
        this.minVal = palette.getMinVal();
    }

    /**
     * Vykreslí obrázek
     * @param g Grafický kontext
     */
    @Override
    public void paint(Graphics g) {
        g2 = (Graphics2D)g;

        // bilineární interpolace
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHints(rh);

        g2.translate(offsetX,offsetY);
        g2.scale(zoom,zoom);

        processImage();
        scale();
        drawImage();

        processContourLine();
        clickArrow();
        findMaxVertical();
        findMinVertical();
        findMaxGrade();
    }

    /**
     * Zobrazí převýšení v daném bodě a také se zvýrazní vrstevice.
     */
    private void clickArrow() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                point = new Point(e.getX(), e.getY());
                repaint();
            }
        });

        if (point != null) {
            int imW = this.image.getWidth();
            int imH = this.image.getHeight();

            int nimW = (int)(imW*this.scale);
            int nimH = (int)(imH*this.scale);

            if (point.getX() <= nimW && point.getY() <= nimH) {
                int x = (int) (point.getX() / scale);
                int y = (int) (point.getY() / scale);
                int val = Main.data[y][x];

                int max = Main.maxValue / 50 * 50 + 50;
                int min = minVal / 50 * 50;
                int up = Main.data[y][x] / 50 * 50 + 50;
                int down = Main.data[y][x] / 50 * 50;

                if (down == min) {
                    drawConLine(up, Color.MAGENTA);
                } else if (up == max) {
                    drawConLine(down, Color.MAGENTA);
                } else if (Math.abs(down - val) < Math.abs(up - val)) {
                    drawConLine(down, Color.MAGENTA);
                } else {
                    drawConLine(up, Color.MAGENTA);
                }

                setUpArrow((int)point.getX(), (int)point.getY(), Integer.toString(val));
            }
        }
    }

    /**
     * Vypočítá hodnoty vrstevnic.
     */
    private void processContourLine() {
        ArrayList<Integer> h = new ArrayList<>();
        int roundedMin = minVal / 50 * 50;

        for (int i = 1; i <= palette.length - 1; i++) {
            h.add(roundedMin + i * 50);
        }

        for (int height : h) {
            drawConLine(height, Color.BLACK);
        }
    }

    /**
     * Vykreslí vrstevnice.
     * @param height výška vrstevnice
     * @param color barva vrstevnice
     */
    private void drawConLine(int height, Color color) {
        int[][] d = Main.data;
        int imW = this.image.getWidth();
        int imH = this.image.getHeight();
        int[][] cases = new int[][] {
                {1,0,0,0},{0,1,1,1},{0,1,0,0},{1,0,1,1},{0,0,1,0},{1,1,0,1},{0,0,0,1},
                {1,1,1,0},{1,1,0,0},{0,0,1,1},{1,0,1,0},{0,1,0,1},{1,0,0,1},{0,1,1,0}
        };
        int lx1;
        int ly1;
        int lx2;
        int ly2;

        g2.setColor(color);

        for (int y = 0; y < imH - 1; y++) {
            for (int x = 0; x < imW - 1; x++) {
                int a = d[y][x];
                int b = d[y][x + 1];
                int c = d[y + 1][x];
                int e = d[y + 1][x + 1];
                int[] v = new int[4];

                if (a < height) {
                    v[0] = 1;
                }

                if (b < height) {
                    v[1] = 1;
                }

                if (c < height) {
                    v[2] = 1;
                }

                if (e < height) {
                    v[3] = 1;
                }

                if (Arrays.equals(v, cases[0]) || Arrays.equals(v, cases[1])) {
                    lx1 = (int) (x * scale + scale);
                    ly1 = (int) (y * scale + 0.5 * scale);
                    lx2 = (int) (x * scale + 0.5 * scale);
                    ly2 = (int) (y * scale + scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }

                if (Arrays.equals(v, cases[2]) || Arrays.equals(v, cases[3])) {
                    lx1 = (int) (x * scale + scale);
                    ly1 = (int) (y * scale + 0.5 * scale);
                    lx2 = (int) (x * scale + 1.5 * scale);
                    ly2 = (int) (y * scale + scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }

                if (Arrays.equals(v, cases[4]) || Arrays.equals(v, cases[5])) {
                    lx1 = (int) (x * scale + 0.5 * scale);
                    ly1 = (int) (y * scale + scale);
                    lx2 = (int) (x * scale + scale);
                    ly2 = (int) (y * scale + 1.5 * scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }

                if (Arrays.equals(v, cases[6]) || Arrays.equals(v, cases[7])) {
                    lx1 = (int) (x * scale + 1.5 * scale);
                    ly1 = (int) (y * scale + scale);
                    lx2 = (int) (x * scale + scale);
                    ly2 = (int) (y * scale + 1.5 * scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }

                if (Arrays.equals(v, cases[8]) || Arrays.equals(v, cases[9])) {
                    lx1 = (int) (x * scale + 0.5 * scale);
                    ly1 = (int) (y * scale + scale);
                    lx2 = (int) (x * scale + 1.5 * scale);
                    ly2 = (int) (y * scale + scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }

                if (Arrays.equals(v, cases[10]) || Arrays.equals(v, cases[11])) {
                    lx1 = (int) (x * scale + scale);
                    ly1 = (int) (y * scale + 0.5 * scale);
                    lx2 = (int) (x * scale + scale);
                    ly2 = (int) (y * scale + 1.5 * scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }

                if (Arrays.equals(v, cases[12]) || Arrays.equals(v, cases[13])) {
                    lx1 = (int) (x * scale + scale);
                    ly1 = (int) (y * scale + 0.5 * scale);
                    lx2 = (int) (x * scale + 0.5 * scale);
                    ly2 = (int) (y * scale + scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);

                    lx1 = (int) (x * scale + 1.5 * scale);
                    ly1 = (int) (y * scale + scale);
                    lx2 = (int) (x * scale + scale);
                    ly2 = (int) (y * scale + 1.5 * scale);
                    g2.drawLine(lx1,ly1,lx2,ly2);
                }
            }
        }
    }

    /**
     * Najde maximální stoupání
     */
    private void findMaxGrade() {
        int[][] array = Main.data;

        // pole stoupání
        int[][] grade = new int[Main.height][Main.width];
        int compare;

        for (int y = 0; y < Main.height; y++) {
            for (int x = 0; x < Main.width; ++x) {
                int mid = array[y][x];

                // sousední pole ve vzdálenosti 1
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        int localGrade;

                        try {
                            compare = array[y + i][x + j];

                            // diagonální
                            if (i == -1 && j == -1 || i == -1 && j == 1 || i == 1 && j == -1 || i == 1 && j == 1) {
                                localGrade = (int) ((mid - compare) / 1.4);
                            }

                            // vedlejší
                            else {
                                localGrade = mid - compare;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            localGrade = -1;
                        }

                        if (localGrade > grade[y][x]) {
                            grade[y][x] = localGrade;
                        }
                    }
                }
            }
        }

        int maxGradeX = -1;
        int maxGradeY = -1;
        int maxValue = -1;

        // najde maximální hodnotu v grade a jho indexy
        for (int i = 0; i < grade.length; i++) {
            for (int j = 0; j < grade[0].length; j++) {
                if (grade[i][j] > maxValue) {
                    maxValue = grade[i][j];
                    maxGradeX = j;
                    maxGradeY = i;
                }
            }
        }

        int x = (int) (maxGradeX * scale);
        int y = (int) (maxGradeY * scale);

        setUpArrow(x, y, "Max. stoupani");
    }

    /**
     * Najde minimální převýšení
     */
    private void findMinVertical() {
        int minValue = Integer.MAX_VALUE;
        int minX = -1;
        int minY = -1;

        for(int y = 0; y < Main.data.length; ++y) {
            for (int x = 0; x < Main.data[y].length; ++x) {
                if (Main.data[y][x] < minValue) {
                    minX = x;
                    minY = y;
                    minValue = Main.data[y][x];
                }
            }
        }

        int x = (int) (minX * scale);
        int y = (int) (minY * scale);

        setUpArrow(x, y, "Min. prevyseni");
    }

    /**
     * Najde maximální převýšení
     */
    private void findMaxVertical() {
        int maxValue = -1;
        int maxX = -1;
        int maxY = -1;

        for(int y = 0; y < Main.data.length; ++y) {
            for (int x = 0; x < Main.data[y].length; ++x) {
                if (Main.data[y][x] > maxValue) {
                    maxX = x;
                    maxY = y;
                    maxValue = Main.data[y][x];
                }
            }
        }

        int x = (int) (maxX * scale);
        int y = (int) (maxY * scale);

        setUpArrow(x, y, "Max. prevyseni");
    }

    /**
     * Vypočítá hodnotu pro škálování
     */
    private void scale() {
        double scalex = ((double) this.getWidth() / (image.getWidth()));
        double scaley = ((double) this.getHeight() / (image.getHeight()));
        this.scale = Math.min(scalex, scaley);
    }

    /**
     * Vykreslí obrázek na plátno
     */
    private void drawImage() {
        int imW = this.image.getWidth();
        int imH = this.image.getHeight();

        int nimW = (int)(imW*this.scale);
        int nimH = (int)(imH*this.scale);

        g2.setColor(Color.BLACK);
        g2.fillRect(-this.getWidth(),-this.getHeight(),2*this.getWidth(),2*this.getHeight());

        g2.drawImage(this.image, 0, 0, nimW, nimH,null);
    }

    /**
     * Zpracuje vstupní data na obraz
     */
    private void processImage() {
        int iW = Main.width;
        int iH = Main.height;
        int offset = (minVal / 50) * 50;


        this.image = new BufferedImage(iW, iH, BufferedImage.TYPE_3BYTE_BGR);

//        nastaví barvu jednotlivých pixelů
//        int max = Main.maxValue;
//        int g;
//        for(int row=0; row<Main.data.length; ++row) {
//            for (int col = 0; col < Main.data[row].length; ++col) {
//                g = 255 * Main.data[row][col] / max;
//                this.image.setRGB(col, row, ((g << 16) | (g << 8) | g));
//            }
//        }

        for(int row=0; row<Main.data.length; ++row) {
            for (int col = 0; col < Main.data[row].length; ++col) {
                int num = Main.data[row][col] - offset;
                int index = num / 50;
                this.image.setRGB(col, row, palette[index].getRGB());
            }
        }
    }

    /**
     * Vypočte souřadnice šipky a popisku.
     * @param x2 koncový bod šipky
     * @param y2 koncový bod šipky
     * @param text popisek šipky
     */
    private void setUpArrow(int x2, int y2, String text) {
        int arrowLength = 45;

        // střed
        int midX = (int) ((image.getWidth() / 2) * scale);
        int midY = (int) ((image.getHeight() / 2) * scale);

        // složky vektoru od (x1, y1) k (x2, y2)
        int vx = x2 - midX;
        int vy = y2 - midY;

        // vektor v
        double vLength = Math.sqrt(vx*vx + vy*vy);

        double vNormX = vx / vLength;
        double vNormY = vy / vLength;

        double vArrowX = vNormX * arrowLength;
        double vArrowY = vNormY * arrowLength;

        // počátek
        int x1 = (int) (x2 - vArrowX);
        int y1 = (int) (y2 - vArrowY);

        // souřadnice popisku
        int txtX = (int) (x2 - vArrowX * 1.4);
        int txtY = (int) (y2 - vArrowY * 1.4);

        g2.setColor(Color.BLUE);

        drawArrow(x1,y1,x2,y2);
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        int textHeight = metrics.getHeight();
        int textWidth = metrics.stringWidth(text);

        g2.drawString(text, txtX - textWidth / 2, txtY + textHeight / 2);
    }

    /**
     * Vykreslí šipku na plátno
     * @param x1 počáteční bod šipky
     * @param y1 počáteční bod šipky
     * @param x2 koncový bod šipky
     * @param y2 koncový bod šipky
     */
    private void drawArrow(double x1, double y1, double x2, double y2) {
        // délka šipky
        double tipLength = 20;

        // šířka šipky
        double tipWidth = 10;

        double vx = x2 - x1;
        double vy = y2 - y1;

        double length = Math.sqrt(vx * vx + vy * vy);

        double vxn = vx / length;
        double vyn = vy / length;

        double tipx = vxn*tipLength;
        double tipy = vyn*tipLength;

        double ax = x2 - tipx;
        double ay = y2 - tipy;

        double nx = vyn * tipWidth;
        double ny = -vxn * tipWidth;

        double bx = ax + nx;
        double by = ay + ny;
        double cx = ax - nx;
        double cy = ay - ny;

        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        g2.draw(new Line2D.Double(x1, y1, x2, y2));

        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

        Path2D tip = new Path2D.Double();
        tip.moveTo(bx, by);
        tip.lineTo(x2,y2);
        tip.lineTo(cx,cy);
        g2.draw(tip);
    }

    /**
     * Vytiskne plátno.
     * @param graphics grafický kontext
     * @param pageFormat formát stránky
     * @param pageIndex index stránky
     * @return 0 = OK
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0 ) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D)graphics;

        final double paperW = 590;
        final double paperH = 835;

        double scaleX = paperW / Main.width;
        double scaleY = paperH / Main.height;
        double scale = Math.min(scaleX, scaleY);
        g2.scale(scale,scale);

        printPanel(graphics);

        return 0;
    }

    /**
     * Vykreslí plátno na papír.
     * @param g grafický kontext
     */
    private void printPanel(Graphics g) {
        g2 = (Graphics2D)g;

        processImage();
        drawImage();

        this.scale = 1;

        processContourLine();
        findMaxVertical();
        findMinVertical();
        findMaxGrade();
    }

    /**
     * Vykreslí plátno pro SVG
     * @param g grafický kontext
     */
    public void svg(Graphics g) {
        g2 = (Graphics2D)g;

        processImage();
        scale();

        processContourLine();
        clickArrow();
        findMaxVertical();
        findMinVertical();
        findMaxGrade();
    }

    /**
     * Vykreslí plátno do PNG
     * @param g grafický kontext
     * @param width šířka png obrázku
     * @param height výška PNG obrázku
     */
    public void drawPng(Graphics g, int width, int height) {
        g2 = (Graphics2D)g;

        // bilineární interpolace
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHints(rh);

        processImage();

        double scalex = ((double) width / (image.getWidth()));
        double scaley = ((double) height / (image.getHeight()));
        this.scale = Math.min(scalex, scaley);

        drawImage();

        processContourLine();
        clickArrow();
        findMaxVertical();
        findMinVertical();
        findMaxGrade();
    }

    /**
     * Uloží mapu do textového ASCII art formátu.
     */
    public void createASCII() {
        // maximální počet písmen
        int max = 26;

        if (palette.length > max) {
            System.out.println("Too many colors for ASCII.");
            return;
        }

        HashMap<Integer, Integer> asciiColor = new HashMap<>();
        // začátek ascii znaků
        int asciiStart = 65;

        for (int i = 0; i < palette.length; i++) {
            int tmp = asciiStart + i;
            asciiColor.put(palette[i].getRGB(), tmp);
        }

        try {
            FileWriter myWriter = new FileWriter("ASCII.txt");

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    myWriter.write(asciiColor.get(image.getRGB(x,y)));
                }

                myWriter.write("\n");
            }

            myWriter.close();
            System.out.println("Successfully created ASCII art.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * posune obrázek nahoru
     */
    public void moveUp() {
        this.offsetY -= 5;
        repaint();
    }

    /**
     * posune obrázek doleva
     */
    public void moveLeft() {
        this.offsetX -= 5;
        repaint();
    }

    /**
     * posune obrázek do prava
     */
    public void moveRight() {
        this.offsetX += 5;
        repaint();
    }

    /**
     * posune obrázek dolu
     */
    public void moveDown() {
        this.offsetY += 5;
        repaint();
    }

    /**
     * přiblíží obrázek
     */
    public void zoomIn() {
        this.zoom += 0.1;
        repaint();
    }

    /**
     * oddálí obrázek
     */
    public void zoomOut() {
        this.zoom -= 0.1;
        repaint();
    }

    /**
     * vrátí obrázek do výchozí polohy
     */
    public void reset() {
        this.zoom = 1;
        this.offsetX = 0;
        this.offsetY = 0;
        repaint();
    }
}
