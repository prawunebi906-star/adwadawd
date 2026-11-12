package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/** Everything a module needs to project world points to the screen this frame. */
public final class View {
    public final MinecraftClient client;
    public final Projector.Cam cam;
    public final float tickDelta;

    private View(MinecraftClient c, Projector.Cam cam, float td) { this.client = c; this.cam = cam; this.tickDelta = td; }

    /** @return null when there is no world, camera or the HUD is hidden. */
    public static View capture(MinecraftClient c) {
        if (c.world == null || c.player == null || c.gameRenderer == null || c.options.hudHidden) return null;
        Camera cam = c.gameRenderer.getCamera();
        if (cam == null || !cam.isReady()) return null;
        Vec3d p = cam.getPos();
        double fov = c.options.getFov().getValue();
        int w = c.getWindow().getScaledWidth(), h = c.getWindow().getScaledHeight();
        return new View(c, new Projector.Cam(p.x, p.y, p.z, cam.getYaw(), cam.getPitch(), fov, w, h), tickDelta(c));
    }

    /** Version-sensitive: the render tick counter's partial-tick progress. */
    private static float tickDelta(MinecraftClient c) { return c.getRenderTickCounter().getTickProgress(false); }
}
