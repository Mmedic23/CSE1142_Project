public class EndPipe extends Tile {
    EndPipe(boolean isVertical) {
        super("end_" + (isVertical ? "vertical" : "horizontal") + ".png");
        //TODO these values should be reconsidered for balls getting stuck on a previous pipe vs.
        //                                             balls getting stuck INSIDE the pipe.
        this.isStatic = true;
        if (isVertical)
            setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
        else
            setValues(0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
}
