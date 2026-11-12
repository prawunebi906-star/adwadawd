package dev.rox;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PluginsTest {
    @Test void namespacesKeepPluginsAndDropSoftware() {
        Set<String> n = Plugins.namespaces(List.of("essentials:home", "minecraft:tp", "bukkit:version", "luckperms:lp", "home", "WorldEdit:up", ":bad"));
        assertEquals(3, n.size());
        assertTrue(n.contains("essentials") && n.contains("luckperms") && n.contains("worldedit"));
    }
    @Test void cleanRemovesPlayersNumbersAndJunk() {
        List<String> r = Plugins.clean(List.of("WorldEdit", "Steve", "12", "/x", "a b", "", "Essentials"), List.of("steve"));
        assertEquals(2, r.size());
        assertEquals("WorldEdit", r.get(0));
        assertEquals("Essentials", r.get(1));
    }
    @Test void hintsFindAntiCheatsFromCommandsAndNamespaces() {
        Set<String> h = Plugins.hints(List.of("grim", "totemguard:reload", "home", "LP"));
        assertTrue(h.contains("GrimAC") && h.contains("TotemGuard") && h.contains("LuckPerms"));
        assertEquals(3, h.size());
    }
    @Test void waitingAnimationCyclesDotsAndSpinner() {
        assertTrue(Plugins.waiting(0).startsWith("Getting info. "));
        assertTrue(Plugins.waiting(6).startsWith("Getting info.. "));
        assertTrue(Plugins.waiting(12).startsWith("Getting info... "));
        assertTrue(Plugins.waiting(18).startsWith("Getting info. "));
        assertTrue(!Plugins.waiting(0).equals(Plugins.waiting(2)));
    }
}
