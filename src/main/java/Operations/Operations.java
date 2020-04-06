package Operations;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class Operations {
    private Point lastPoint;

    Operations() {
        lastPoint = null;
    }

    void run(Graphics2D g, List<Operation> copy) {
        copy.forEach(operation -> {
            interpolateAction(lastPoint, operation.getPoint(), point -> operation.createCopyWithPoint(point).doOperation(g));
            operation.doOperation(g);
            lastPoint = operation.getPoint();
        });
        copy.clear();
    }

    private void interpolateAction(Point lp, Point p, Consumer<? super Point> actionConsumer) {
        if(lp == null || p == null) return;
        if(lp.equals(p)) {
            actionConsumer.accept(p);
            return;
        }
        float max_diff = Math.max(Math.abs(lp.x - p.x), Math.abs(lp.y - p.y));
        for (float f = 0; f < 1; f += 1/max_diff) {
            actionConsumer.accept(lerp(lp, p, f));
        }
    }

    private Point lerp(Point p0, Point p1, float t) {
        int newX = (int) ((1 - t) * p0.getX() + t * p1.getX());
        int newY = (int) ((1 - t) * p0.getY() + t * p1.getY());
        return new Point(newX, newY);
    }
}


