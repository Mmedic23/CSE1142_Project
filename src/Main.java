import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

    private final ArrayList<Tile> allTiles = new ArrayList<>(); // final doesn't make the ArrayList contents final, just the reference of the ArrayList final.
    private int removedFromRow;
    private int removedFromIndex;
    private int removedFromColumn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        File level1 = new File("res/level1.txt");
        Scanner levelScanner;
        try {
            levelScanner = new Scanner(level1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Pane rootPane = new Pane();
        GridPane mainGrid = new GridPane();
        Group dragGroup = new Group();
        rootPane.getChildren().addAll(mainGrid, dragGroup);
        Scene mainScene = new Scene(rootPane);
        mainScene.setFill(Color.BLACK);
        rootPane.layoutXProperty().bind(mainScene.widthProperty().subtract(mainGrid.widthProperty()).divide(2.0));
        rootPane.layoutYProperty().bind(mainScene.heightProperty().subtract(mainGrid.heightProperty()).divide(2.0));

        while (levelScanner.hasNext()) {
            String[] inputArgs = levelScanner.nextLine().split(",");
            int index = (Integer.parseInt(inputArgs[0]) - 1);
            int row = index / 4;
            int column = index % 4;
            Tile tileToAdd = null;
            if (inputArgs[1].startsWith("Pipe")) {
                boolean isStatic = inputArgs[1].length() > 4;
                if (inputArgs[2].equals("Vertical"))
                    tileToAdd = new VerticalPipe(isStatic);
                else if (inputArgs[2].equals("Horizontal"))
                    tileToAdd = new HorizontalPipe(isStatic);
                else
                    tileToAdd = new BentPipe(inputArgs[2]);
            }
            else if (inputArgs[1].equals("Starter")) {
                tileToAdd = new StartPipe(inputArgs[2].equals("Vertical"));
            }
            else if (inputArgs[1].equals("End")) {
                tileToAdd = new EndPipe(inputArgs[2].equals("Vertical"));
            }
            else if (inputArgs[1].equals("Empty")) {
                tileToAdd = new EmptyTile(inputArgs[2].equals("Free"));
            }
            // Will not produce NullPointerException as long as level file is in correct format.
            //noinspection ConstantConditions
            tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
            tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
            tileToAdd.setPreserveRatio(true);
            mainGrid.add(tileToAdd, column, row);
        }

        // START WIP ZONE
        for (Node tile : mainGrid.getChildren()) {
            Tile tileToDrag = (Tile) tile;
            allTiles.add(tileToDrag);
            if (tileToDrag.isStatic)
                continue;
            tileToDrag.setOnMousePressed(mouseEvent -> {
                double cellWidth = mainGrid.getWidth() / 4;
                double xMargin = (mainScene.getWidth() - mainGrid.getWidth()) / 2.0;
                double yMargin = (mainScene.getHeight() - mainGrid.getHeight()) / 2.0;
                int row = (int) ((mouseEvent.getSceneY() - yMargin) / cellWidth);
                int column = (int) ((mouseEvent.getSceneX() - xMargin) / cellWidth);
                //System.out.printf("Scene width: %f Scene height: %f\nGrid width: %f Grid height: %f\nX Margin: %f Y Margin: %f\n\n", mainScene.getWidth(), mainScene.getHeight(), mainGrid.getWidth(), mainGrid.getHeight() ,xMargin, yMargin);
                removedFromRow = row;
                removedFromColumn = column;
                EmptyTile emptyTile = new EmptyTile(true);
                emptyTile.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                emptyTile.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                emptyTile.setPreserveRatio(true);

                mainGrid.getChildren().remove(tileToDrag);
                mainGrid.add(emptyTile, column, row);
                dragGroup.getChildren().add(tileToDrag);
                removedFromIndex = (row * 4) + column;
                allTiles.remove(removedFromIndex);
                allTiles.add(removedFromIndex, emptyTile);
            });
            tileToDrag.setOnMouseDragged(mouseEvent -> {
                tileToDrag.setX(mouseEvent.getX() - mainGrid.getWidth() / 8.0);
                tileToDrag.setY(mouseEvent.getY() - mainGrid.getHeight() / 8.0);
            });
            tileToDrag.setOnMouseReleased(mouseEvent -> {
                double cellWidth = mainGrid.getWidth() / 4;
                double xMargin = (mainScene.getWidth() - mainGrid.getWidth()) / 2.0;
                double yMargin = (mainScene.getHeight() - mainGrid.getHeight()) / 2.0;
                int row = (int) ((mouseEvent.getSceneY() - yMargin) / cellWidth);
                int column = (int) ((mouseEvent.getSceneX() - xMargin) / cellWidth);
                Tile tileToRemove = null;
                for (Node tile1 : mainGrid.getChildren()) {
                    if (GridPane.getRowIndex(tile1) == row && GridPane.getColumnIndex(tile1) == column) {
                        tileToRemove = (Tile) tile1;
                        break;
                    }
                }
                dragGroup.getChildren().remove(tileToDrag);
                if (tileToRemove instanceof EmptyTile && tileToRemove.isStatic && ((removedFromColumn - column) + (removedFromRow - row) == 1 || (removedFromColumn - column) + (removedFromRow - row) == -1)) {
                    mainGrid.getChildren().remove(tileToRemove);
                    mainGrid.add(tileToDrag, column, row);
                    removedFromIndex = (row * 4) + column;
                    allTiles.remove(removedFromIndex);
                    allTiles.add(removedFromIndex, tileToDrag);
                } else {
                    mainGrid.add(tileToDrag, removedFromColumn, removedFromRow);
                    removedFromIndex = (removedFromRow * 4) + removedFromColumn;
                    allTiles.remove(removedFromIndex);
                    allTiles.add(removedFromIndex, tileToDrag);
                }
                CheckPath();
            });
        }
        // END WIP ZONE
        mainGrid.setAlignment(Pos.CENTER);
        stage.setScene(mainScene);
        stage.show();
    }

    private void CheckPath() {
        int lastBox = 0;
        int currentBox = 4;
        while (true) {
            int diff = currentBox - lastBox;
            int enteredFrom;
            if (diff == 4)
                enteredFrom = Tile.TOP;
            else if (diff == 1)
                enteredFrom = Tile.LEFT;
            else if (diff == -1)
                enteredFrom = Tile.RIGHT;
            else
                enteredFrom = Tile.BOTTOM;

            int moveValue = allTiles.get(currentBox).values[enteredFrom];
            if (moveValue == Integer.MIN_VALUE) {
                // This value is important for determining where the ball got stuck on.
                //noinspection UnusedAssignment
                currentBox = lastBox;
                break;
            }
            if (moveValue == 0) {
                break;
            }

            lastBox = currentBox;
            currentBox += moveValue;
        }
        //System.out.println(currentBox);
    }
}
