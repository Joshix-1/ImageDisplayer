
import Operations.DrawingOperations;
import Operations.RemovingOperations;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawImage {
    static final public Color TRANSPARENT = new Color(255, 255, 255, 0);
    static final public String COPIED = "Image copied";
    static final public String COPIED_AND_SAVED = "Image copied and saved";

    private Color[] colors = {Color.red, Color.black, Color.cyan, Color.BLUE, Color.gray, Color.green, Color.WHITE, Color.ORANGE, Color.MAGENTA};
    private Image img;
    private BufferedImage img2;
    private int drawSize;
    private String name;
    private int drawingIndex;
    private String imageCopied;
    private CuttingEdges edges;
    private DrawingOperations drawOps;
    private RemovingOperations removeOps;
    private int edgeSize;
    private boolean changed, drawn;

    public DrawImage(Image image, String name) {
        img = new ImageIcon(image).getImage();
        img2 = new BufferedImage(img.getWidth(null), img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        drawSize = 3 + (img2.getWidth() + img2.getHeight())/200;
        this.name = name;
        Color[] most_common = new ImageTester(img).getMostCommonColors(7);
        Color[] c = new Color[colors.length + most_common.length];
        System.arraycopy(most_common, 0, c, 0, most_common.length);
        System.arraycopy(colors, 0, c, most_common.length, colors.length);
        colors = c;
        drawingIndex = 0;
        imageCopied = "";
        drawOps = new DrawingOperations();
        removeOps = new RemovingOperations();
        changed = true;
        drawn = true;


        edges = new CuttingEdges(this);
    }

    public void draw(Point p) {
        drawOps.add(pointOnCutImage(p), drawSize, colors[drawingIndex]);
        changed = true;
    }

    public void remove(Point p) {
        removeOps.add(pointOnCutImage(p), drawSize);
        changed = true;
    }

    //private boolean pointIsOnCutImage(Point p) {
    //    return p.x >= edges.getLeft() && p.x < edges.getRight() && p.y >= edges.getTop() && p.y < edges.getBottom();
    //}

    private Point pointOnCutImage(Point p) {
        int xOnWindow = p.x - edges.getLeft();
        int yOnWindow = p.y - edges.getTop() ;
        return new Point(xOnWindow, yOnWindow);
    }

    public void resetDrawingOperations() {
        drawOps = new DrawingOperations();
        removeOps = new RemovingOperations();
    }

    public void drawCircles() {
        boolean remove = removeOps.canDraw();
        if(!(drawOps.canDraw() || remove || drawn)) return;
        try {
            drawn = false;

            boolean cut = edges.getCutWidth() == getWidth() && edges.getCutHeight() == getHeight();
            Graphics2D g;
            BufferedImage cut_copy = null;
            if (cut) {
                cut_copy = copyImage(img2.getSubimage(edges.getLeft(), edges.getTop(), edges.getCutWidth(), edges.getCutHeight()));
                g = cut_copy.createGraphics();
            } else {
                g = img2.createGraphics();
            }

            drawOps.run(g);
            removeOps.run(g);

            g.dispose();

            if (cut) { //draw cut_copy onto image:
                if (remove) {
                    int[] rgbArr = cut_copy.getRGB(0, 0, cut_copy.getWidth(), cut_copy.getHeight(), null, 0, cut_copy.getWidth());
                    img2.setRGB(edges.getLeft(), edges.getTop(), cut_copy.getWidth(), cut_copy.getHeight(), rgbArr, 0, cut_copy.getWidth());
                } else {
                    Graphics g2 = img2.getGraphics();
                    g2.drawImage(cut_copy, edges.getLeft(), edges.getTop(), null);
                    g2.dispose();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        drawn = true;
        changed = true;
    }

    public BufferedImage getDisplayImage() {
        return getDisplayImage(getWidth(), getHeight());
    }

    public BufferedImage getCopyImage() {
        return getDisplayImage().getSubimage(edges.getLeft(), edges.getTop(), edges.getCutWidth(), edges.getCutHeight());
    }

    public BufferedImage getDisplayImage(int w, int h) {
        double ratioW = (double)w/(double)getWidth();
        double ratioH = (double)h/(double)getHeight();

        try {
            CuttingEdges scaledEdges = this.edges.scaledCopy(ratioW, ratioH);

            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Image img_copy = new ImageIcon(this.img.getScaledInstance(  w, h, Image.SCALE_SMOOTH)).getImage();
            Image img2_copy = new ImageIcon(this.img2.getScaledInstance(w, h, Image.SCALE_SMOOTH)).getImage();

            Graphics g_bi = bi.getGraphics();

            g_bi.setColor(Main.BG);
            g_bi.fillRect(0, 0, w, h);
            g_bi.drawImage(img_copy, 0, 0, null);
            g_bi.drawImage(img2_copy,0, 0, null);


            g_bi.setColor(Main.BG);


            g_bi.fillRect(0, 0, scaledEdges.getLeft(), h);
            g_bi.fillRect(0, 0, w, scaledEdges.getTop());
            g_bi.fillRect(scaledEdges.getRight(), 0, w - scaledEdges.getRight(), h);
            g_bi.fillRect(0,  scaledEdges.getBottom(), w, h - scaledEdges.getBottom());

            g_bi.setColor(Color.BLACK);
            g_bi.drawRect(scaledEdges.getLeft(), scaledEdges.getTop(), scaledEdges.getCutWidth() - 1, scaledEdges.getCutHeight() - 1);

            g_bi.dispose();

            changed = false;

            return bi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getWidth() {
        return img2.getWidth();
    }

    public int getHeight() {
        return img2.getHeight();
    }

    public String getTitle() {
        return imageCopied.isEmpty() ? (name + " " + edges.getCutWidth() + "x" + edges.getCutHeight() + " (" + ((int) drawSize) + ")") : imageCopied;
    }

    public String getName() {
        return name;
    }

    public void setImageCopied(String str) {
        this.imageCopied = str;
    }

    public void removeImageCopied() {
        this.imageCopied = "";
    }

    public Color getDrawingColor() {
        return colors[drawingIndex];
    }

    public void changeDrawingColor() {
        Color pre = colors[drawingIndex];
        do{
            drawingIndex = (drawingIndex + 1) % colors.length;
        } while(colors[drawingIndex] == null || colors[drawingIndex].equals(pre));
        changed = true;
    }

    public void increaseDrawSize(int incr) {
        drawSize += incr;
        if(drawSize <= 0) drawSize = 1;
    }

    public boolean pointIsOnEdge(Point p) {
        return p != null && edges.touchesPoint(p) != -1;
    }

    public void startEdgeSelection(Point p) {
        edges.selectEdge(edges.touchesPoint(p));
    }

    public void unselectEdge() {
        edges.unselectEdge();
    }

    public void moveSelectedEdge(Point p) {
        edges.moveSelectedEdge(p);
        changed = true;
    }

    public static BufferedImage copyImage(Image source){
        source = new ImageIcon(source).getImage();
        BufferedImage b = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    public boolean hasChanged() {
        return  changed;
    }

    public void setChanged() {
        changed = true;
    }

    public int getEdgeSize() {
        return edgeSize;
    }

    public void setEdgeSize(int edgeSize) {
        this.edgeSize = edgeSize;
    }

    public boolean hasDrawn() {
        return drawn;
    }
}
