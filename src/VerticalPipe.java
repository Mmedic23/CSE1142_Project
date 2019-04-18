class VerticalPipe extends Tile {
    VerticalPipe(boolean isStatic) {
        super(isStatic ? "s_vertical.png" : "vertical.png");
        setValues(0, 0, +4, -4);
    }
}
