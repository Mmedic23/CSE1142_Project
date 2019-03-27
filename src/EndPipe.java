public class EndPipe extends Pipe {
    EndPipe(boolean isVertical) {
        super("end_" + (isVertical ? "vertical" : "horizontal") + ".png");
        //TODO these values should be reconsidered for balls getting stuck on a previous pipe vs.
        //                                             balls getting stuck INSIDE the pipe.
        if (isVertical)
            setValues(0, 0, 0, 0);
        else
            setValues(0, 0, 0, 0);
    }
}
