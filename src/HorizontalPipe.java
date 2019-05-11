
import javafx.scene.layout.GridPane;
import javafx.scene.shape.*;

class HorizontalPipe extends Tile {
    HorizontalPipe(boolean isStatic) {
        super(isStatic ? "s_horizontal.png" : "horizontal.png");
        this.isStatic = isStatic;
        setValues(+1, -1, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public HLineTo createPath(GridPane mainGrid, int cellIndex) {
        double cellWidth = mainGrid.getWidth()/4;
        int cellColumn = Math.abs(cellIndex % 4);


        if(cellIndex>0)
            return new HLineTo((cellWidth + cellColumn * cellWidth));
        else
            return new HLineTo(cellColumn * cellWidth);
    }
}
