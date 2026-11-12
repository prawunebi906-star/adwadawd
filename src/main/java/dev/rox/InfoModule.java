package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** One-shot info module: "Getting info.." with an animation, then the result typed out line by line, then it switches itself off. */
public abstract class InfoModule extends Module {
    private int t, shown;
    private boolean running;
    private List<Text> lines;

    protected InfoModule(String name, String description) { super(name, description, Category.INFO);
        persist = false; }

    /** Reads whatever the module reports. Runs once, on the client thread. */
    protected abstract List<Text> gather(MinecraftClient c);

    protected static Text head(String s) { return Text.literal("--- " + s + " ---").formatted(Formatting.AQUA); }
    protected static Text row(String label, String value) { return Text.literal(label + ": ").formatted(Formatting.GREEN).append(Text.literal(value).formatted(Formatting.WHITE)); }

    @Override
    protected void onEnable() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null) { setEnabled(false); return; }
        running = true; t = 0; shown = 0; lines = null;
        Chat.say(c, Text.literal("Getting info..").formatted(Formatting.GRAY));
    }

    @Override protected void onDisable() { running = false; }

    @Override
    public void tick(MinecraftClient c) {
        if (!enabled() || !running) return;
        if (c.player == null || c.world == null) { running = false; setEnabled(false); return; }
        t++;
        if (lines == null) {
            c.player.sendMessage(Text.literal(Plugins.waiting(t)).formatted(Formatting.AQUA), true);
            if (t >= 20) {
                try { lines = gather(c); }
                catch (RuntimeException e) { lines = List.of(Text.literal("Could not read info: " + e).formatted(Formatting.RED)); }
                t = 0;
            }
            return;
        }
        if (t % 4 != 0) return;
        if (shown < lines.size()) Chat.say(c, lines.get(shown++));
        else { running = false; setEnabled(false); }
    }
}
