package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Boxes, tracers, names and health on other players. Drawn in screen space, so they show through walls. */
public final class PlayerEspModule extends Module {
    private final Setting box = add(Setting.bool("box", "Box", true));
    private final Setting tracers = add(Setting.bool("tracers", "Tracers", true));
    private final Setting name = add(Setting.bool("name", "Name", true));
    private final Setting distance = add(Setting.bool("distance", "Distance", true));
    private final Setting health = add(Setting.bool("health", "Health bar", true));
    private final Setting range = add(Setting.num("range", "Range", 16, 256, 8, 128));
    private final Setting color = add(Setting.choice("color", "Color", Colors.NAMES, 4));

    public PlayerEspModule() { super("PlayerESP", "Box, tracer, name and health for players around you.", Category.RENDER); }

    @Override
    public void render(DrawContext g, View v) {
        MinecraftClient c = v.client;
        int col = Colors.argb(color.index(), 0xFF);
        double r2 = range.num() * range.num();
        Projector.Cam cam = v.cam;

        for (AbstractClientPlayerEntity p : c.world.getPlayers()) {
            if (p == c.player || p.isRemoved() || p.isSpectator()) continue;
            Vec3d pos = p.getLerpedPos(v.tickDelta);
            double dx = pos.x - cam.x, dy = pos.y - cam.y, dz = pos.z - cam.z, d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > r2) continue;
            Box bb = p.getBoundingBox().offset(pos.x - p.getX(), pos.y - p.getY(), pos.z - p.getZ());

            if (tracers.bool()) Draw.tracer(g, cam, cam.w / 2.0, cam.h, (bb.minX + bb.maxX) / 2, (bb.minY + bb.maxY) / 2, (bb.minZ + bb.maxZ) / 2, 1, col);
            if (!box.bool() && !name.bool() && !distance.bool() && !health.bool()) continue;
            double[] r = Draw.screenRect(cam, bb);
            if (r == null) continue;
            int x1 = (int) r[0], y1 = (int) r[1], x2 = (int) r[2], y2 = (int) r[3];

            if (box.bool()) {
                Draw.rect(g, x1 - 1, y1 - 1, x2 + 1, y2 + 1, 2, 0xAA000000);   // dark edge so it reads on any background
                Draw.rect(g, x1, y1, x2, y2, 1, col);
            }
            if (health.bool()) {
                float f = MathHelper.clamp(p.getHealth() / Math.max(1f, p.getMaxHealth()), 0f, 1f);
                g.fill(x1 - 5, y1, x1 - 3, y2, 0xAA000000);
                g.fill(x1 - 5, y2 - (int) ((y2 - y1) * f), x1 - 3, y2, 0xFF000000 | MathHelper.hsvToRgb(f * 0.33f, 0.85f, 1f));
            }
            if (name.bool() || distance.bool()) {
                String s = (name.bool() ? p.getName().getString() : "") + (distance.bool() ? (name.bool() ? "  " : "") + Math.round(Math.sqrt(d2)) + "m" : "");
                g.drawText(c.textRenderer, s, (x1 + x2) / 2 - c.textRenderer.getWidth(s) / 2, y1 - 11, 0xFFFFFFFF, true);
            }
        }
    }
}
