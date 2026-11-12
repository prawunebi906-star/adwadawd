package dev.rox;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RoxClient implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("rox");
    public static final List<Module> MODULES = List.of(
            new ServerInfoModule(), new PlayersModule(), new WorldModule(), new CommandsModule(), new NearbyModule(),
            new PlayerEspModule(), new StorageEspModule(),
            new WingsModule(), new HaloModule(), new OrbitModule(),
            new TpsModule());
    private static boolean insertWasDown;

    @Override
    public void onInitializeClient() {
        ConfigStore.load(MODULES);
        ClientTickEvents.END_CLIENT_TICK.register(RoxClient::tick);
        // Version-sensitive: HudRenderCallback is deprecated; HudElementRegistry is the replacement if it ever goes away.
        HudRenderCallback.EVENT.register((g, tickCounter) -> render(g, MinecraftClient.getInstance()));
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> ConfigStore.save(MODULES));
    }

    public static long handle(MinecraftClient c) { return c.getWindow().getHandle(); }

    private static void tick(MinecraftClient c) {
        boolean down = GLFW.glfwGetKey(handle(c), GLFW.GLFW_KEY_INSERT) == GLFW.GLFW_PRESS;
        if (down && !insertWasDown && c.currentScreen == null && c.player != null) c.setScreen(new RoxScreen());
        insertWasDown = down;
        for (Module m : MODULES) {
            if (!m.enabled()) continue;
            try { m.tick(c); } catch (RuntimeException e) { crashed(m, e); }
        }
    }

    private static void render(DrawContext g, MinecraftClient c) {
        Draw.begin();
        View v = View.capture(c);
        if (v == null) return;
        for (Module m : MODULES) {
            if (!m.enabled()) continue;
            try { m.render(g, v); } catch (RuntimeException e) { crashed(m, e); }
        }
    }

    private static void crashed(Module m, RuntimeException e) {
        LOG.error("{} crashed and was switched off", m.name, e);
        m.setEnabled(false);
    }
}
