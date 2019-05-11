import javafx.scene.layout.GridPane;
import javafx.scene.shape.ArcTo;

class BentPipe extends Tile {
    private String pipeType;

    BentPipe(String type) {
        super(type + ".png");
        this.pipeType = type;
        switch (type) {
            case "00":
                setValues(-4, Integer.MIN_VALUE, -1, Integer.MIN_VALUE);
                break;
            case "01":
                setValues(Integer.MIN_VALUE, -4, 1, Integer.MIN_VALUE);
                break;
            case "10":
                setValues(4, Integer.MIN_VALUE, Integer.MIN_VALUE, -1);
                break;
            case "11":
                setValues(Integer.MIN_VALUE, 4, Integer.MIN_VALUE, 1);
                break;
        }
    }

    public ArcTo createPath(GridPane mainGrid, int cellIndex) {
        double cellWidth = mainGrid.getWidth() / 4;
        double cellHeight = mainGrid.getHeight() / 4;
        int cellRow = Math.abs(cellIndex / 4);
        int cellColumn = Math.abs(cellIndex % 4);

        switch (this.pipeType) {
            case "00":
                return new ArcTo(cellWidth * 0.7, cellHeight * 0.7, 0, (cellIndex > 0) ? (cellWidth / 2 + cellColumn * cellWidth) : (cellColumn * cellWidth),
                        (cellIndex < 0) ? (cellHeight / 2 + (cellRow * cellHeight)) : (cellRow * cellHeight), false, cellIndex <= 0);
            case "01":
                return new ArcTo(cellWidth * 0.7, cellHeight * 0.7, 0, (cellIndex < 0) ? (cellWidth / 2 + cellColumn * cellWidth) : 1.5 * cellWidth + (cellColumn * cellWidth),
                        (cellIndex > 0) ? (cellHeight / 2 + cellRow * cellHeight) : (cellRow * cellHeight), false, false);
            case "10":
                return new ArcTo(cellWidth * 0.7, cellHeight * 0.7, 0, (cellIndex > 0) ? (cellWidth + cellColumn * cellWidth) : (cellColumn * cellWidth),
                        (cellIndex > 0) ? (cellHeight + cellRow * cellHeight) : cellHeight / 2 + (cellRow * cellHeight), false, true);
            case "11":
                return new ArcTo(cellWidth * 0.7, cellHeight * 0.7, 0, (cellIndex < 0) ? (cellWidth / 2 + cellColumn * cellWidth) : cellWidth + (cellColumn * cellWidth),
                        (cellIndex > 0) ? (cellHeight / 2 + cellRow * cellHeight) : cellHeight + (cellRow * cellHeight), false, false);
        }
        return null;
    }

    public String getType() {
        return pipeType;
    }
}
