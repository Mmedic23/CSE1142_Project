import javafx.scene.image.ImageView;

public abstract class Tile extends ImageView {
    /*
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    public static final int BENT_UP_RIGHT = 2;
    public static final int BENT_UP_LEFT = 3;
    public static final int BENT_DOWN_RIGHT = 4;
    public static final int BENT_DOWN_LEFT = 5;
    */

    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int TOP = 2;
    static final int BOTTOM = 3;

    boolean isStatic = false;
    int[] values = new int[4];

    public Tile(String fileName) {
        super("file:res/" + fileName);
    }

    void setValues(int left, int right, int top, int bottom) {
        values[0] = left;
        values[1] = right;
        values[2] = top;
        values[3] = bottom;
    }
}
