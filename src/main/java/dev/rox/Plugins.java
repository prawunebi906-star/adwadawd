package dev.rox;

import java.util.*;

/** Pure helpers for turning command names and tab-complete answers into a plugin list. */
public final class Plugins {
    private Plugins() { }

    private static final Set<String> NOT_PLUGINS = Set.of("minecraft", "bukkit", "spigot", "paper", "purpur", "folia", "velocity", "bungeecord", "craftbukkit");
    /** command or namespace (lower case) -> plugin name, for plugins that give themselves away by a command. */
    private static final Map<String, String> HINTS = Map.ofEntries(
            Map.entry("grim", "GrimAC"), Map.entry("grimac", "GrimAC"), Map.entry("totemguard", "TotemGuard"),
            Map.entry("ncp", "NoCheatPlus"), Map.entry("nocheatplus", "NoCheatPlus"), Map.entry("matrix", "Matrix"),
            Map.entry("vulcan", "Vulcan"), Map.entry("spartan", "Spartan"), Map.entry("aac", "AAC"),
            Map.entry("intave", "Intave"), Map.entry("themis", "Themis"), Map.entry("negativity", "Negativity"),
            Map.entry("verus", "Verus"), Map.entry("polar", "Polar"), Map.entry("lp", "LuckPerms"),
            Map.entry("luckperms", "LuckPerms"), Map.entry("viaversion", "ViaVersion"), Map.entry("viaver", "ViaVersion"),
            Map.entry("protocollib", "ProtocolLib"), Map.entry("worldedit", "WorldEdit"), Map.entry("essentials", "Essentials"));

    /** "essentials:home" -> "essentials"; server-software namespaces are dropped. */
    public static Set<String> namespaces(Collection<String> rootNames) {
        Set<String> out = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String n : rootNames) {
            int i = n.indexOf(':');
            if (i <= 0) continue;
            String ns = n.substring(0, i);
            if (!NOT_PLUGINS.contains(ns.toLowerCase(Locale.ROOT))) out.add(ns);
        }
        return out;
    }

    /** Tab-complete answers minus junk (player names, numbers, anything with spaces or slashes). */
    public static List<String> clean(Collection<String> suggestions, Collection<String> playerNames) {
        Set<String> players = new HashSet<>();
        for (String p : playerNames) players.add(p.toLowerCase(Locale.ROOT));
        List<String> out = new ArrayList<>();
        for (String s : suggestions) {
            String t = s == null ? "" : s.trim();
            if (t.isEmpty() || t.contains(" ") || t.startsWith("/") || t.matches("\\d+") || players.contains(t.toLowerCase(Locale.ROOT))) continue;
            out.add(t);
        }
        return out;
    }

    /** Known plugins recognised from command names, namespaces or probe results. */
    public static Set<String> hints(Collection<String> names) {
        Set<String> out = new TreeSet<>();
        for (String n : names) {
            String l = n.toLowerCase(Locale.ROOT);
            int i = l.indexOf(':');
            if (i >= 0) l = l.substring(0, i);
            String h = HINTS.get(l);
            if (h != null) out.add(h);
        }
        return out;
    }

    /** "Getting info." / ".." / "..." cycle with a spinner. */
    public static String waiting(int tick) {
        return "Getting info" + ".".repeat(1 + (tick / 6) % 3) + " " + "|/-\\".charAt((tick / 2) % 4);
    }
}
