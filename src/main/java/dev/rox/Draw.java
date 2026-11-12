package dev.rox;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;

/** Line and box drawing on the 2D HUD layer, built only from DrawContext.fill so it is version-proof. */
public final class Draw {
    private Draw() { }

    /** Max rectangles drawn per frame. Keeps worst cases (many long lines) from tanking the frame rate. */
    private static final int FRAME_BUDGET = 5000;
    private static int budget = FRAME_BUDGET;

    public static void begin() { budget = FRAME_BUDGET; }

    private static void fill(DrawContext g, int x1, int y1, int x2, int y2, int color) {
        if (budget <= 0) return;
        budget--;
        g.fill(x1, y1, x2, y2, color);
    }

    public static void line(DrawContext g, double x1, double y1, double x2, double y2, int t, int color, int w, int h) {
        double[] c = Projector.clipLine(x1, y1, x2, y2, w, h);
        if (c == null) return;
        t = Math.max(1, t);
        double dx = c[2] - c[0], dy = c[3] - c[1];
        double len = Math.sqrt(dx * dx + dy * dy);
        int steps = Math.max(1, (int) Math.min(Math.ceil(len / Math.max(1.0, t * 0.8)), 220)); // very long lines become gently dotted
        for (int i = 0; i <= steps; i++) {
            double f = (double) i / steps;
            int x = (int) Math.round(c[0] + dx * f - t / 2.0), y = (int) Math.round(c[1] + dy * f - t / 2.0);
            fill(g, x, y, x + t, y + t, color);
        }
    }

    public static void rect(DrawContext g, int x1, int y1, int x2, int y2, int t, int color) {
        fill(g, x1, y1, x2, y1 + t, color);
        fill(g, x1, y2 - t, x2, y2, color);
        fill(g, x1, y1 + t, x1 + t, y2 - t, color);
        fill(g, x2 - t, y1 + t, x2, y2 - t, color);
    }

    /** Line between two world points (near-plane clipped). */
    public static void world(DrawContext g, Projector.Cam c, double ax, double ay, double az, double bx, double by, double bz, int t, int color) {
        double[] s = Projector.projectSegment(c, ax, ay, az, bx, by, bz);
        if (s != null) line(g, s[0], s[1], s[2], s[3], t, color, c.w, c.h);
    }

    /** Line from a screen point to a world point; targets behind you still draw toward the side to turn to. */
    public static void tracer(DrawContext g, Projector.Cam c, double ox, double oy, double tx, double ty, double tz, int t, int color) {
        double[] p = Projector.projectDirectional(c, tx, ty, tz);
        line(g, ox, oy, p[0], p[1], t, color, c.w, c.h);
    }

    /** All 12 edges of an axis-aligned box. */
    public static void edges(DrawContext g, Projector.Cam c, double x1, double y1, double z1, double x2, double y2, double z2, int t, int color) {
        world(g, c, x1, y1, z1, x2, y1, z1, t, color); world(g, c, x1, y1, z2, x2, y1, z2, t, color);
        world(g, c, x1, y2, z1, x2, y2, z1, t, color); world(g, c, x1, y2, z2, x2, y2, z2, t, color);
        world(g, c, x1, y1, z1, x1, y2, z1, t, color); world(g, c, x2, y1, z1, x2, y2, z1, t, color);
        world(g, c, x1, y1, z2, x1, y2, z2, t, color); world(g, c, x2, y1, z2, x2, y2, z2, t, color);
        world(g, c, x1, y1, z1, x1, y1, z2, t, color); world(g, c, x2, y1, z1, x2, y1, z2, t, color);
        world(g, c, x1, y2, z1, x1, y2, z2, t, color); world(g, c, x2, y2, z1, x2, y2, z2, t, color);
    }

    /** Screen-space bounding rectangle {x1,y1,x2,y2} of a world box, or null if any corner is behind the camera. */
    public static double[] screenRect(Projector.Cam c, Box b) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double[] p = Projector.project(c, (i & 1) == 0 ? b.minX : b.maxX, (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ);
            if (p == null) return null;
            minX = Math.min(minX, p[0]); maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
        }
        if (maxX < 0 || maxY < 0 || minX > c.w || minY > c.h) return null; // fully off-screen
        return new double[]{minX, minY, maxX, maxY};
    }
}
