package dev.rox;

/** Named colour presets used by the "Color" choice settings. */
public final class Colors {
    private Colors() { }

    public static final String[] NAMES = {"Green", "Red", "Blue", "Purple", "Cyan", "Yellow", "Orange", "Pink", "White"};
    private static final int[] RGB = {0x2BFF5E, 0xFF3B3B, 0x3A8DFF, 0x9B6BFF, 0x2FE6FF, 0xFFE63B, 0xFF9A2E, 0xFF5CC8, 0xF2F2F7};

    public static int rgb(int index) { return RGB[Math.floorMod(index, RGB.length)]; }
    public static int argb(int index, int alpha) { return (alpha << 24) | rgb(index); }
}
