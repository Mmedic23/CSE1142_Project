public class StartPipe extends Tile {
    StartPipe(boolean isVertical) {
        super("start_" + (isVertical ? "vertical" : "horizontal") + ".png");
        this.isStatic = true;
        if (isVertical)
            setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, 4, Integer.MIN_VALUE);
        else
            setValues(Integer.MIN_VALUE, -1, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
}
