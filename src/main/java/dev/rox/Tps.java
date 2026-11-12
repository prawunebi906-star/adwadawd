package dev.rox;

/**
 * TPS estimate from world-age corrections. The client advances the world age by 1 every tick and the server resets it
 * every 20 server ticks. If the server is slow, the client has run ahead and the age jumps back: with c client ticks
 * between two corrections, jump = 20 - c, so tps = 20 * 20 / c. No jump for a while means the server keeps up.
 */
public final class Tps {
    private long last = Long.MIN_VALUE;
    private double value = 20;
    private int quiet;

    public static double estimate(long jump) {
        if (jump >= 0) return 20;
        return Math.min(20, Math.max(0.5, 400.0 / (20.0 - jump)));
    }

    /** Call once per client tick with the world age. */
    public void sample(long age) {
        if (last != Long.MIN_VALUE) {
            long jump = age - (last + 1);
            if (jump < 0 && jump > -2400) { value = 0.6 * value + 0.4 * estimate(jump); quiet = 0; }
        }
        last = age;
        if (++quiet > 160) value = Math.min(20, value + 0.05);
    }

    public void reset() { last = Long.MIN_VALUE; value = 20; quiet = 0; }
    public double value() { return value; }
}
