package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Menu: categories on the left, the modules of the chosen category in the middle, settings of the selected module on
 * the right. Click the box to switch a module on or off, click its name to see its settings. Insert or Esc closes.
 * The mouse is read straight from GLFW each frame, so no Screen input signatures are overridden.
 */
public final class RoxScreen extends Screen {
    private static final int W = 440, H = 280, TOP = 28, SIDE = 92, LIST = 158;
    private static final int BG = 0xF0202124, PANEL = 0xFF2A2B2F, EDGE = 0xFF45474D, TEXT = 0xFFE8E8E8, DIM = 0xFF8D9096, ACCENT = 0xFFE8A33D, OFF = 0xFF55575D;

    private Module.Category category = Module.Category.INFO;
    private Module selected;
    private Setting dragging;
    private int dragX, dragW;
    private boolean wasDown, wasInsert = true;
    private final long opened = System.nanoTime();
    private float alpha;

    public RoxScreen() {
        super(Text.literal("ROX"));
        selected = first(category);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { ConfigStore.save(RoxClient.MODULES); }

    private static Module first(Module.Category cat) {
        for (Module m : RoxClient.MODULES) if (m.category == cat) return m;
        return null;
    }

    private int c(int argb) { return (((int) (((argb >>> 24) & 0xFF) * alpha)) << 24) | (argb & 0xFFFFFF); }
    private void fill(DrawContext g, int x1, int y1, int x2, int y2, int color) { g.fill(x1, y1, x2, y2, c(color)); }
    private void text(DrawContext g, String s, int x, int y, int color) { g.drawText(textRenderer, s, x, y, c(color), false); }
    private void box(DrawContext g, int x, int y, boolean on) {                       // 10x10 checkbox
        fill(g, x, y, x + 10, y + 10, EDGE);
        fill(g, x + 1, y + 1, x + 9, y + 9, on ? ACCENT : PANEL);
    }
    private static boolean in(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    @Override
    public void render(DrawContext g, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long win = RoxClient.handle(mc);
        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !wasDown;
        wasDown = down;
        if (!down) dragging = null;
        boolean insert = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_INSERT) == GLFW.GLFW_PRESS;
        if (insert && !wasInsert) { close(); return; }
        wasInsert = insert;

        float t = (float) Math.min(1, (System.nanoTime() - opened) / 150_000_000.0);
        alpha = t;

        int x0 = (width - W) / 2, y0 = (height - H) / 2;
        fill(g, 0, 0, width, height, 0x66000000);
        fill(g, x0 - 1, y0 - 1, x0 + W + 1, y0 + H + 1, EDGE);
        fill(g, x0, y0, x0 + W, y0 + H, BG);

        text(g, "ROX", x0 + 12, y0 + 10, ACCENT);
        text(g, "Insert closes", x0 + W - 12 - textRenderer.getWidth("Insert closes"), y0 + 10, DIM);
        fill(g, x0, y0 + TOP, x0 + W, y0 + TOP + 1, EDGE);

        tabs(g, x0, y0, mx, my, click);
        modules(g, x0 + SIDE, y0 + TOP + 1, mx, my, click);
        settings(g, x0 + SIDE + LIST, y0 + TOP + 1, mx, my, down, click);
    }

    private void tabs(DrawContext g, int x0, int y0, int mx, int my, boolean click) {
        int y = y0 + TOP + 8;
        for (Module.Category cat : Module.Category.values()) {
            boolean sel = cat == category;
            if (sel) { fill(g, x0, y, x0 + 2, y + 18, ACCENT); }
            String label = cat.name().charAt(0) + cat.name().substring(1).toLowerCase();
            text(g, label, x0 + 12, y + 5, sel ? TEXT : DIM);
            if (click && in(mx, my, x0, y, SIDE, 18) && !sel) { category = cat; selected = first(cat); }
            y += 22;
        }
    }

    private void modules(DrawContext g, int x0, int y0, int mx, int my, boolean click) {
        fill(g, x0, y0, x0 + 1, y0 + H - TOP - 1, EDGE);
        int y = y0 + 8;
        for (Module m : RoxClient.MODULES) {
            if (m.category != category) continue;
            if (m == selected) fill(g, x0 + 4, y - 3, x0 + LIST - 4, y + 15, PANEL);
            box(g, x0 + 10, y + 1, m.enabled());
            text(g, m.name, x0 + 28, y + 3, m.enabled() ? TEXT : DIM);
            if (click && in(mx, my, x0 + 4, y - 3, LIST - 8, 18)) {
                selected = m;
                if (mx < x0 + 24) m.toggle();
            }
            y += 20;
        }
    }

    private void settings(DrawContext g, int x0, int y0, int mx, int my, boolean down, boolean click) {
        int pw = W - SIDE - LIST;
        fill(g, x0, y0, x0 + 1, y0 + H - TOP - 1, EDGE);
        if (selected == null) return;
        int x = x0 + 12, w = pw - 24, y = y0 + 10;
        text(g, selected.name, x, y, ACCENT);
        y += 13;
        int perLine = Math.max(10, w / 6);
        String d = selected.description;
        for (int i = 0; i < d.length(); i += perLine) { text(g, d.substring(i, Math.min(d.length(), i + perLine)), x, y, DIM); y += 10; }
        y += 8;

        for (Setting s : selected.settings) {
            switch (s.kind) {
                case BOOL -> {
                    text(g, s.label, x, y + 1, TEXT);
                    box(g, x + w - 10, y, s.bool());
                    if (click && in(mx, my, x, y - 3, w, 16)) s.setBool(!s.bool());
                    y += 18;
                }
                case CHOICE -> {
                    text(g, s.label, x, y + 1, TEXT);
                    String val = s.option();
                    int vx = x + w - textRenderer.getWidth(val);
                    if (s.options == Colors.NAMES) fill(g, vx - 12, y + 1, vx - 4, y + 9, 0xFF000000 | Colors.rgb(s.index()));
                    text(g, val, vx, y + 1, ACCENT);
                    if (click && in(mx, my, x, y - 3, w, 16)) s.cycle();
                    y += 18;
                }
                case NUM -> {
                    String val = s.step >= 1 ? String.valueOf(s.intValue()) : String.format("%.1f", s.num());
                    text(g, s.label, x, y + 1, TEXT);
                    text(g, val, x + w - textRenderer.getWidth(val), y + 1, DIM);
                    int sy = y + 14;
                    double f = (s.num() - s.min) / (s.max - s.min);
                    fill(g, x, sy, x + w, sy + 2, OFF);
                    fill(g, x, sy, x + (int) (w * f), sy + 2, ACCENT);
                    fill(g, x + (int) (w * f) - 2, sy - 3, x + (int) (w * f) + 2, sy + 5, TEXT);
                    if (click && in(mx, my, x, y - 3, w, 24)) { dragging = s; dragX = x; dragW = w; }
                    if (dragging == s && down) s.set(s.min + Math.max(0, Math.min(1, (mx - dragX) / (double) dragW)) * (s.max - s.min));
                    y += 26;
                }
            }
        }
        String st = selected.status();
        if (st != null) {
            y += 4;
            for (int i = 0; i < st.length(); i += perLine) { text(g, st.substring(i, Math.min(st.length(), i + perLine)), x, y, DIM); y += 10; }
        }
    }
}
