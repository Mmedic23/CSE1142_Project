import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends Application {

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
        GridPane mainGrid = new GridPane();
        Scene mainScene = new Scene(mainGrid);
        // assert levelScanner != null;
        while (levelScanner.hasNext()) {
            String[] inputArgs = levelScanner.nextLine().split(",");
            int index = (Integer.parseInt(inputArgs[0]) - 1);
            int row = index / 4;
            int column = index % 4;
            Pipe pipeToAdd;
            ImageView pepe = new ImageView("file:res/pepe.png");
            pepe.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
            pepe.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
            pepe.setPreserveRatio(true);
            if (inputArgs[1].startsWith("Pipe")) {
                boolean isStatic = inputArgs[1].length() > 4;
                if (inputArgs[2].equals("Vertical"))
                    pipeToAdd = new VerticalPipe(isStatic);
                else if (inputArgs[2].equals("Horizontal"))
                    pipeToAdd = new HorizontalPipe(isStatic);
                else
                    pipeToAdd = new BentPipe(isStatic, inputArgs[2]);
                pipeToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                pipeToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                pipeToAdd.setPreserveRatio(true);
                mainGrid.add(pipeToAdd, column, row);
            }
            else if (inputArgs[1].equals("Starter")) {
                pipeToAdd = new StartPipe(inputArgs[2].equals("Vertical"));
                //TODO eliminate copy-pasted code
                pipeToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                pipeToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                pipeToAdd.setPreserveRatio(true);
                mainGrid.add(pipeToAdd, column, row);
            }
            else if (inputArgs[1].equals("End")) {
                pipeToAdd = new EndPipe(inputArgs[2].equals("Vertical"));
                pipeToAdd.fitWidthProperty().bind(mainScene.widthProperty().divide(4.0));
                pipeToAdd.fitHeightProperty().bind(mainScene.heightProperty().divide(4.0));
                pipeToAdd.setPreserveRatio(true);
                mainGrid.add(pipeToAdd, column, row);
            }
            else {
                mainGrid.add(pepe, column, row);
            }
        }

        int lastBox = 0;
        int currentBox = 4;
        while (currentBox != 13) {
            int diff = currentBox - lastBox;
            int enteredFrom;
            if (diff == 4)
                enteredFrom = Pipe.TOP;
            else if (diff == 1)
                enteredFrom = Pipe.LEFT;
            else if (diff == -1)
                enteredFrom = Pipe.RIGHT;
            else
                enteredFrom = Pipe.BOTTOM;

            int moveValue = ((Pipe) mainGrid.getChildren().get(currentBox)).values[enteredFrom];
            if (moveValue == 0) {
                currentBox = lastBox;
                break;
            }

            lastBox = currentBox;
            currentBox += moveValue;
        }
        System.out.println(currentBox);
        mainGrid.setAlignment(Pos.CENTER);
        stage.setScene(mainScene);
        stage.show();
    }
}
