package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Client-side chat lines (never sent to the server). */
final class Chat {
    private Chat() { }

    static void say(MinecraftClient c, Text t) {
        if (c.player != null) c.player.sendMessage(Text.literal("[ROX] ").formatted(Formatting.AQUA, Formatting.BOLD).append(t), false);
    }
}
