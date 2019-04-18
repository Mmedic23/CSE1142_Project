class HorizontalPipe extends Tile {
    HorizontalPipe(boolean isStatic) {
        super(isStatic ? "s_horizontal.png" : "horizontal.png");
        setValues(+1, -1, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
}
