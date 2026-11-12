package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;

public final class TpsModule extends Module {
    private final Setting corner = add(Setting.choice("corner", "Corner", new String[]{"Top left", "Top right", "Bottom left", "Bottom right"}, 0));
    private final Setting ping = add(Setting.bool("ping", "Show ping", true));
    private final Tps tps = new Tps();
    private ClientWorld world;

    public TpsModule() { super("TPS", "Server TPS (estimated) and your ping on screen.", Category.HUD); }

    @Override protected void onEnable() { tps.reset(); }

    @Override
    public void tick(MinecraftClient c) {
        if (c.world == null) { world = null; return; }
        if (c.world != world) { world = c.world; tps.reset(); }
        tps.sample(c.world.getTime());
    }

    @Override
    public void render(DrawContext g, View v) {
        MinecraftClient c = v.client;
        double t = tps.value();
        int col = t >= 18 ? 0xFF55FF55 : t >= 15 ? 0xFFFFFF55 : 0xFFFF5555;
        String s = String.format("TPS %.1f", t);
        String p = null;
        if (ping.bool() && c.player != null && c.player.networkHandler != null) {
            PlayerListEntry e = c.player.networkHandler.getPlayerListEntry(c.player.getUuid());
            if (e != null) p = "Ping " + e.getLatency() + "ms";
        }
        int w = Math.max(c.textRenderer.getWidth(s), p == null ? 0 : c.textRenderer.getWidth(p));
        int x = corner.index() % 2 == 0 ? 4 : v.cam.w - w - 4;
        int y = corner.index() < 2 ? 4 : v.cam.h - (p == null ? 14 : 24);
        g.drawText(c.textRenderer, s, x, y, col, true);
        if (p != null) g.drawText(c.textRenderer, p, x, y + 10, 0xFFCCCCCC, true);
    }

    @Override public String status() { return "Estimate from world-age corrections, not an exact reading."; }
}
