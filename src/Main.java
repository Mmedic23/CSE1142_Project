import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

    //TODO There's a bug where a box can be moved onto a pipe, if the pipe is moved to an invalid position beforehand.
    //TODO There's a bug where any block can be moved in an L shape

    private final ArrayList<Tile> allTiles = new ArrayList<>(); // final doesn't make the ArrayList contents final, just the reference of the ArrayList final.
    private GridPane mainGrid;
    private Scene mainScene;
    private Group dragGroup;
    private IntegerProperty numberOfMoves = new SimpleIntegerProperty(0);
    private IntegerProperty currentLevel = new SimpleIntegerProperty(1);
    private int removedFromRow;
    private int removedFromIndex;
    private int removedFromColumn;
    private int startPipe;
    private int endPipe;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        File levelFile = new File("res/level1.txt");
        Scanner levelScanner;
        try {
            levelScanner = new Scanner(levelFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        // Set up the root panes and center them in the scene.
        stage.setHeight(500);
        stage.setWidth(475);
        Pane rootPane = new Pane();
        mainGrid = new GridPane();
        dragGroup = new Group();
        rootPane.getChildren().addAll(mainGrid, dragGroup);
        mainScene = new Scene(rootPane);
        rootPane.layoutXProperty().bind(mainScene.widthProperty().subtract(mainGrid.widthProperty()).divide(2.0));
        rootPane.layoutYProperty().bind(mainScene.heightProperty().subtract(mainGrid.heightProperty()).divide(2.0));

        // TODO some good styling should be written in place of this line
        mainScene.setFill(Color.BLACK);

        constructLevel(levelScanner);
        StringProperty gameTitle = new SimpleStringProperty("PLACEHOLDER GAME NAME - Level: ");
        StringProperty movesTitle = new SimpleStringProperty(" Moves: ");
        stage.titleProperty().bind(gameTitle.concat(currentLevel.asString()).concat(movesTitle).concat(numberOfMoves.asString()));
        stage.setScene(mainScene);
        stage.show();
    }

    private void constructLevel(Scanner levelScanner) {
        // Construct the level using input from the txt file specified.
        while (levelScanner.hasNext()) {
            String[] inputArgs = levelScanner.nextLine().split(",");
            if (inputArgs[0].isBlank()) {
                continue;
            }
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
            } else if (inputArgs[1].equals("Starter")) {
                tileToAdd = new StartPipe(inputArgs[2].equals("Vertical"));
                startPipe = (row * 4) + column;
            } else if (inputArgs[1].equals("End")) {
                tileToAdd = new EndPipe(inputArgs[2].equals("Vertical"));
                endPipe = (row * 4) + column;
            } else if (inputArgs[1].equals("Empty")) {
                tileToAdd = new EmptyTile(inputArgs[2].equals("Free"));
            }
            // Will not produce NullPointerException as long as level file is in correct format.
            //noinspection ConstantConditions
            tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
            tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
            tileToAdd.setPreserveRatio(true);
            mainGrid.add(tileToAdd, column, row);
        }

        // Add event handlers for dynamic tiles, to enable drag and drop features.
        // Also add each tile to an array for later use in level passed creation.
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
                    numberOfMoves.setValue(numberOfMoves.add(1).getValue());
                } else {
                    mainGrid.add(tileToDrag, removedFromColumn, removedFromRow);
                    removedFromIndex = (removedFromRow * 4) + removedFromColumn;
                    allTiles.remove(removedFromIndex);
                    allTiles.add(removedFromIndex, tileToDrag);
                }
                checkPath();
            });
        }
    }

    private void checkPath() {
        int lastBox = startPipe;
        int currentBox = startPipe + (allTiles.get(lastBox).values[Tile.TOP] == 4 ? 4 : -1);
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
                currentBox = lastBox;
                break;
            }
            if (moveValue == 0) {
                break;
            }

            lastBox = currentBox;
            currentBox += moveValue;
        }
        if (currentBox == endPipe) {
            levelPassed();
        }
    }

    private void levelPassed() {
        Pane winPane = new Pane();
        winPane.prefHeightProperty().bind(mainGrid.heightProperty());
        winPane.prefWidthProperty().bind(mainGrid.widthProperty());

        ImageView winBg = new ImageView("file:res/diamond_block.png");
        winBg.fitHeightProperty().bind(mainGrid.heightProperty().divide(1.5));
        winBg.fitWidthProperty().bind(mainGrid.widthProperty().divide(1.5));
        winBg.xProperty().bind(winPane.prefWidthProperty().divide(2.0).subtract(winBg.fitWidthProperty().divide(2.0)));
        winBg.yProperty().bind(winPane.prefWidthProperty().divide(2.0).subtract(winBg.fitHeightProperty().divide(2.0)));

        Label winMsg = new Label();
        winMsg.setTextAlignment(TextAlignment.CENTER);
        if (numberOfMoves.getValue() != 1)
            winMsg.setText(String.format("Congratulations!\nYou won with %d moves!", numberOfMoves.getValue()));
        else
            winMsg.setText("Congratulations!\nYou won with a single move!");
        winMsg.setStyle("-fx-font-size: 16px");
        winMsg.translateXProperty().bind(
                winPane.translateXProperty().add(
                        winPane.widthProperty().divide(2.0)
                ).subtract(
                        winMsg.widthProperty().divide(2.0)
                )
        );
        winMsg.translateYProperty().bind(
                winPane.translateYProperty().add(
                        winPane.heightProperty().divide(2.0)
                ).subtract(
                        winMsg.heightProperty().divide(2.0)
                )
        );

        Button nextLevelBtn = new Button("Next Level ->");
        nextLevelBtn.prefHeightProperty().bind(winPane.heightProperty().divide(20.0));
        nextLevelBtn.prefWidthProperty().bind(winPane.widthProperty().divide(2.0));
        nextLevelBtn.translateXProperty().bind(
                winPane.translateXProperty().add(
                        winPane.prefWidthProperty().divide(2.0)
                ).subtract(
                        nextLevelBtn.widthProperty().divide(2.0)
                )
        );
        nextLevelBtn.translateYProperty().bind(
                winPane.translateYProperty().add(
                        winPane.prefHeightProperty().divide(2.0)
                ).subtract(
                        nextLevelBtn.heightProperty().divide(2.0)
                ).add(
                        winPane.prefHeightProperty().divide(8.0)
                )
        );
        nextLevelBtn.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-background-image: url('file:res/button_normal.png'); -fx-background-size: 100% 100%;");
        nextLevelBtn.setOnMousePressed(mouseEvent -> {
            // Construct the new level
            dragGroup.getChildren().remove(winPane);
            currentLevel.set(currentLevel.add(1).getValue());
            File nextLevel = new File(String.format("res/level%d.txt", currentLevel.getValue()));
            Scanner levelScanner;
            try {
                levelScanner = new Scanner(nextLevel);
            } catch (FileNotFoundException e) {
                //TODO Ask the user if they wish to continue with random levels.
                e.printStackTrace();
                return;
            }
            mainGrid.getChildren().removeAll(mainGrid.getChildren());
            allTiles.clear();
            numberOfMoves.setValue(0);
            constructLevel(levelScanner);
        });
        nextLevelBtn.setOnMouseEntered(mouseEvent -> nextLevelBtn.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-background-image: url('file:res/button_hover.png'); -fx-background-size: 100% 100%;"));
        nextLevelBtn.setOnMouseExited(mouseEvent -> nextLevelBtn.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-background-image: url('file:res/button_normal.png'); -fx-background-size: 100% 100%;"));

        winPane.getChildren().addAll(winBg, nextLevelBtn, winMsg);
        dragGroup.getChildren().add(winPane);
    }
}
