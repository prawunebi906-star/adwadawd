package dev.rox;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category { INFO, RENDER, COSMETICS, HUD }

    public final String name, description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    /** Whether "enabled" is remembered between sessions (one-shot info modules do not). */
    public boolean persist = true;
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    public boolean enabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        try { if (on) onEnable(); else onDisable(); }
        catch (RuntimeException e) { RoxClient.LOG.error("{} failed on {}", name, on ? "enable" : "disable", e); }
    }

    protected Setting add(Setting s) { settings.add(s); return s; }

    protected void onEnable() { }
    protected void onDisable() { }
    public void tick(MinecraftClient c) { }
    public void render(DrawContext g, View v) { }
    /** One short line under the settings in the GUI, or null. */
    public String status() { return null; }
}
