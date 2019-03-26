import javafx.application.Application;
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
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        GridPane mainGrid = new GridPane();
        // assert levelScanner != null;
        while (levelScanner.hasNext()) {
            String[] inputArgs = levelScanner.nextLine().split(",");
            int index = (Integer.parseInt(inputArgs[0]) - 1);
            int row = index / 4;
            int column = index % 4;
            Pipe pipeToAdd;
            ImageView pepe = new ImageView("file:res/pepe.png");
            if (inputArgs[1].equals("Pipe")) {
                if (inputArgs[2].equals("Vertical"))
                    pipeToAdd = new VerticalPipe();
                else if (inputArgs[2].equals("Horizontal"))
                    pipeToAdd = new HorizontalPipe();
                else
                    pipeToAdd = new BentPipe(inputArgs[2]);

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

        Scene mainScene = new Scene(mainGrid);
        stage.setScene(mainScene);
        stage.show();
    }
}
