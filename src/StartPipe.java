public class StartPipe extends Pipe {
    StartPipe(boolean isVertical) {
        super("start_" + (isVertical ? "vertical" : "horizontal") + ".png");
        if (isVertical)
            setValues(0, 0, 4, 0);
        else
            setValues(0, -1, 0, 0);
    }
}
