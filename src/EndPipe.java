import javafx.scene.layout.GridPane;
import javafx.scene.shape.LineTo;

class EndPipe extends Tile {
    private boolean isVertical;

    EndPipe(boolean isVertical) {
        super("end_" + (isVertical ? "vertical" : "horizontal") + ".png");
        //TODO these values should be reconsidered for balls getting stuck on a previous pipe vs.
        //                                             balls getting stuck INSIDE the pipe.
        this.isStatic = true;
        this.isVertical = isVertical;
        if (isVertical)
            setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
        else
            setValues(0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public LineTo createPath(GridPane mainGrid, int cellIndex) {
        double cellHeight = mainGrid.getHeight() / 4;
        double cellWidth = mainGrid.getWidth() / 4;
        int cellRow = Math.abs(cellIndex / 4);
        int cellColumn = Math.abs(cellIndex % 4);

        if (isVertical) {
            return new LineTo((2 * cellWidth / 2 + cellColumn * cellWidth), (cellHeight / 2 + cellRow * cellHeight));
        }
        else {
            return new LineTo((cellWidth + cellColumn * cellWidth), (cellHeight / 2 + cellRow * cellHeight));
        }
    }
}
