package dev.rox;

/** A module option: toggle, bounded number or choice list. */
public final class Setting {
    public enum Kind { BOOL, NUM, CHOICE }

    public final String key, label;
    public final Kind kind;
    public final double min, max, step;
    public final String[] options;
    private boolean b;
    private double n;
    private int idx;

    private Setting(String key, String label, Kind kind, double min, double max, double step, String[] options) {
        this.key = key; this.label = label; this.kind = kind; this.min = min; this.max = max; this.step = step; this.options = options;
    }

    public static Setting bool(String key, String label, boolean def) {
        Setting s = new Setting(key, label, Kind.BOOL, 0, 1, 1, null);
        s.b = def;
        return s;
    }

    public static Setting num(String key, String label, double min, double max, double step, double def) {
        Setting s = new Setting(key, label, Kind.NUM, min, max, step, null);
        s.set(def);
        return s;
    }

    public static Setting choice(String key, String label, String[] options, int def) {
        Setting s = new Setting(key, label, Kind.CHOICE, 0, options.length - 1, 1, options);
        s.idx = Math.max(0, Math.min(options.length - 1, def));
        return s;
    }

    public boolean bool() { return b; }
    public double num() { return n; }
    public int intValue() { return (int) Math.round(n); }
    public int index() { return idx; }
    public String option() { return options[idx]; }

    public void setBool(boolean v) { b = v; }
    public void cycle() { idx = (idx + 1) % options.length; }
    public void setOption(String name) { for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(name)) { idx = i; return; } }

    public void set(double v) {
        if (Double.isNaN(v)) return;
        double c = Math.max(min, Math.min(max, v));
        if (step > 0) c = Math.round(c / step) * step;
        n = Math.max(min, Math.min(max, c));
    }
}
