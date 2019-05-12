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
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main extends Application {

    private final ArrayList<Tile> allTiles = new ArrayList<>(); // final doesn't make the ArrayList contents final, just the reference of the ArrayList final.
    private GridPane mainGrid;
    private Scene mainScene;
    private Group dragGroup;
    private IntegerProperty numberOfMoves = new SimpleIntegerProperty(0);
    private IntegerProperty currentLevel = new SimpleIntegerProperty(1);
    private int totalLevels;
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
        totalLevels = getLevelCount();
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

        mainScene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getText().toLowerCase().equals("g")) {
                if (!nowPlaying) {
                    animationDur = 0.5;
                    Media gasModeMedia = new Media(new File("res/gas.wav").toURI().toString());
                    gasPlayer = new MediaPlayer(gasModeMedia);
                    nowPlaying = true;
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
        StringProperty gameTitle = new SimpleStringProperty("Cringe Pane - Level: ");
        StringProperty movesTitle = new SimpleStringProperty(" Moves: ");
        stage.titleProperty().bind(gameTitle.concat(currentLevel.asString()).concat(movesTitle).concat(numberOfMoves.asString()));
        stage.setScene(mainScene);
        stage.resizableProperty().bindBidirectional(screenResizable);
        stage.show();
    }

    // This method constructs the level, using input from the Scanner object provided.
    private void constructLevel(Scanner levelScanner) {
        while (levelScanner.hasNext()) {
            // Split the line into proper arguments of Index, Tile Type and Tile Sub-type
            String[] inputArgs = levelScanner.nextLine().split(",");
            // Ignore empty lines, if there are any.
            if (inputArgs[0].isBlank()) {
                continue;
            }
            // First argument is always the index of the tile.
            int index = (Integer.parseInt(inputArgs[0]) - 1);
            // Row and column can be calculated in a 4x4 grid as follows:
            int row = index / 4;
            int column = index % 4;
            // We define a Tile tileToAdd, which will be the tile that is read from the input file and added to the grid
            Tile tileToAdd = null;
            // This whole if-else block constructs the appropriate pipe for the given input.
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
            // The comment below is a message to IntelliJ IDEA, it tells the IDE to ignore the possible NullPointerException here, as the statement
            // will not produce NullPointerException as long as level file is in correct format, i.e. doesn't have any non-existing tile types written in it.
            //noinspection ConstantConditions
            tileToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
            tileToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
            tileToAdd.setPreserveRatio(true);
            // After binding the tile's size to the proper values, we add it to the GridPane.
            mainGrid.add(tileToAdd, column, row);
        }

        // Add event handlers for dynamic tiles, to enable drag and drop features.
        // Also add each tile to an array for later use in checking whether the level was solved or not.
        for (Node tile : mainGrid.getChildren()) {
            Tile tileToDrag = (Tile) tile;
            allTiles.add(tileToDrag);
            // If the tile is a static tile, it cannot move and therefore does not need event handlers.
            if (tileToDrag.isStatic)
                continue;

            tileToDrag.setOnMousePressed(mouseEvent -> {
                // Here, we calculate on which row and column the mouse is hovering above.
                // Since our stage is resizable, we need to account for changes in sizes and empty spaces in our scene.
                double cellWidth = mainGrid.getWidth() / 4;
                double xMargin = (mainScene.getWidth() - mainGrid.getWidth()) / 2.0;
                double yMargin = (mainScene.getHeight() - mainGrid.getHeight()) / 2.0;
                int row = (int) ((mouseEvent.getSceneY() - yMargin) / cellWidth);
                int column = (int) ((mouseEvent.getSceneX() - xMargin) / cellWidth);
                // We record the row, column and the index the tile was picked up from, in case we need to put it back to its original spot in the grid.
                removedFromRow = row;
                removedFromColumn = column;
                removedFromIndex = (row * 4) + column;
                // We create a new empty tile, to put in the place of the tile that is now being dragged by the user.
                EmptyTile emptyTile = new EmptyTile(true);
                emptyTile.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                emptyTile.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                emptyTile.setPreserveRatio(true);

                // Since GridPane doesn't allow it's children to move freely and locks them into its cells, we need to work around it.
                // We handle dragging by removing the tile from the GridPane, adding it to a Group that was added on top of the GridPane.
                // A Group doesn't limit it's children's X-Y or size values.
                mainGrid.getChildren().remove(tileToDrag);
                mainGrid.add(emptyTile, column, row);
                dragGroup.getChildren().add(tileToDrag);
                allTiles.remove(removedFromIndex);
                // We also add the emptyTile we have created previously.
                allTiles.add(removedFromIndex, emptyTile);
            });

            // Here, we simply set the tile the user is dragging to follow the user's mouse.
            // We subtract the mainGrid.getWidth() / 8.0 value, which corresponds to the cell width value divided by two, to make the tile snap its center to the mouse, instead of its top-left corner.
            // This is simply an aesthetic choice.
            tileToDrag.setOnMouseDragged(mouseEvent -> {
                tileToDrag.setX(mouseEvent.getX() - mainGrid.getWidth() / 8.0);
                tileToDrag.setY(mouseEvent.getY() - mainGrid.getHeight() / 8.0);
            });

            tileToDrag.setOnMouseReleased(mouseEvent -> {
                // Here, we calculate on which row and column the mouse is hovering above.
                // Since our stage is resizable, we need to account for changes in sizes and empty spaces in our scene.
                double cellWidth = mainGrid.getWidth() / 4;
                double xMargin = (mainScene.getWidth() - mainGrid.getWidth()) / 2.0;
                double yMargin = (mainScene.getHeight() - mainGrid.getHeight()) / 2.0;
                int row = (int) ((mouseEvent.getSceneY() - yMargin) / cellWidth);
                int column = (int) ((mouseEvent.getSceneX() - xMargin) / cellWidth);
                // Here, we look through each tile in our grid to find the one that the mouse is currently hovering over.
                // We need to do this by row index and column index, because as we add and remove elements to the mainGrid, the elements are added to the end of the children of mainGrid.
                // This makes it impossible to simply use getChildren.get(index), as the contents of getChildren() don't follow the same order as the one visible onscreen.
                Tile tileToRemove = null;
                for (Node tileChild : mainGrid.getChildren()) {
                    if (GridPane.getRowIndex(tileChild) == row && GridPane.getColumnIndex(tileChild) == column) {
                        tileToRemove = (Tile) tileChild;
                        break;
                    }
                }
                // Since the user let go of the tile, we will put the tile back into the grid.
                dragGroup.getChildren().remove(tileToDrag);
                if (tileToRemove instanceof EmptyTile && tileToRemove.isStatic &&
                        ((removedFromColumn - column) + (removedFromRow - row) == 1 || (removedFromColumn - column) + (removedFromRow - row) == -1) &&
                        (removedFromRow == row || removedFromColumn == column)) {
                    // If the tile the mouse was hovering over satisfies these conditions, then we can safely remove the tile below the mouse and replace it with the hovering tile.
                    // This corresponds to a valid and successful move.
                    mainGrid.getChildren().remove(tileToRemove);
                    mainGrid.add(tileToDrag, column, row);
                    removedFromIndex = (row * 4) + column;
                    allTiles.remove(removedFromIndex);
                    allTiles.add(removedFromIndex, tileToDrag);
                    numberOfMoves.setValue(numberOfMoves.add(1).getValue());
                }
                else {
                    // If hover, the tile doesn't satisfy the conditions, we snap the hovering tile back to its original place
                    // This corresponds to an invalid and unsuccessful move.
                    removedFromIndex = (removedFromRow * 4) + removedFromColumn;
                    mainGrid.getChildren().removeIf(t -> GridPane.getRowIndex(t) == removedFromRow && GridPane.getColumnIndex(t) == removedFromColumn);
                    mainGrid.add(tileToDrag, removedFromColumn, removedFromRow);
                    allTiles.remove(removedFromIndex);
                    allTiles.add(removedFromIndex, tileToDrag);
                }
                // After each move, we check if the path has been completed. If it has, the method will also run the animation and show the dialogue to start the next level.
                checkPath();
            });
        }
    }

    // This method checks if there's a path starting from the starter pipe and reaching to the end pipe.
    // It also handles starting the ball animation.
    // The main logic behind this method, is that a 4x4 grid can be traversed item by item, using 4 different values, and a list of items on that grid.
    // Basically, say we are on (0,0) and want to move 1 block down. We would start from index 0 and add +4 to the index, to reach index 4, which is (0,1), one block down from (0,0)
    // The valid directions and the corresponding movement values are:
    // Left: -1 || Right: +1 || Up: -4 || Down: +4
    private void checkPath() {
        // We start from the startPipe.
        int lastBox = startPipe;
        // And as a first step, move to the next pipe, determined by whether the starter pipe is horizontal or vertical.
        int currentBox = startPipe + (allTiles.get(lastBox).values[Tile.TOP] == 4 ? 4 : -1);
        // Each index is added to pathList to later play the animation.
        ArrayList<Integer> pathList = new ArrayList<>();
        pathList.add(startPipe);

        // Each pipe can move a ball to two different indexes. For example: a horizontal pipe can move the ball one tile to the left, or one tile to the left, depending on where the ball has entered the horizontal pipe.
        // We use the values defined inside each Pipe class to get in which direction the ball should move.
        // Since we also need to know which side the ball entered the pipe from, we hold 2 indices, and the difference between them gives us the correct answer.
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
            // If moveValue turns out to be Integer.MIN_VALUE, this means the ball has entered a tile in an invalid way (e.g. entered from the top of a horizontal pipe), thus, we send the ball back where it came from.
            if (moveValue == Integer.MIN_VALUE) {
                currentBox = lastBox;
                break;
            }
            // If moveValue is 0, the ball has reached a closed end, such as an end pipe.
            if (moveValue == 0) {
                break;
            }

            lastBox = currentBox;
            currentBox += moveValue;
        }
        if (currentBox == endPipe) {
            // The ball can successfully travel the level from start to end, play the animation.
            startAnimation(pathList);
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

        // We also lock every tile in the mainGrid in place, to prevent duplicate animations from playing back to back.
        for (Node child : mainGrid.getChildren()) {
            child.setOnMousePressed(null);
            child.setOnMouseDragged(null);
            child.setOnMouseReleased(null);
        }
        // We set the screenResizable property as false, because if the window is resized while the animation is playing, the animation path will not update.
        screenResizable.set(false);
        pathTransition.play();
    }

    // This is a simple method to calculate the number of levels found in the res/ directory.
    // It is used to decide when to switch to generating random levels.
    private int getLevelCount() {
        File resDir = new File("res/");
        File[] allLevels = resDir.listFiles((dir, name) -> name.endsWith(".txt"));
        return (allLevels != null) ? allLevels.length : 0;
    }

    // This method is called when the ball animation finishes, and shows a win message and a next level button on the screen.
    // If the player has passed the last level, it informs the player that they have won the game, and asks if they'd like to continue playing with randomly generated levels.
    private void levelPassed() {
        // Since our stage is resizable, we bind every object's size and X-Y values.

        // We create our main pane which will hold every other object.
        Pane winPane = new Pane();
        winPane.prefHeightProperty().bind(mainGrid.heightProperty());
        winPane.prefWidthProperty().bind(mainGrid.widthProperty());

        // This is the background image for the win panel.
        ImageView winBg = new ImageView("file:res/diamond_block.png");
        winBg.fitHeightProperty().bind(mainGrid.heightProperty().divide(1.25));
        winBg.fitWidthProperty().bind(mainGrid.widthProperty().divide(1.25));
        winBg.xProperty().bind(winPane.prefWidthProperty().divide(2.0).subtract(winBg.fitWidthProperty().divide(2.0)));
        winBg.yProperty().bind(winPane.prefWidthProperty().divide(2.0).subtract(winBg.fitHeightProperty().divide(2.0)));

        // This is the win message itself.
        Label winMsg = new Label();
        winMsg.setTextAlignment(TextAlignment.CENTER);
        // We set its font size relative to the pane's size. Unfortunately, we can't bind just the font size property of a Text object in JavaFX.
        winMsg.setStyle("-fx-font-size: " + winPane.getPrefHeight() / 28.0 + "px");
        // We bind the winMsg's CENTER in the middle of the winPane, but...
        winMsg.translateXProperty().bind(
                winPane.translateXProperty().add(
                        winPane.widthProperty().divide(2.0)
                ).subtract(
                        winMsg.widthProperty().divide(2.0)
                )
        );
        // ...here, we bind it's Y value to have winMsg's CENTER slightly higher than the exact middle point.
        winMsg.translateYProperty().bind(
                winPane.translateYProperty().add(
                        winPane.heightProperty().divide(2.0)
                ).subtract(
                        winMsg.heightProperty().divide(1.5)
                )
        );

        // This is the button that lets the player continue the game with new levels.
        Button nextLevelBtn = new Button();
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
        nextLevelBtn.setStyle("-fx-font-size: " + winPane.getPrefHeight() / 28.0 + "px; -fx-background-color: transparent; -fx-background-image: url('file:res/button_normal.png'); -fx-background-size: 100% 100%;");

        // If the player hasn't completed every level yet, congratulate them, show them their move count and let them move on to the next level.
        if (currentLevel.getValue() != totalLevels) {
            if (numberOfMoves.getValue() != 1)
                winMsg.setText(String.format("Congratulations!\nYou won with %d moves!", numberOfMoves.getValue()));
            else
                winMsg.setText("Congratulations!\nYou won with a single move!");

            nextLevelBtn.setText("Next Level");
            nextLevelBtn.setOnMousePressed(mouseEvent -> {
                // Construct the new level when the user clicks next level
                screenResizable.set(true);
                // Remove everything (winPane) from the Group
                dragGroup.getChildren().clear();
                // Increment the level counter
                currentLevel.set(currentLevel.add(1).getValue());
                // Setup the file and scanner for the next level.
                File nextLevel = new File(String.format("res/level%d.txt", currentLevel.getValue()));
                Scanner levelScanner;
                try {
                    levelScanner = new Scanner(nextLevel);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                // Clear the grid and the allTiles ArrayList
                mainGrid.getChildren().removeAll(mainGrid.getChildren());
                allTiles.clear();
                // Reset the move counter
                numberOfMoves.setValue(0);
                // And finally construct the new level.
                constructLevel(levelScanner);
            });
        }
        // If the player has completed all the levels in the res directory, congratulate them and ask whether they'd like to continue with randomly generated levels.
        else {
            winMsg.setText("Congratulations! You won the game!\nIf you want to, you can\ncontinue with randomly generated levels.");

            nextLevelBtn.setText("Continue");
            nextLevelBtn.setOnMousePressed(mouseEvent -> {
                screenResizable.set(true);
                dragGroup.getChildren().clear();
                mainGrid.getChildren().removeAll(mainGrid.getChildren());
                allTiles.clear();
                numberOfMoves.setValue(0);
                // The random level generator is called using two random values, difficulty and length.
                Scanner randomLevelScanner = randomLevel((int) ((Math.random() * 2) + 1), (int) ((Math.random() * 6) + 7));
                // The random level generator is not perfect, and can sometimes fail to generate a valid level. In such cases, it returns null.
                // We work around the issue by calling the method again if it returns null. Since the levels are random, it eventually generates a valid level.
                while (randomLevelScanner == null) {
                    randomLevelScanner = randomLevel((int) ((Math.random() * 2) + 1), (int) ((Math.random() * 6) + 7));
                }
                constructLevel(randomLevelScanner);
            });
        }
        nextLevelBtn.setOnMouseEntered(mouseEvent -> nextLevelBtn.setStyle("-fx-font-size: " + winPane.getPrefHeight() / 28.0 + "px; -fx-background-color: transparent; -fx-background-image: url('file:res/button_hover.png'); -fx-background-size: 100% 100%;"));
        nextLevelBtn.setOnMouseExited(mouseEvent -> nextLevelBtn.setStyle("-fx-font-size: " + winPane.getPrefHeight() / 28.0 + "px; -fx-background-color: transparent; -fx-background-image: url('file:res/button_normal.png'); -fx-background-size: 100% 100%;"));
        // In the end, we add everything to the win pane
        winPane.getChildren().addAll(winBg, nextLevelBtn, winMsg);
        // And add the win pane, to the drag group.
        // We add them to the drag group simply out of convenience. The win pane will not be dragged inside the dragGroup.
        dragGroup.getChildren().add(winPane);
    }

    // This method generates a random level, and returns it as a Scanner object.
    // The method is not perfect, and can sometimes fail to generate a valid, solvable level. In such cases it will return null.
    // The basic method it generates a level is as follows:
    // 1. Select a random starting point.
    // 2. If available, select a random direction for the start pipe, if not, such as when against the edge of the grid, select the only available direction.
    // 3. Move 1 tile to a random direction, which doesn't already have a tile placed in it
    // 4. Repeat 3 until length is reached.
    // It then finds the appropriate pipe types, such as horizontal, vertical, bent, starter or end, based on the index that comes before and after each pipe.
    // It also fills all empty spaces with Empty,none tiles.
    // Then, it removes some Empty,none tiles, and shuffles the pipes to random valid directions.
    // IMPORTANT! The removal of empty tiles and the shuffling is not implemented yet.
    private Scanner randomLevel(int difficulty, int length) {
        Random rnd = new Random();
        StringBuilder level = new StringBuilder();
        // pipePath holds the correct path to solve the level.
        ArrayList<Integer> pipePath = new ArrayList<>();
        // pipeTypes holds every tile on the grid, in format Index,Type,Sub-type
        ArrayList<String> pipeTypes = new ArrayList<>(16);
        // We first populate pipeTypes with empty strings, so we can set the values in a non-specific order.
        for (int i = 0; i < 16; i++)
            pipeTypes.add("");

        // We select the random starting point.
        int startIndex = rnd.nextInt(16);
        // Since the start pipe only has two directions, index 12 is an invalid index for the starter pipe.
        while (startIndex == 12) {
            startIndex = rnd.nextInt(16);
        }
        int startColumn = startIndex % 4;
        int startRow = startIndex / 4;

        // Here, we determine whether the start pipe should be vertical or horizontal.
        // If there are no limitations, then the value is selected at random.
        boolean startVertical;
        if (startRow == 3) {
            startVertical = false;
        }
        else if (startColumn == 0) {
            startVertical = true;
        }
        else {
            startVertical = rnd.nextBoolean();
        }

        // We add the starter pipe's index, and the index that should follow it based on the starter's direction, to the pipePath
        pipePath.add(startIndex);
        if (startVertical) {
            pipePath.add(startIndex + 4);
        }
        else {
            pipePath.add(startIndex - 1);
        }

        // Then we move block by block in random directions, until length is reached.
        for (int i = 2; i < length; i++) {
            int currentIndex = pipePath.get(i - 1);
            int currentColumn = currentIndex % 4;
            int currentRow = currentIndex / 4;
            // possibleDirections is defined and items are removed from it based on limitations such as grid edges.
            // Then, a direction is picked at random from the list.
            ArrayList<Integer> possibleDirections = new ArrayList<>(Arrays.asList(-1, +1, -4, +4));
            if (currentColumn == 0) { // e.g. if we are at the leftmost column, we cannot move further to the left
                possibleDirections.removeIf(n -> n == -1);
            }
            else if (currentColumn == 3) {
                possibleDirections.removeIf(n -> n == +1);
            }
            if (currentRow == 0) {
                possibleDirections.removeIf(n -> n == -4);
            }
            else if (currentRow == 3) {
                possibleDirections.removeIf(n -> n == +4);
            }
            if (i == length - 1) { // Because the end pipe can only be entered from the left (+1) or from the bottom (-4), we remove the invalid directions going into the last index.
                possibleDirections.removeIf(n -> n == -1 || n == +4);
            }
            else { // This check happens to prevent the level generator from getting stuck in the top right corner, where the end pipe cannot be placed anywhere.
                if (currentIndex == 2) {
                    possibleDirections.removeIf(n -> n == +1);
                }
                if (currentIndex == 7) {
                    possibleDirections.removeIf(n -> n == -4);
                }
            }
            int moveValue = possibleDirections.get(rnd.nextInt(possibleDirections.size()));
            int nextIndex = currentIndex + moveValue;
            // Here we check if the index we're trying to move to has already been passed through. If it has, we remove the invalid direction leading into the already existing pipe, and re-select a random direction.
            while (pipePath.contains(nextIndex)) {
                possibleDirections.remove((Integer) moveValue);
                // If there are no more directions that we can move to, the level generator got stuck and has failed.. We return null.
                if (possibleDirections.size() == 0) {
                    return null;
                }
                moveValue = possibleDirections.get(rnd.nextInt(possibleDirections.size()));
                nextIndex = currentIndex + moveValue;
            }
            pipePath.add(currentIndex + moveValue);
        }

        // Then we loop 16 times, one for each tile in the 4x4 grid, to determine the correct type for each pipe and fill the empty spaces with Empty tiles.
        // We determine the type of the pipe by looking at the index differences between the current index, and those that come before and after the current index.
        for (int tileIndex = 0; tileIndex < 16; tileIndex++) {
            if (pipePath.contains(tileIndex)) {
                // There's a pipe at current tileIndex
                int beforeIndex = pipePath.indexOf(tileIndex) - 1;
                if (beforeIndex < 0) {
                    // There is no pipe that comes before the current one, therefore, the current one is the starter.
                    pipeTypes.set(tileIndex, (tileIndex + 1) + ",Starter," + (startVertical ? "Vertical" : "Horizontal"));
                    continue;
                }
                int beforeDiff = tileIndex - pipePath.get(beforeIndex);

                int afterIndex = pipePath.indexOf(tileIndex) + 1;
                if (afterIndex >= pipePath.size()) {
                    // There is no pipe that comes after the current one, therefore, the current one is the end pipe.
                    pipeTypes.set(tileIndex, (tileIndex + 1) + ",End," + ((beforeDiff == 1) ? "Horizontal" : "Vertical"));
                    continue;
                }
                int afterDiff = tileIndex - pipePath.get(afterIndex);
                if (beforeDiff + afterDiff == 0) { // This always means the current pipe is a straight pipe.
                    if (Math.abs(beforeDiff) == 4) {
                        pipeTypes.set(tileIndex, (tileIndex + 1) + ",Pipe,Vertical");
                        continue;
                    }
                    else {
                        pipeTypes.set(tileIndex, (tileIndex + 1) + ",Pipe,Horizontal");
                        continue;
                    }
                }
                // The current pipe is a bent pipe. To determine the sub-type, we change our difference definitions from before and after to vertical and horizontal.
                // Using these differences, we can easily find out which type of bent pipe is supposed to be at the current index.
                int verticalDiff, horizontalDiff;
                String bentType = (tileIndex + 1) + ",Pipe,";
                if (beforeDiff == 4 || beforeDiff == -4) {
                    verticalDiff = beforeDiff;
                    horizontalDiff = afterDiff;
                }
                else {
                    verticalDiff = afterDiff;
                    horizontalDiff = beforeDiff;
                }
                bentType += (verticalDiff < 0) ? "1" : "0";
                bentType += (horizontalDiff < 0) ? "1" : "0";
                pipeTypes.set(tileIndex, bentType);

            }
            else {
                pipeTypes.set(tileIndex, (tileIndex + 1) + ",Empty,none");
            }
        }

        // Finally, we write all the pipe types as strings, into the level String, then we pass it to the Scanner's constructor.
        for (String pipeString : pipeTypes) {
            level.append(pipeString).append("\n");
        }
        return new Scanner(level.toString());
    }
}
