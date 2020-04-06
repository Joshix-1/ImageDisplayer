package Operations;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RemovingOperations extends Operations {
    static private Color TRANSPARENT = new Color(255, 255, 255, 0);
    private List<RemoveOperation> operationList;

    public RemovingOperations() {
        super();
        operationList = new ArrayList<>();
    }

    public void run(Graphics2D g) {
        if(!canDraw()) return;

        List<Operation> copy = new ArrayList<>(operationList);
        operationList = new ArrayList<>();
        g.setComposite(AlphaComposite.Clear);
        g.setColor(TRANSPARENT);
        super.run(g, copy);
    }

    public void add(Point p, int r) {
        operationList.add(new RemoveOperation(p, r));
    }

    public boolean canDraw() {
        return operationList.size() > 0;
    }

    private class RemoveOperation extends Operation {

        private RemoveOperation(Point p, int radius) {
            super(p, radius);
        }

        @Override
        public RemoveOperation createCopyWithPoint(Point p) {
            return new RemoveOperation(p, radius);
        }
    }
}
