package Operations;

import java.awt.*;

public class Operation {
    private Point point;
    protected int radius;

    Operation(Point p, int radius) {
        this.point = p;
        this.radius = radius;
    }

    public void doOperation (Graphics2D g){
        if (radius <= 3) {
            g.fillRect(point.x - (radius/3), point.y - (radius/3), radius, radius);
        } else {
            g.fillOval(point.x - radius, point.y - radius, 2 * radius, 2 * radius);
        }
    }

    Point getPoint() {
        return point;
    }

    public Operation createCopyWithPoint(Point p) {
        return new Operation(p, radius);
    }
}

