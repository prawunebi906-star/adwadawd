package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;

import java.util.ArrayList;
import java.util.List;

public final class WorldModule extends InfoModule {
    public WorldModule() { super("World", "Dimension, time, weather, biome, world border."); }

    @Override
    protected List<Text> gather(MinecraftClient c) {
        ClientWorld w = c.world;
        BlockPos p = c.player.getBlockPos();
        WorldBorder b = w.getWorldBorder();
        long time = w.getTimeOfDay();
        String biome = w.getBiome(p).getKey().map(k -> k.getValue().toString()).orElse("unknown");
        String weather = w.isThundering() ? "thunder" : w.isRaining() ? "rain" : "clear";
        List<Text> out = new ArrayList<>();
        out.add(head("World"));
        out.add(row("Dimension", w.getRegistryKey().getValue().toString()));
        out.add(row("Biome", biome));
        out.add(row("Time", Info.clock(time) + " (day " + (time / 24000) + ")  |  weather: " + weather));
        out.add(row("Border", "size " + Math.round(b.getSize()) + ", centre " + Math.round(b.getCenterX()) + " " + Math.round(b.getCenterZ())));
        out.add(row("You", p.getX() + " " + p.getY() + " " + p.getZ() + "  |  view distance " + c.options.getViewDistance().getValue() + " chunks"));
        return out;
    }
}
