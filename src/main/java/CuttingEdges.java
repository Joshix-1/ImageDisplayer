import java.awt.*;

public class CuttingEdges {
    static final int LEFT = 0, TOP = 1, RIGHT = 2, BOTTOM = 3;
    private int imgWidth, imgHeight, selected;
    private double[] edges;

    public CuttingEdges(int w, int h) {
        imgWidth = w;
        imgHeight = h;
        edges = new double[]{0, 0, w, h};
        selected = -1;
    }

    public void setLeft(int pos) {
        edges[LEFT] = pos >= getRight() - 1 ? getRight() - 1 : Math.max(pos, 0);
    }
    public void setTop(int pos) {
        edges[TOP] = pos >= getBottom() - 1 ? getBottom() - 1 : Math.max(pos, 0);
    }
    public void setRight(int pos) {
        edges[RIGHT] = pos <= getLeft() + 1 ? getLeft() + 1 : Math.min(pos, imgWidth);
    }
    public void setBottom(int pos) {
        edges[BOTTOM] = pos <= getTop() + 1 ? getTop() + 1 : Math.min(pos, imgHeight);
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
        return (int) edges[LEFT];
    }
    public int getTop() {
        return (int) edges[TOP];
    }
    public int getRight() {
        return (int) edges[RIGHT];
    }
    public int getBottom() {
        return (int) edges[BOTTOM];
    }

    public int getCutWidth() {
        return Math.max(0, (int) edges[RIGHT] - (int) edges[LEFT]);
    }

    public int getCutHeight() {
        return Math.max(0, (int) edges[BOTTOM] - (int) edges[TOP]);
    }

    public int touchesPoint(Point p) {
        if(p.x >= -Main.EDGE_SIZE && p.x <= getLeft() && p.y >= 0 && p.y < imgHeight) return LEFT;
        if(p.y >= -Main.EDGE_SIZE && p.y <= getTop()  && p.x >= 0 && p.x < imgWidth) return TOP;
        if(p.x <= imgWidth + Main.EDGE_SIZE && p.x >= getRight() && p.y >= 0 && p.y < imgHeight) return RIGHT;
        if(p.y <= imgHeight + Main.EDGE_SIZE && p.y >= getBottom() && p.x >= 0 && p.x < imgWidth) return BOTTOM;
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
        CuttingEdges copy = new CuttingEdges((int) (imgWidth * scaleW), (int) (imgHeight * scaleH));

        copy.setLeft((int) (getLeft() * scaleW));
        copy.setTop((int) (getTop() * scaleH));
        copy.setRight((int) (getRight() * scaleW));
        copy.setBottom((int) (getBottom() * scaleH));

        if(selected != -1) copy.selectEdge(selected);

        return copy;
    }
}