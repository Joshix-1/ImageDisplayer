import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ImageTester {
    private static int width = 300, height = 300;
    private HashMap<Color, Integer> colors;

    public ImageTester(Image img) {
        img = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING)).getImage();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();

        image.flush();

        HashMap<Color, Integer> m = new HashMap<>();
        for(int i=0; i < width ; i++) {
            for(int j=0; j < height ; j++) {
                Color rgb = new Color(image.getRGB(i, j));

                if (!isGray(rgb)) {
                    Integer counter = m.get(rgb);
                    if (counter == null)
                        counter = 0;
                    counter++;
                    m.put(rgb, counter);
                }
            }
        }
        colors = m;
        colors.remove(null);
    }


    public Color[] getMostCommonColors(int count) {
        List<Map.Entry<Color, Integer>> list = new ArrayList<>(colors.entrySet());
        count = Math.min(list.size(), count);
        Color[] c = new Color[count];
        list.sort(Comparator.comparingInt(Map.Entry::getValue));
        boolean b1 = true;
        int sim = 0;
        for (int i = 0; i < count - sim; i++) {
            if(list.get(i).getKey() == null) {
                b1 = false;
                sim++;
            }
            for (int j = 0; j < i - sim; j++) {
                if(areSimilar(list.get(i).getKey(), c[j])) {
                    b1 = false;
                    sim++;
                    break;
                }
            }
            if(b1) c[i - sim] = list.get(i).getKey();
            b1 = true;
        }
        return Arrays.copyOf(c, count - sim);
    }

    private static boolean areSimilar(Color c1, Color c2) {
        int rDiff = Math.abs(c1.getRed() - c2.getRed());
        int gDiff = Math.abs(c1.getGreen() - c2.getGreen());
        int bDiff = Math.abs(c1.getBlue() - c2.getBlue());

        return rDiff + bDiff + gDiff < 11;
    }

    private static boolean isGray(Color c) {
        int rgDiff = Math.abs(c.getRed() - c.getGreen());
        int rbDiff = Math.abs(c.getRGB() - c.getBlue());
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        return rbDiff <= tolerance && rgDiff <= tolerance;
    }
}