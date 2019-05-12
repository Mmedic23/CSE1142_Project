import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main extends Application {

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
    private boolean nowPlaying = false;
    private double animationDur = 5.0;
    private MediaPlayer gasPlayer;
    private BooleanProperty screenResizable = new SimpleBooleanProperty(true);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        File levelFile = new File(String.format("res/level%d.txt", currentLevel.getValue()));
        Scanner levelScanner;
        try {
            levelScanner = new Scanner(levelFile);
        }
        catch (FileNotFoundException e) {
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

        mainScene.setOnKeyTyped(keyEvent -> {
            if (keyEvent.getCharacter().toLowerCase().equals("g")) {
                if (!nowPlaying) {
                    animationDur = 0.5;
                    Media gasModeMedia = new Media(new File("res/gas.wav").toURI().toString());
                    gasPlayer = new MediaPlayer(gasModeMedia);
                    gasPlayer.play();
                }
                else {
                    animationDur = 5;
                    gasPlayer.stop();
                    gasPlayer = null;
                    nowPlaying = false;
                }
            }
        });

        constructLevel(levelScanner);
        StringProperty gameTitle = new SimpleStringProperty("PLACEHOLDER GAME NAME - Level: ");
        StringProperty movesTitle = new SimpleStringProperty(" Moves: ");
        stage.titleProperty().bind(gameTitle.concat(currentLevel.asString()).concat(movesTitle).concat(numberOfMoves.asString()));
        stage.setScene(mainScene);
        stage.resizableProperty().bindBidirectional(screenResizable);
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
            }
            else if (inputArgs[1].equals("Starter")) {
                tileToAdd = new StartPipe(inputArgs[2].equals("Vertical"));
                startPipe = (row * 4) + column;
            }
            else if (inputArgs[1].equals("End")) {
                tileToAdd = new EndPipe(inputArgs[2].equals("Vertical"));
                endPipe = (row * 4) + column;
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
                for (Node tileChild : mainGrid.getChildren()) {
                    if (GridPane.getRowIndex(tileChild) == row && GridPane.getColumnIndex(tileChild) == column) {
                        tileToRemove = (Tile) tileChild;
                        break;
                    }
                }
                dragGroup.getChildren().remove(tileToDrag);
                if (tileToRemove instanceof EmptyTile && tileToRemove.isStatic &&
                        ((removedFromColumn - column) + (removedFromRow - row) == 1 || (removedFromColumn - column) + (removedFromRow - row) == -1) &&
                        (removedFromRow == row || removedFromColumn == column)) {
                    mainGrid.getChildren().remove(tileToRemove);
                    mainGrid.add(tileToDrag, column, row);
                    removedFromIndex = (row * 4) + column;
                    allTiles.remove(removedFromIndex);
                    allTiles.add(removedFromIndex, tileToDrag);
                    numberOfMoves.setValue(numberOfMoves.add(1).getValue());
                }
                else {
                    removedFromIndex = (removedFromRow * 4) + removedFromColumn;
                    mainGrid.getChildren().removeIf(t -> GridPane.getRowIndex(t) == removedFromRow && GridPane.getColumnIndex(t) == removedFromColumn);
                    mainGrid.add(tileToDrag, removedFromColumn, removedFromRow);
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

        ArrayList<Integer> pathList = new ArrayList<>();
        pathList.add(startPipe);

        while (true) {
            int diff = currentBox - lastBox;
            int enteredFrom;
            if (diff == 4) {
                enteredFrom = Tile.TOP;
                if (allTiles.get(currentBox) instanceof BentPipe && ((BentPipe) allTiles.get(currentBox)).getType().equals("00"))
                    pathList.add(-currentBox);
                else
                    pathList.add(currentBox);
            }
            else if (diff == 1) {
                enteredFrom = Tile.LEFT;
                pathList.add(currentBox);
            }
            else if (diff == -1) {
                enteredFrom = Tile.RIGHT;
                pathList.add(-currentBox);
            }
            else {
                enteredFrom = Tile.BOTTOM;
                if (allTiles.get(currentBox) instanceof BentPipe && ((BentPipe) allTiles.get(currentBox)).getType().equals("11"))
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
                break;
            }

            lastBox = currentBox;
            currentBox += moveValue;
        }
        if (currentBox == endPipe) {
            startAnimation(pathList);
            //levelPassed();
        }
    }

    private void startAnimation(ArrayList<Integer> pathList) {
        Path path = new Path();
        double cellHeight = mainGrid.getHeight() / 4;
        double cellWidth = mainGrid.getWidth() / 4;
        int cellRow = Math.abs(pathList.get(0)) / 4;
        int cellColumn = Math.abs(Math.abs(pathList.get(0))) % 4;

        Circle circle = new Circle((cellWidth / 2 + cellColumn * cellWidth), (cellHeight / 2 + cellRow * cellHeight), 10);
        circle.setFill(Color.YELLOW);
        dragGroup.getChildren().add(circle);
        PathTransition pathTransition = new PathTransition();
        path.getElements().add(((StartPipe) allTiles.get(Math.abs(pathList.get(0)))).starterMoveTo(mainGrid, pathList.get(0)));

        for (Integer pathValue : pathList) {
            if (allTiles.get(Math.abs(pathValue)) instanceof StartPipe)
                path.getElements().add(((StartPipe) allTiles.get(Math.abs(pathValue))).createPath(mainGrid, pathValue));
            else if (allTiles.get(Math.abs(pathValue)) instanceof VerticalPipe)
                path.getElements().add(((VerticalPipe) allTiles.get(Math.abs(pathValue))).createPath(mainGrid, pathValue));
            else if (allTiles.get(Math.abs(pathValue)) instanceof HorizontalPipe)
                path.getElements().add(((HorizontalPipe) allTiles.get(Math.abs(pathValue))).createPath(mainGrid, pathValue));
            else if (allTiles.get(Math.abs(pathValue)) instanceof BentPipe)
                path.getElements().add(((BentPipe) allTiles.get(Math.abs(pathValue))).createPath(mainGrid, pathValue));
            else
                path.getElements().add(((EndPipe) allTiles.get(Math.abs(pathValue))).createPath(mainGrid, pathValue));

        }

        pathTransition.setPath(path);
        pathTransition.setNode(circle);
        pathTransition.setDuration(Duration.seconds(animationDur));
        pathTransition.setCycleCount(1);
        pathTransition.setOnFinished(actionEvent -> {
            //TODO maybe remove the ball and replace the end tile's picture with a finish_tile.png which has the ball on it? Because the ball disappears when the window is resized after the next level button is shown.
            levelPassed();
        });

        for (Node child : mainGrid.getChildren()) {
            child.setOnMousePressed(null);
            child.setOnMouseDragged(null);
            child.setOnMouseReleased(null);
        }
        screenResizable.set(false);
        pathTransition.play();
    }

    private void levelPassed() {
        screenResizable.set(true);
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
            //dragGroup.getChildren().remove(winPane);
            dragGroup.getChildren().clear();
            currentLevel.set(currentLevel.add(1).getValue());
            File nextLevel = new File(String.format("res/level%d.txt", currentLevel.getValue()));
            Scanner levelScanner;
            try {
                levelScanner = new Scanner(nextLevel);
            }
            catch (FileNotFoundException e) {
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
