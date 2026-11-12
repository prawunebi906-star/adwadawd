package dev.rox;

/**
 * World -> screen projection (pure maths, unit-tested). Minecraft conventions: yaw 0 faces +Z (south), pitch +90
 * looks straight down, FOV is vertical. Screen units are scaled GUI pixels.
 */
public final class Projector {
    private Projector() { }

    public static final double NEAR = 0.05;

    public static final class Cam {
        public final double x, y, z, scale;
        public final int w, h;
        final double fx, fy, fz, rx, ry, rz, ux, uy, uz;

        public Cam(double x, double y, double z, double yawDeg, double pitchDeg, double fovDeg, int w, int h) {
            this.x = x; this.y = y; this.z = z; this.w = w; this.h = h;
            double yaw = Math.toRadians(yawDeg), pitch = Math.toRadians(pitchDeg);
            fx = -Math.sin(yaw) * Math.cos(pitch);
            fy = -Math.sin(pitch);
            fz = Math.cos(yaw) * Math.cos(pitch);
            rx = -Math.cos(yaw); ry = 0; rz = -Math.sin(yaw);
            ux = ry * fz - rz * fy;            // up = right x forward
            uy = rz * fx - rx * fz;
            uz = rx * fy - ry * fx;
            scale = (h / 2.0) / Math.tan(Math.toRadians(Math.max(1, Math.min(170, fovDeg))) / 2.0);
        }

        /** Camera-space coordinates {right, up, depth}. */
        public double[] toCamera(double px, double py, double pz) {
            double dx = px - x, dy = py - y, dz = pz - z;
            return new double[]{dx * rx + dy * ry + dz * rz, dx * ux + dy * uy + dz * uz, dx * fx + dy * fy + dz * fz};
        }
    }

    /** @return {screenX, screenY, depth} or null when the point is behind the near plane. */
    public static double[] project(Cam c, double px, double py, double pz) {
        double[] v = c.toCamera(px, py, pz);
        if (v[2] < NEAR) return null;
        return new double[]{c.w / 2.0 + v[0] / v[2] * c.scale, c.h / 2.0 - v[1] / v[2] * c.scale, v[2]};
    }

    /** Like {@link #project} but never null: points behind the camera land far off-screen in the direction to turn. */
    public static double[] projectDirectional(Cam c, double px, double py, double pz) {
        double[] v = c.toCamera(px, py, pz);
        double d = Math.max(Math.abs(v[2]), 1e-3);
        return new double[]{c.w / 2.0 + v[0] / d * c.scale, c.h / 2.0 - v[1] / d * c.scale, v[2]};
    }

    /** Segment between two world points clipped to the near plane, projected: {x1,y1,x2,y2} or null if fully behind. */
    public static double[] projectSegment(Cam c, double ax, double ay, double az, double bx, double by, double bz) {
        double[] a = c.toCamera(ax, ay, az), b = c.toCamera(bx, by, bz);
        if (a[2] < NEAR && b[2] < NEAR) return null;
        if (a[2] < NEAR) a = lerpToNear(a, b);
        else if (b[2] < NEAR) b = lerpToNear(b, a);
        return new double[]{
                c.w / 2.0 + a[0] / a[2] * c.scale, c.h / 2.0 - a[1] / a[2] * c.scale,
                c.w / 2.0 + b[0] / b[2] * c.scale, c.h / 2.0 - b[1] / b[2] * c.scale};
    }

    private static double[] lerpToNear(double[] behind, double[] front) {
        double t = (NEAR - behind[2]) / (front[2] - behind[2]);
        return new double[]{behind[0] + t * (front[0] - behind[0]), behind[1] + t * (front[1] - behind[1]), NEAR};
    }

    /** Liang-Barsky clip of a screen segment to [0,w]x[0,h]: {x1,y1,x2,y2} or null if entirely outside. */
    public static double[] clipLine(double x1, double y1, double x2, double y2, double w, double h) {
        double t0 = 0, t1 = 1, dx = x2 - x1, dy = y2 - y1;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x1, w - x1, y1, h - y1};
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) { if (q[i] < 0) return null; continue; }
            double r = q[i] / p[i];
            if (p[i] < 0) { if (r > t1) return null; if (r > t0) t0 = r; }
            else { if (r < t0) return null; if (r < t1) t1 = r; }
        }
        return new double[]{x1 + t0 * dx, y1 + t0 * dy, x1 + t1 * dx, y1 + t1 * dy};
    }
}
