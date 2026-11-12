package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PlayersModule extends InfoModule {
    public PlayersModule() { super("Players", "Online players with ping; flags staff-like ranks and spectators."); }

    @Override
    protected List<Text> gather(MinecraftClient c) {
        List<PlayerListEntry> es = new ArrayList<>(c.player.networkHandler.getPlayerList());
        es.sort(Comparator.comparing(e -> e.getProfile().name().toLowerCase()));
        List<String> all = new ArrayList<>(), staff = new ArrayList<>(), spec = new ArrayList<>();
        for (PlayerListEntry e : es) {
            String n = e.getProfile().name();
            all.add(n + " " + e.getLatency() + "ms");
            Text dn = e.getDisplayName();
            if (Info.staffLike(dn == null ? null : dn.getString())) staff.add(n);
            if (e.getGameMode() != null && e.getGameMode().name().equals("SPECTATOR")) spec.add(n);
        }
        List<Text> out = new ArrayList<>();
        out.add(head("Players (" + es.size() + ")"));
        out.add(Text.literal(Info.top(all, 40)).formatted(Formatting.WHITE));
        out.add(row("Staff-like ranks", staff.isEmpty() ? "none" : String.join(", ", staff)));
        out.add(row("In spectator mode", spec.isEmpty() ? "none" : String.join(", ", spec)));
        out.add(Text.literal("Guesses from tab-list names and game modes only.").formatted(Formatting.DARK_GRAY));
        return out;
    }
}
