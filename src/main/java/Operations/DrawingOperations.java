package Operations;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DrawingOperations extends Operations {
    private List<DrawOperation> operationList;
    private List<Color> colors;

    public DrawingOperations() {
        super();
        operationList = new ArrayList<>();
        colors = new LinkedList<>();
    }

    public void run(Graphics2D g) {
        if(!canDraw()) return;

        List<Operation> copy = new ArrayList<>(operationList);
        operationList = new ArrayList<>();
        super.run(g, copy);
    }

    public void add(Point p, int r, Color c) {
        operationList.add(new DrawOperation(p, r, c));
    }

    public boolean canDraw() {
        return operationList.size() > 0;
    }

    private class DrawOperation extends Operation {
        private int color = -1;

        DrawOperation(Point p, int radius, Color color) {
            super(p, radius);

            for (int i = 0; i < colors.size(); i++) {
                if(colors.get(i).equals(color)) {
                    this.color = i;
                }
            }
            if(this.color == -1) {
                this.color = colors.size();
                colors.add(color);
            }
        }

        DrawOperation(Point p, int radius, int color) {
            super(p, radius);
            this.color = color;
        }

        @Override
        public void doOperation(Graphics2D g) {
            g.setColor(colors.get(color));
            super.doOperation(g);
        }

        @Override
        public DrawOperation createCopyWithPoint(Point p) {
            return new DrawOperation(p, radius, color);
        }
    }

}
