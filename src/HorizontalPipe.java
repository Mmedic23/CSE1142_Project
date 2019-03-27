class HorizontalPipe extends Pipe {
    HorizontalPipe(boolean isStatic) {
        super(isStatic ? "s_horizontal.png" : "horizontal.png");
        setValues(+1, -1, 0, 0);
    }
}
