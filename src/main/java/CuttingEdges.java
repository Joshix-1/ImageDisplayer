import java.awt.*;
import java.awt.image.BufferedImage;

public class CuttingEdges {
    private static final int LEFT = 0, TOP = 1, RIGHT = 2, BOTTOM = 3;
    private int selected;
    private DrawImage image;
    private int[] edges;

    public CuttingEdges(DrawImage img) {
        image = img;
        edges = new int[]{0, 0, img.getWidth(), img.getHeight()};
        selected = -1;
    }

    public void setLeft(int pos) {
        edges[LEFT] = pos >= getRight() - 1 ? getRight() - 1 : Math.max(pos, 0);
    }
    public void setTop(int pos) {
        edges[TOP] = pos >= getBottom() - 1 ? getBottom() - 1 : Math.max(pos, 0);
    }
    public void setRight(int pos) {
        edges[RIGHT] = pos <= getLeft() + 1 ? getLeft() + 1 : Math.min(pos, image.getWidth());
    }
    public void setBottom(int pos) {
        edges[BOTTOM] = pos <= getTop() + 1 ? getTop() + 1 : Math.min(pos, image.getHeight());
    }

    private void setIndex(int index, Point p) {
        switch (index) {
            case LEFT: {
                setLeft(p.x);
                break;
            }
            case TOP: {
                setTop(p.y);
                break;
            }
            case RIGHT: {
                setRight(p.x);
                break;
            }
            case BOTTOM: {
                setBottom(p.y);
                break;
            }
        }
    }

    public int getLeft() {
        return edges[LEFT];
    }
    public int getTop() {
        return edges[TOP];
    }
    public int getRight() {
        return edges[RIGHT];
    }
    public int getBottom() {
        return edges[BOTTOM];
    }

    public int getCutWidth() {
        return Math.max(0, edges[RIGHT] - edges[LEFT]);
    }

    public int getCutHeight() {
        return Math.max(0, edges[BOTTOM] - edges[TOP]);
    }

    public int touchesPoint(Point p) {
        if(p.x >= -image.getEdgeSize() && p.x <= getLeft() && p.y >= 0 && p.y < image.getHeight()) return LEFT;
        if(p.y >= -image.getEdgeSize() && p.y <= getTop()  && p.x >= 0 && p.x < image.getWidth()) return TOP;
        if(p.x <= image.getWidth() + image.getEdgeSize() && p.x >= getRight() && p.y >= 0 && p.y < image.getHeight()) return RIGHT;
        if(p.y <= image.getHeight() + image.getEdgeSize() && p.y >= getBottom() && p.x >= 0 && p.x < image.getWidth()) return BOTTOM;
        //here check if on corner:
        return -1;
    }

    public void selectEdge(int edge) {
        selected = edge;
    }

    public void unselectEdge() {
        selected = -1;
    }

    public void moveSelectedEdge(Point p) {
        if(selected == -1) return;
        Point p2 = p.getLocation();
        setIndex(selected, p2);
    }

    public CuttingEdges scaledCopy(double scaleW, double scaleH) {
        CuttingEdges copy = new CuttingEdges(new DrawImage(new BufferedImage((int) (image.getWidth() * scaleW), (int)(image.getHeight() * scaleH), BufferedImage.TYPE_INT_ARGB), ""));

        copy.setLeft((int) (getLeft() * scaleW));
        copy.setTop((int) (getTop() * scaleH));
        copy.setRight((int) (getRight() * scaleW));
        copy.setBottom((int) (getBottom() * scaleH));

        if(selected != -1) copy.selectEdge(selected);

        return copy;
    }
}