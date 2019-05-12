import javafx.scene.layout.GridPane;
import javafx.scene.shape.VLineTo;

class VerticalPipe extends Tile {
    VerticalPipe(boolean isStatic) {
        super(isStatic ? "s_vertical.png" : "vertical.png");
        this.isStatic = isStatic;
        setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, +4, -4);
    }

    VLineTo createPath(GridPane mainGrid, int cellIndex) {
        double cellHeight = mainGrid.getHeight() / 4;


        int cellRow = Math.abs(cellIndex / 4);
        if (cellIndex > 0)
            return new VLineTo(((cellRow + 1) * cellHeight));
        else
            return new VLineTo((cellRow * cellHeight));
    }
}
