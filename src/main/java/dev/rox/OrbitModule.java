package dev.rox;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class OrbitModule extends CosmeticModule {
    private static final int TRAIL = 6;

    private final Setting count = add(Setting.num("count", "Sparks", 2, 8, 1, 4));
    private final Setting radius = add(Setting.num("radius", "Radius", 0.5, 1.5, 0.1, 0.9));
    private final Setting speed = add(Setting.num("speed", "Speed", 0.5, 4, 0.5, 1.5));
    private final Setting rainbow = add(Setting.bool("rainbow", "Rainbow", true));
    private final Setting color = add(Setting.choice("color", "Color", Colors.NAMES, 7));

    public OrbitModule() { super("Orbit", "Sparks with a short trail circling your waist (third person, only you see them)."); }

    @Override
    public void render(DrawContext g, View v) {
        Frame f = frame(v);
        if (f == null) return;
        double t = seconds() * speed.num();
        int n = count.intValue();
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < TRAIL; k++) {
                double a = t + Math.PI * 2 * i / n - k * 0.14;
                double[] s = Projector.project(v.cam, f.x() + Math.cos(a) * radius.num(), f.y() + 0.9 + 0.15 * Math.sin(a * 2 + i), f.z() + Math.sin(a) * radius.num());
                if (s == null) continue;
                int rgb = rainbow.bool() ? MathHelper.hsvToRgb((float) ((t * 0.12 + i / (double) n + k * 0.02) % 1.0), 0.7f, 1f) : Colors.rgb(color.index());
                int alpha = (int) (255 * (1 - k / (double) TRAIL)), r = k == 0 ? 3 : 2;
                g.fill((int) s[0] - r, (int) s[1] - r, (int) s[0] + r, (int) s[1] + r, (alpha << 24) | rgb);
            }
        }
    }
}
