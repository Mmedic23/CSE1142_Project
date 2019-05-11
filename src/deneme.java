
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class deneme extends Application {
    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        // Create a pane
        Pane pane = new Pane();

        // Create a circle
        Circle circle = new Circle(125, 100, 10);
        circle.setFill(Color.RED);
        circle.setStroke(Color.BLACK);

        //Add line
        Line lin2 = new Line(125,250,125,400);
        Line line = new Line(125,5,125,250);
        lin2.setFill(Color.BLACK);
        line.setFill(Color.BLACK);


        pane.getChildren().add(circle);
        pane.getChildren().add(line);
        ArcTo arc = new ArcTo(125,125,0,250,375,false,false);
        ArcTo arcTo = new ArcTo(125,125,0,250,125,false,false);
        arcTo.setX(50.0);
        arcTo.setY(50.0);
        arcTo.setRadiusX(125.0);
        arcTo.setRadiusY(125.0);
        //arcTo.setSweepFlag(false);
        //arcTo.setLargeArcFlag(false);




        // Create a path transition


        PathTransition pt2 = new PathTransition();
        PathTransition pt = new PathTransition();
        Path path = new Path();
        //SequentialTransition sequentialTransition = new SequentialTransition(pt,pt2);



        path.setFill(Color.BLACK);
        path.setStroke(Color.BLACK);

        pt.setDuration(Duration.millis(2000));

        pt.setNode(circle);
        pt.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        LineTo lineTo2 = new LineTo(125,250);
        LineTo lineTo1 = new LineTo(125,400);
        path.getElements().add(new MoveTo(125,0));
        path.getElements().add(lineTo2);
        path.getElements().add(arc);
        path.getElements().add(arcTo);
        pt.setPath(path);
        pt.setAutoReverse(true);
        pt.setCycleCount(Timeline.INDEFINITE);
        pt.play();
        //pt.setCycleCount(Timeline.INDEFINITE);
        //pt.setAutoReverse(true);
        pt2.setDuration(Duration.millis(2000));
        pt2.setPath(path);
        pt2.setNode(circle);
        pt2.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        //pt2.setCycleCount(Timeline.INDEFINITE);


        //sequentialTransition.setDelay(Duration.ZERO);

        //sequentialTransition.play();
       //pt.play(); // Start animation


        System.out.println(Math.abs(-3/2));



        circle.setOnMousePressed(e -> pt.pause());
        circle.setOnMouseReleased(e -> pt.play());

        // Create a scene and place it in the stage
        Scene scene = new Scene(pane, 500, 500);
        primaryStage.setTitle("PathTransitionDemo"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
