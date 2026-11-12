package dev.rox;

import java.util.List;
import java.util.Locale;

/** Pure formatting helpers for the info modules. */
public final class Info {
    private Info() { }

    private static final String[] STAFF = {"owner", "admin", "mod", "helper", "staff", "manager", "support", "dev", "founder", "operator", "builder"};

    /** Does a tab-list display name look like a rank prefix of staff? */
    public static boolean staffLike(String displayName) {
        if (displayName == null) return false;
        String l = displayName.toLowerCase(Locale.ROOT);
        for (String s : STAFF) if (l.contains(s)) return true;
        return false;
    }

    /** World time (ticks, 0 = 06:00) as HH:MM. */
    public static String clock(long ticks) {
        long t = Math.floorMod(ticks, 24000L);
        long h = (t / 1000 + 6) % 24, m = (t % 1000) * 60 / 1000;
        return String.format("%02d:%02d", h, m);
    }

    /** First {@code max} items joined with ", ", then "(+n more)". */
    public static String top(List<String> items, int max) {
        if (items.size() <= max) return String.join(", ", items);
        return String.join(", ", items.subList(0, max)) + " (+" + (items.size() - max) + " more)";
    }
}
