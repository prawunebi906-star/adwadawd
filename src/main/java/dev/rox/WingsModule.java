package dev.rox;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public final class WingsModule extends CosmeticModule {
    /** Feather tips as {span, height above feet}; the wing grows out of ROOT. */
    private static final double[][][] TIPS = {
            {{0.50, 1.85}, {1.00, 2.05}, {1.40, 1.70}, {1.45, 1.20}, {1.20, 0.80}},   // angel
            {{0.70, 2.30}, {1.50, 2.15}, {1.95, 1.50}, {1.60, 0.85}, {0.95, 0.75}},   // dragon
            {{1.00, 1.95}, {1.70, 1.70}, {1.95, 1.15}, {1.50, 0.70}}};                // phantom
    private static final double ROOT_LAT = 0.12, ROOT_H = 1.30;

    private final Setting style = add(Setting.choice("style", "Style", new String[]{"Angel", "Dragon", "Phantom"}, 0));
    private final Setting color = add(Setting.choice("color", "Color", Colors.NAMES, 8));
    private final Setting size = add(Setting.num("size", "Size", 0.6, 1.6, 0.1, 1.0));
    private final Setting flap = add(Setting.num("flap", "Flap speed", 0, 4, 0.5, 1.5));
    private final Setting thick = add(Setting.num("thickness", "Line thickness", 1, 3, 1, 1));

    public WingsModule() { super("Wings", "Angel, dragon or phantom wings on your back (third person, only you see them)."); }

    @Override
    public void render(DrawContext g, View v) {
        Frame f = frame(v);
        if (f == null) return;
        double sz = size.num(), drop = f.sneaking() ? -0.3 : 0;
        double ang = Math.toRadians(32 + (f.airborne() ? 20 : 5) * Math.sin(seconds() * flap.num() * 2.0));
        double[][] tips = TIPS[style.index()];
        boolean membrane = style.index() != 0;
        int col = Colors.argb(color.index(), 0xFF), t = thick.intValue();

        for (int side = -1; side <= 1; side += 2) {
            Vec3d root = point(f, side, ROOT_LAT, ROOT_H, sz, ang, drop);
            Vec3d prev = null;
            for (double[] tip : tips) {
                Vec3d p = point(f, side, tip[0], tip[1], sz, ang, drop);
                seg(g, v, root, p, t, col);                                              // feather / bone
                if (prev != null) {
                    if (membrane) {                                                      // scalloped edge, pulled in toward the root
                        Vec3d mid = prev.add(p).multiply(0.5).multiply(0.75).add(root.multiply(0.25));
                        seg(g, v, prev, mid, t, col); seg(g, v, mid, p, t, col);
                    } else seg(g, v, prev, p, t, col);
                }
                prev = p;
            }
        }
    }

    /** Wing-plane point (span u, height h) swung back by {@code ang} around the shoulder. */
    private static Vec3d point(Frame f, int side, double u, double h, double sz, double ang, double drop) {
        double du = (u - ROOT_LAT) * sz;
        return f.at(side * (ROOT_LAT + du * Math.cos(ang)), ROOT_H + (h - ROOT_H) * sz + drop, -(0.2 + du * Math.sin(ang)));
    }
}
