package dev.rox;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public final class HaloModule extends CosmeticModule {
    private final Setting color = add(Setting.choice("color", "Color", Colors.NAMES, 5));
    private final Setting radius = add(Setting.num("radius", "Radius", 0.2, 0.5, 0.05, 0.3));
    private final Setting thick = add(Setting.num("thickness", "Line thickness", 1, 3, 1, 2));

    public HaloModule() { super("Halo", "A ring floating above your head (third person, only you see it)."); }

    @Override
    public void render(DrawContext g, View v) {
        Frame f = frame(v);
        if (f == null) return;
        double y = f.height() + 0.28 + 0.03 * Math.sin(seconds() * 2) - (f.sneaking() ? 0.2 : 0);
        int col = Colors.argb(color.index(), 0xFF), n = 24;
        double r = radius.num();
        Vec3d prev = new Vec3d(f.x() + r, f.y() + y, f.z());
        for (int i = 1; i <= n; i++) {
            double a = Math.PI * 2 * i / n;
            Vec3d p = new Vec3d(f.x() + Math.cos(a) * r, f.y() + y, f.z() + Math.sin(a) * r);
            seg(g, v, prev, p, thick.intValue(), col);
            prev = p;
        }
    }
}
