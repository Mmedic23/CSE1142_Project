import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

    int removedFromRow;
    int removedFromColumn;
    private int removedFromIndex;
    GridPane mainGrid = new GridPane();
    ArrayList<Tile> allTiles = new ArrayList<>();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        File level1 = new File("res/level1.txt");
        Scanner levelScanner = null;
        try {
            levelScanner = new Scanner(level1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Pane rootPane = new Pane();

        Group dragGroup = new Group();
        rootPane.getChildren().addAll(mainGrid, dragGroup);
        Scene mainScene = new Scene(rootPane);
        mainScene.setFill(Color.BLACK);
        rootPane.layoutXProperty().bind(mainScene.widthProperty().subtract(mainGrid.widthProperty()).divide(2.0));
        rootPane.layoutYProperty().bind(mainScene.heightProperty().subtract(mainGrid.heightProperty()).divide(2.0));

        int startPipe = 0;
        int endPipe = 0;
        // assert levelScanner != null;
        while (levelScanner.hasNext()) {
            String[] inputArgs = levelScanner.nextLine().split(",");
            int index = (Integer.parseInt(inputArgs[0]) - 1);
            int row = index / 4;
            int column = index % 4;
            Tile tileToAdd;
            ImageView pepe = new ImageView("file:res/pepe.png");
            pepe.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
            pepe.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
            pepe.setPreserveRatio(true);

            if (inputArgs[1].startsWith("Pipe")) {
                boolean isStatic = inputArgs[1].length() > 4;
                if (inputArgs[2].equals("Vertical"))
                    tileToAdd = new VerticalPipe(isStatic);
                else if (inputArgs[2].equals("Horizontal"))
                    tileToAdd = new HorizontalPipe(isStatic);
                else
                    tileToAdd = new BentPipe(inputArgs[2]);
                tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                tileToAdd.setPreserveRatio(true);
                mainGrid.add(tileToAdd, column, row);
            } else if (inputArgs[1].equals("Starter")) {
                tileToAdd = new StartPipe(inputArgs[2].equals("Vertical"));
                //TODO eliminate copy-pasted code
                tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                tileToAdd.setPreserveRatio(true);
                mainGrid.add(tileToAdd, column, row);
                startPipe = row * 4 + column;
            } else if (inputArgs[1].equals("End")) {
                tileToAdd = new EndPipe(inputArgs[2].equals("Vertical"));
                tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                tileToAdd.setPreserveRatio(true);
                mainGrid.add(tileToAdd, column, row);
                endPipe = row * 4 + column;
            } else if (inputArgs[1].equals("Empty")) {
                tileToAdd = new EmptyTile(inputArgs[2].equals("Free"));
                tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                tileToAdd.setPreserveRatio(true);
                mainGrid.add(tileToAdd, column, row);
            } else {
                mainGrid.add(pepe, column, row);
            }
        }

        // START WIP ZONE
        for (Node tile : mainGrid.getChildren()) {
            Tile tileToDrag = (Tile) tile;
            allTiles.add(tileToDrag);
            if (tileToDrag.isStatic)
                continue;
            tileToDrag.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
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
                }
            });
            tileToDrag.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    tileToDrag.setX(mouseEvent.getX() - mainGrid.getWidth() / 8.0);
                    tileToDrag.setY(mouseEvent.getY() - mainGrid.getHeight() / 8.0);
                }
            });
            tileToDrag.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    double cellWidth = mainGrid.getWidth() / 4;
                    double xMargin = (mainScene.getWidth() - mainGrid.getWidth()) / 2.0;
                    double yMargin = (mainScene.getHeight() - mainGrid.getHeight()) / 2.0;
                    int row = (int) ((mouseEvent.getSceneY() - yMargin) / cellWidth);
                    int column = (int) ((mouseEvent.getSceneX() - xMargin) / cellWidth);
                    Tile tileToRemove = null;
                    for (Node tile : mainGrid.getChildren()) {
                        if (GridPane.getRowIndex(tile) == row && GridPane.getColumnIndex(tile) == column) {
                            tileToRemove = (Tile) tile;
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
                    CheckPath(mainGrid);
                }
            });
        }
        // END WIP ZONE
        mainGrid.setAlignment(Pos.CENTER);
        stage.setScene(mainScene);
        stage.show();
    }

    private void CheckPath(GridPane mainGrid) {
        int lastBox = 0;
        int currentBox = 4;

        ArrayList<Integer> pathList = new ArrayList<>();
        pathList.add(0);
        while (true) {
            int diff = currentBox - lastBox;
            int enteredFrom;
            if (diff == 4) {
                enteredFrom = Tile.TOP;
                if(allTiles.get(currentBox) instanceof BentPipe && ((BentPipe) allTiles.get(currentBox)).getType().equals("00"))
                    pathList.add(-currentBox);
                else
                    pathList.add(currentBox);

            } else if (diff == 1) {
                enteredFrom = Tile.LEFT;
                pathList.add(currentBox);

            } else if (diff == -1) {
                enteredFrom = Tile.RIGHT;
                pathList.add(-currentBox);

            } else {
                enteredFrom = Tile.BOTTOM;
                if(allTiles.get(currentBox) instanceof BentPipe && ((BentPipe) allTiles.get(currentBox)).getType().equals("11"))
                    pathList.add(currentBox);
                else
                    pathList.add(-currentBox);

            }

            int moveValue = allTiles.get(currentBox).values[enteredFrom];
            if (moveValue == Integer.MIN_VALUE) {
                currentBox = lastBox;
                break;
            }
            if (moveValue == 0) {
                startAnimation(pathList);
                break;
            }

            lastBox = currentBox;
            currentBox += moveValue;
        }
        System.out.println(currentBox);
    }

    private void startAnimation(ArrayList<Integer> pathList) {
        Path path = new Path();
        double cellHeight = mainGrid.getHeight() / 4;
        double cellWidth = mainGrid.getWidth() / 4;
        int cellRow = Math.abs(pathList.get(0)) / 4;
        int cellColumn = Math.abs(Math.abs(pathList.get(0))) % 4;

        Circle circle = new Circle((cellWidth / 2 + cellColumn * cellWidth), (cellHeight / 2 + cellRow * cellHeight), 10);
        circle.setFill(Color.YELLOW);
        mainGrid.getChildren().add(circle);
        PathTransition pathTransition = new PathTransition();
        path.getElements().add(((StartPipe) allTiles.get(Math.abs(pathList.get(0)))).starterMoveTo(mainGrid, pathList.get(0)));

        for (int i = 0; i < pathList.size(); i++) {
            if (allTiles.get(Math.abs(pathList.get(i))) instanceof StartPipe)
                path.getElements().add(((StartPipe) allTiles.get(Math.abs(pathList.get(i)))).createPath(mainGrid, pathList.get(i)));
            else if (allTiles.get(Math.abs(pathList.get(i))) instanceof VerticalPipe)
                path.getElements().add(((VerticalPipe) allTiles.get(Math.abs(pathList.get(i)))).createPath(mainGrid, pathList.get(i)));
            else if (allTiles.get(Math.abs(pathList.get(i))) instanceof HorizontalPipe)
                path.getElements().add(((HorizontalPipe) allTiles.get(Math.abs(pathList.get(i)))).createPath(mainGrid, pathList.get(i)));
            else if (allTiles.get(Math.abs(pathList.get(i))) instanceof BentPipe)
                path.getElements().add(((BentPipe) allTiles.get(Math.abs(pathList.get(i)))).createPath(mainGrid, pathList.get(i)));
            else
                path.getElements().add(((EndPipe) allTiles.get(Math.abs(pathList.get(i)))).createPath(mainGrid, pathList.get(i)));

        }

        pathTransition.setPath(path);
        pathTransition.setNode(circle);
        pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setDuration(Duration.millis(7000));
        pathTransition.setAutoReverse(true);
        pathTransition.setCycleCount(Timeline.INDEFINITE);
            pathTransition.play();


    }
}
