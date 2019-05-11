import javafx.animation.Transition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.stage.Stage;

import javax.xml.crypto.dsig.TransformService;

//from  w w  w.  ja  v  a  2 s  . co  m
public class dnm2 extends Application {
    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 300, 150);
        stage.setScene(scene);
        stage.setTitle("");
        Circle circle = new Circle(25,25,25, Color.ORANGE);
        Path path = new Path();

        MoveTo moveTo = new MoveTo();
        moveTo.setX(0.0f);
        moveTo.setY(50.0f);

        QuadCurveTo quadTo = new QuadCurveTo();
        quadTo.setControlX(25.0f);
        quadTo.setControlY(0.0f);
        quadTo.setX(50.0f);
        quadTo.setY(50.0f);


        scene.setRoot(root);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}