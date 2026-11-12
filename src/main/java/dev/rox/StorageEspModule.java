package dev.rox;

import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/** Boxes on chests, barrels, ender chests, shulker boxes and hoppers, read from the block entities your client has loaded. */
public final class StorageEspModule extends Module {
    private static final String[] NAMES = {"Chest", "Trapped chest", "Barrel", "Ender chest", "Shulker box", "Hopper"};
    private static final int[] RGB = {0xFFB347, 0xFF5555, 0xB5835A, 0xB26BFF, 0xFF6BD6, 0xA0A0A8};

    private record Hit(BlockPos pos, int type) { }

    private final Setting[] types = new Setting[NAMES.length];
    private final Setting tracers = add(Setting.bool("tracers", "Tracers", false));
    private final Setting range = add(Setting.num("range", "Range", 16, 128, 8, 64));
    private List<Hit> hits = List.of();
    private int ticks;

    public StorageEspModule() {
        super("StorageESP", "Boxes on chests, barrels, ender chests, shulkers and hoppers.", Category.RENDER);
        for (int i = 0; i < NAMES.length; i++) types[i] = add(Setting.bool("t" + i, NAMES[i], i != 5));
    }

    @Override protected void onDisable() { hits = List.of(); }

    private static int type(BlockEntity be) {
        if (be instanceof TrappedChestBlockEntity) return 1;     // before ChestBlockEntity: it is a subclass
        if (be instanceof ChestBlockEntity) return 0;
        if (be instanceof BarrelBlockEntity) return 2;
        if (be instanceof EnderChestBlockEntity) return 3;
        if (be instanceof ShulkerBoxBlockEntity) return 4;
        if (be instanceof HopperBlockEntity) return 5;
        return -1;
    }

    @Override
    public void tick(MinecraftClient c) {
        if (++ticks % 10 != 0) return;
        if (c.world == null || c.player == null) { hits = List.of(); return; }
        int reach = (int) Math.ceil(range.num() / 16) + 1, pcx = c.player.getBlockX() >> 4, pcz = c.player.getBlockZ() >> 4;
        List<Hit> found = new ArrayList<>();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                Chunk ch = c.world.getChunk(pcx + dx, pcz + dz, ChunkStatus.FULL, false);   // false: never load or request chunks
                if (!(ch instanceof WorldChunk wc)) continue;
                for (BlockEntity be : new ArrayList<>(wc.getBlockEntities().values())) {
                    int t = type(be);
                    if (t >= 0 && types[t].bool()) found.add(new Hit(be.getPos(), t));
                }
            }
        }
        hits = found;
    }

    @Override
    public void render(DrawContext g, View v) {
        Projector.Cam cam = v.cam;
        double r2 = range.num() * range.num();
        for (Hit h : hits) {
            BlockPos p = h.pos();
            double dx = p.getX() + 0.5 - cam.x, dy = p.getY() + 0.5 - cam.y, dz = p.getZ() + 0.5 - cam.z;
            if (dx * dx + dy * dy + dz * dz > r2) continue;
            boolean chest = h.type() == 0 || h.type() == 1 || h.type() == 3;      // chests are a little smaller than a block
            double in = chest ? 0.0625 : 0, top = chest ? 0.875 : 1;
            int col = 0xFF000000 | RGB[h.type()];
            Draw.edges(g, cam, p.getX() + in, p.getY(), p.getZ() + in, p.getX() + 1 - in, p.getY() + top, p.getZ() + 1 - in, 1, col);
            if (tracers.bool()) Draw.tracer(g, cam, cam.w / 2.0, cam.h, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, col);
        }
    }

    @Override public String status() { return hits.size() + " in range"; }
}
