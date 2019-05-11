
import javafx.scene.layout.GridPane;
import javafx.scene.shape.*;


class VerticalPipe extends Tile {
    VerticalPipe(boolean isStatic) {
        super(isStatic ? "s_vertical.png" : "vertical.png");
        this.isStatic = isStatic;
        setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, +4, -4);
    }


    public VLineTo createPath(GridPane mainGrid, int cellIndex) {
        double cellHeight = mainGrid.getHeight() / 4;


        int cellRow = Math.abs(cellIndex / 4);
        if (cellIndex > 0)
            return new VLineTo((cellHeight + cellRow * cellHeight));
        else
            return new VLineTo((cellRow * cellHeight));
    }
}
