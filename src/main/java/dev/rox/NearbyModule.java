package dev.rox;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class NearbyModule extends InfoModule {
    private static final double RADIUS = 64;

    public NearbyModule() { super("Nearby", "Players and mobs within 64 blocks."); }

    @Override
    protected List<Text> gather(MinecraftClient c) {
        record P(String name, double dist) { }
        List<P> players = new ArrayList<>();
        int hostile = 0, animals = 0, items = 0, other = 0;
        for (Entity e : c.world.getEntities()) {
            if (e == c.player) continue;
            double d = Math.sqrt(c.player.squaredDistanceTo(e));
            if (d > RADIUS) continue;
            if (e instanceof PlayerEntity pe) players.add(new P(pe.getName().getString(), d));
            else if (e instanceof HostileEntity) hostile++;
            else if (e instanceof AnimalEntity) animals++;
            else if (e instanceof ItemEntity) items++;
            else other++;
        }
        players.sort((a, b) -> Double.compare(a.dist, b.dist));
        List<String> names = new ArrayList<>();
        for (P p : players) names.add(p.name + " " + Math.round(p.dist) + "m");
        List<Text> out = new ArrayList<>();
        out.add(head("Nearby (" + (int) RADIUS + " blocks)"));
        out.add(row("Players", players.isEmpty() ? "none" : Info.top(names, 15)));
        out.add(row("Mobs", hostile + " hostile, " + animals + " animals, " + items + " items, " + other + " other"));
        return out;
    }
}
