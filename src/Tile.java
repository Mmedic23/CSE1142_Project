import javafx.scene.image.ImageView;

// The abstract Tile class is the superclass of all other classes in this program.
// Each Tile object as 4 values, each corresponding to a direction the ball should move towards, when entered from a specified direction.
abstract class Tile extends ImageView {
    // These constants are used to improve readability.
    // They are used to get the appropriate value from the values array, when checking the path.
    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int TOP = 2;
    static final int BOTTOM = 3;


    boolean isStatic = false;
    final int[] values = new int[4];

    Tile(String fileName) {
        super("file:res/" + fileName);
    }

    void setValues(int left, int right, int top, int bottom) {
        values[0] = left;
        values[1] = right;
        values[2] = top;
        values[3] = bottom;
    }
}
