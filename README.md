# ROX (Fabric 1.21.11, client only)

**Insert** opens/closes the GUI. Modules (all print to your own chat with the same "Getting info.." animation, then switch themselves off, except ServerInfo which stays on and rescans when you change server):

| Module | Category | What it does |
|---|---|---|
| ServerInfo | Info | address, brand, players, ping, plugins (works with /pl disabled), recognised anti-cheats |
| Players | Info | online players with ping, staff-like rank prefixes, spectators |
| World | Info | dimension, biome, time, weather, border, your position |
| Commands | Info | commands the server shows you, visible admin ones |
| Nearby | Info | players and a mob count within 64 blocks |
| PlayerESP | Render | box, tracer, name, distance, health bar on other players (range 16-256) |
| StorageESP | Render | boxes on chests, trapped chests, barrels, ender chests, shulker boxes, hoppers, read from the block entities your client has loaded; optional tracers |
| Wings | Cosmetics | angel / dragon / phantom wings that flap harder in the air; size, colour, speed |
| Halo | Cosmetics | floating ring above your head |
| Orbit | Cosmetics | sparks with a trail circling you, rainbow or one colour |
| TPS | HUD | server TPS (estimate) and ping in a screen corner |

Cosmetics are line art drawn on your own player in third person (F5). Only you see them, other players do not. No texture or model assets are involved.
TPS is an estimate from world-age corrections (the simulation test gives 20 / ~10 / ~5 for 20 / 10 / 5 TPS servers). An exact reading needs a packet hook (mixin).

The menu: categories on the left, modules in the middle (click the box to switch on/off, click the name to see its settings), settings on the right. Settings and which modules were on are saved in `config/rox.json`.

Players/Commands/Nearby are guesses from data the server already sent (tab list, command tree, entities your client has loaded).

## ServerInfo
Enable it (GUI button) and chat shows `[ROX] Getting info..` while the action bar animates `Getting info... |/-\`.
Then the result is typed out line by line: address, server brand, player count, ping, the plugin list, and any
recognised anti-cheat or well-known plugin. It re-runs by itself when you join another server while enabled.

How it finds plugins when `/pl` is disabled (both answers come from the server, nothing is executed):
1. **Command tree**: plugin commands are registered as `plugin:command`; those prefixes are the plugin names.
2. **Tab-complete** of `/version `, `/ver `, `/about `, `/icanhasbukkit `: on Bukkit-based servers this lists installed plugin names. Online player names are filtered out.

Limits: a server that hides its command tree and blocks those completions (permissions, ProtocolLib/Paper hiding) shows nothing, and the list can be incomplete. The scan sends 4 tab-complete requests, which a server can log.

## Not verified
Never built or run in game (no access to Fabric/Mojang servers where this was written). Only the pure helpers (`Plugins`, `Info`, `Tps`) are tested: 13 tests pass. Confirm the version strings in `gradle.properties` at https://fabricmc.net/develop (1.21.11). Version-sensitive calls: `ClientCommandSource#getCompletions`, `ClientPlayNetworkHandler#getCommandDispatcher/getCommandSource/getBrand/getPlayerList`, `PlayerListEntry#getProfile().name()`, `ServerInfo.address/ping`, `PlayerListEntry#getLatency/getGameMode/getDisplayName`, `ClientWorld#getEntities/getBiome`, `Entity#getLerpedPos`, `LivingEntity#getBodyYaw`, `WorldChunk#getBlockEntities`, `GameOptions#getPerspective`, `Screen#render`, `HudRenderCallback` (deprecated, replace with `HudElementRegistry` if missing).

Build: JDK 21 (set JAVA_HOME to it), Loom 1.14-SNAPSHOT, Gradle 8.14.3.
Linux/macOS: `./gradlew build`. Windows (cmd/PowerShell): `gradlew.bat build`. Output: `build/libs/rox-1.0.0.jar`.
JDK 21 note for Windows 8.1: Microsoft/Oracle list Windows 10+ as supported for JDK 21; if the JDK refuses to start there, use a Temurin/Zulu 21 build and check it runs with `java -version`.
