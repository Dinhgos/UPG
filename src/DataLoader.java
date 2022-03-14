import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Načte data ze vstupního souboru. Cesta k souboru se mění v Main.filePath.
 */
public class DataLoader {

    /**
     * Načte data ze vstupního souboru
     */
    public void loadData() {
        Scanner sc = null;


        // cesta k souboru
        String pathName = Main.filePath;

        File fileWithData = new File(pathName);

        try {
            sc = new Scanner(fileWithData);
        } catch (FileNotFoundException e) {
            pathName = "bin/data/" + Main.filePath;
            fileWithData = new File(pathName);

            try {
                sc = new Scanner(fileWithData);
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("Failed to load data.");
                System.exit(-1);
            }
        }

        // počet načtenźch dat
        int dataCounter = 0;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            // filtrování validních dat
            if (!(line.charAt(0) == '#') && !line.isBlank()) {
                String[] data = line.split(" ");

                for(int i = 0; i < data.length; i++) {
                    if(!data[i].isBlank()) {
                        String str = data[i];

                        // typ souboru PGM
                        if (Main.fileType.isBlank()) {
                            Main.fileType = str;
                        }

                        // šířka
                        else if (Main.width == -1) {
                            Main.width = Integer.parseInt(str);
                        }

                        // výška
                        else if (Main.height == -1) {
                            Main.height = Integer.parseInt(str);
                        }

                        // maximální hodnota v datech
                        else if (Main.maxValue == -1) {
                            Main.maxValue = Integer.parseInt(str);
                            Main.data = new int[Main.height][Main.width];
                        }

                        // data
                        else if (!(Main.data == null)) {
                            int x = dataCounter % Main.width;
                            int y = dataCounter / Main.width;
                            Main.data[y][x] = Integer.parseInt(str);
                            dataCounter++;
                        }
                    }
                }
            }
        }


        sc.close();
    }
}
