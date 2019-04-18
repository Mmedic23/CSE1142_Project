class BentPipe extends Tile {
    BentPipe(boolean isStatic, String type) {
        super((isStatic ? "s_" : "") + type + ".png");
        switch (type) {
            case "00":
                setValues(-4, Integer.MIN_VALUE, -1, Integer.MIN_VALUE);
                break;
            case "01":
                setValues(Integer.MIN_VALUE, -4, 1, Integer.MIN_VALUE);
                break;
            case "10":
                setValues(4, Integer.MIN_VALUE, Integer.MIN_VALUE, -1);
                break;
            case "11":
                setValues(Integer.MIN_VALUE, 4, Integer.MIN_VALUE, 1);
                break;
        }
        /*
        // This is smart but useless.
        int left, right, top, bottom;
        left = right = top = bottom = 1;
        if (type.charAt(0) == '0') {
            left = right *= -1;
            bottom = 0;
        }
        else {
            // left = right = 1;
            top = 0;
        }
        if (type.charAt(1) == '0') {
            top = bottom *= -1;
            right = 0;
        }
        else {
            // top = bottom *= 1;
            left = 0;
        }
        setValues(left, right, top, bottom);
        */
    }
}
