import javafx.scene.layout.GridPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;


public class StartPipe extends Tile {
    boolean isVertical;

    StartPipe(boolean isVertical) {
        super("start_" + (isVertical ? "vertical" : "horizontal") + ".png");
        this.isStatic = true;
        this.isVertical = isVertical;
        if (isVertical)
            setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, 4, Integer.MIN_VALUE);
        else
            setValues(Integer.MIN_VALUE, -1, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public MoveTo starterMoveTo(GridPane mainGrid, int cellIndex) {

        double cellHeight = mainGrid.getHeight() / 4;
        double cellWidth = mainGrid.getWidth() / 4;
        int cellRow = Math.abs(cellIndex / 4);
        int cellColumn = Math.abs(cellIndex % 4);

        return new MoveTo((cellWidth  + cellColumn * cellWidth),(cellHeight / 2 + cellRow * cellHeight));
    }
    public LineTo createPath(GridPane mainGrid, int cellIndex) {
        double cellHeight = mainGrid.getHeight() / 4;
        double cellWidth = mainGrid.getWidth() / 4;
        int cellRow = cellIndex / 4;
        int cellColumn = cellIndex % 4;

        if (isVertical) {
            return new LineTo((cellWidth   + cellColumn * cellWidth), (cellHeight + cellRow * cellHeight) ); // Don't know why x property starts with cellWidth
        } else {
            return new LineTo(cellColumn * cellWidth , (cellHeight / 2 + cellRow * cellHeight));
        }
    }
}
