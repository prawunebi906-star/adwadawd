package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/** Shared bits for cosmetics: they are line art drawn on top of your own player, visible to you only, in third person. */
abstract class CosmeticModule extends Module {
    /** Player position (interpolated), facing basis and size. {@code at} converts body-relative offsets to world coordinates. */
    record Frame(double x, double y, double z, double fx, double fz, double rx, double rz, double height, boolean sneaking, boolean airborne) {
        Vec3d at(double lateral, double up, double forward) { return new Vec3d(x + rx * lateral + fx * forward, y + up, z + rz * lateral + fz * forward); }
    }

    protected CosmeticModule(String name, String description) { super(name, description, Category.COSMETICS); }

    /** Null when there is nothing to draw on (first person, spectator, no player). */
    protected static Frame frame(View v) {
        MinecraftClient c = v.client;
        ClientPlayerEntity p = c.player;
        if (p == null || p.isSpectator() || c.options.getPerspective().isFirstPerson()) return null;
        Vec3d pos = p.getLerpedPos(v.tickDelta);
        double yaw = Math.toRadians(p.getBodyYaw());
        return new Frame(pos.x, pos.y, pos.z, -Math.sin(yaw), Math.cos(yaw), -Math.cos(yaw), -Math.sin(yaw), p.getHeight(), p.isSneaking(), !p.isOnGround());
    }

    protected static void seg(DrawContext g, View v, Vec3d a, Vec3d b, int thickness, int color) {
        Draw.world(g, v.cam, a.x, a.y, a.z, b.x, b.y, b.z, thickness, color);
    }

    protected static double seconds() { return System.nanoTime() / 1_000_000_000.0; }
}
