package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class CommandsModule extends InfoModule {
    private static final Set<String> ADMIN = Set.of("op", "deop", "gamemode", "give", "ban", "kick", "stop", "setblock", "fill", "summon", "tp", "whitelist");

    public CommandsModule() { super("Commands", "Commands the server lets you see, and which admin ones are among them."); }

    @Override
    protected List<Text> gather(MinecraftClient c) {
        Set<String> plain = new TreeSet<>(), spaced = new TreeSet<>(), admin = new TreeSet<>();
        c.player.networkHandler.getCommandDispatcher().getRoot().getChildren().forEach(n -> {
            String s = n.getName();
            (s.contains(":") ? spaced : plain).add(s);
            if (ADMIN.contains(s)) admin.add(s);
        });
        List<Text> out = new ArrayList<>();
        out.add(head("Commands"));
        out.add(row("Visible", plain.size() + " plain, " + spaced.size() + " namespaced"));
        out.add(row("Admin commands you can see", admin.isEmpty() ? "none (probably not operator)" : String.join(", ", admin)));
        out.add(Text.literal(Info.top(new ArrayList<>(plain), 60)).formatted(Formatting.WHITE));
        out.add(Text.literal("The server filters this list by your permissions.").formatted(Formatting.DARK_GRAY));
        return out;
    }
}
