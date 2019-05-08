class EmptyTile extends Tile {
    EmptyTile(boolean isStatic) {
        super((isStatic ? "s_" : "") + "empty.png");
        this.isStatic = isStatic;
        setValues(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
}
