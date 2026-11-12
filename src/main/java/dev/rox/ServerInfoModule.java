package dev.rox;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Finds the server's plugins without /pl. Two sources, both answered by the server itself:
 * 1) the command tree it sent us (plugin commands appear as "plugin:command"), and
 * 2) tab-completion of "/version ", "/ver ", "/about " and "/icanhasbukkit ", which on Bukkit-based servers lists the
 *    installed plugin names. Nothing is run; only completion requests are sent.
 * A server that hides both cannot be read, so the list can be incomplete.
 */
public final class ServerInfoModule extends Module {
    private static final String[] PROBES = {"/version ", "/ver ", "/about ", "/icanhasbukkit "};
    private static final int START_DELAY = 10, TIMEOUT = 40, REVEAL_EVERY = 6;

    private enum S { IDLE, WAIT, PROBE, REVEAL }
    private S state = S.IDLE;
    private int t, probe, probeAge, shown;
    private CompletableFuture<Suggestions> pending;
    private final List<String> answers = new ArrayList<>();
    private final List<Text> lines = new ArrayList<>();
    private Object lastPlayer;

    public ServerInfoModule() { super("ServerInfo", "Shows the server's plugins (works even when /pl is disabled).", Category.INFO); persist = false; }

    @Override protected void onEnable() { MinecraftClient c = MinecraftClient.getInstance(); lastPlayer = c.player; begin(c, START_DELAY); }
    @Override protected void onDisable() { state = S.IDLE; pending = null; }

    private void begin(MinecraftClient c, int delay) {
        state = S.WAIT; t = -delay; probe = 0; shown = 0; pending = null;
        answers.clear(); lines.clear();
        if (c.player != null) Chat.say(c, Text.literal("Getting info..").formatted(Formatting.GRAY));
    }

    @Override
    public void tick(MinecraftClient c) {
        if (!enabled()) return;
        if (c.player == null || c.player.networkHandler == null) { lastPlayer = null; return; }
        if (c.player != lastPlayer) { lastPlayer = c.player; begin(c, 60); }   // new server/world: rescan once the command tree has arrived
        ClientPlayNetworkHandler h = c.player.networkHandler;
        t++;
        switch (state) {
            case WAIT -> {
                animate(c);
                if (t >= 0) { state = S.PROBE; probeAge = 0; sendProbe(c, h); }
            }
            case PROBE -> {
                animate(c);
                probeAge++;
                boolean done = pending == null || pending.isDone() || probeAge > TIMEOUT;
                if (!done) return;
                if (pending != null && pending.isDone() && !pending.isCompletedExceptionally() && !pending.isCancelled()) {
                    for (Suggestion s : pending.join().getList()) answers.add(s.getText());
                }
                if (++probe >= PROBES.length) { build(c, h); state = S.REVEAL; t = 0; }
                else { probeAge = 0; sendProbe(c, h); }
            }
            case REVEAL -> {
                if (t % REVEAL_EVERY != 0) return;
                if (shown < lines.size()) Chat.say(c, lines.get(shown++));
                else state = S.IDLE;
            }
            default -> { }
        }
    }

    private void animate(MinecraftClient c) { c.player.sendMessage(Text.literal(Plugins.waiting(t + 20)).formatted(Formatting.AQUA), true); }

    /** Version-sensitive: ClientCommandSource#getCompletions sends the completion request and returns the answer. */
    private void sendProbe(MinecraftClient c, ClientPlayNetworkHandler h) {
        try {
            CommandDispatcher<ClientCommandSource> d = h.getCommandDispatcher();
            ClientCommandSource src = h.getCommandSource();
            CommandContext<ClientCommandSource> ctx = new CommandContextBuilder<>(d, src, d.getRoot(), 0).build(PROBES[probe]);
            pending = src.getCompletions(ctx);
        } catch (RuntimeException e) { pending = null; }
    }

    private void build(MinecraftClient c, ClientPlayNetworkHandler h) {
        List<String> players = new ArrayList<>();
        for (PlayerListEntry e : h.getPlayerList()) players.add(e.getProfile().name());
        List<String> roots = new ArrayList<>();
        h.getCommandDispatcher().getRoot().getChildren().forEach(n -> roots.add(n.getName()));

        Set<String> plugins = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        plugins.addAll(Plugins.clean(answers, players));          // proper capitalisation first
        plugins.addAll(Plugins.namespaces(roots));
        List<String> all = new ArrayList<>(roots); all.addAll(plugins);
        Set<String> hints = Plugins.hints(all);

        ServerInfo si = c.getCurrentServerEntry();
        String brand = h.getBrand() == null ? "unknown" : h.getBrand();
        lines.add(Text.literal("--- Server info ---").formatted(Formatting.AQUA));
        lines.add(Text.literal((si != null ? si.address : "singleplayer") + "  |  brand: " + brand + "  |  players: " + players.size()
                + (si != null && si.ping > 0 ? "  |  ping: " + si.ping + "ms" : "")).formatted(Formatting.WHITE));
        if (plugins.isEmpty()) lines.add(Text.literal("Plugins: none found (the server hides its command tree and completions).").formatted(Formatting.YELLOW));
        else lines.add(Text.literal("Plugins (" + plugins.size() + "): ").formatted(Formatting.GREEN).append(Text.literal(String.join(", ", plugins)).formatted(Formatting.WHITE)));
        if (!hints.isEmpty()) lines.add(Text.literal("Recognised: " + String.join(", ", hints)).formatted(Formatting.RED));
        lines.add(Text.literal("Sources: command tree + tab-complete. The list can be incomplete.").formatted(Formatting.DARK_GRAY));
    }
}
