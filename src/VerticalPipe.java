class VerticalPipe extends Tile {
    VerticalPipe(boolean isStatic) {
        super(isStatic ? "s_vertical.png" : "vertical.png");
        this.isStatic = isStatic;
        setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, +4, -4);
    }
}
