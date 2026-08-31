## Ultimate Mod Additions

A content mod built around one idea: **nothing is hardcoded**. The coliseum, the mobs that spawn in it, what they drop, what the shop sells — all of it lives in JSON files that any datapack can override, or in KubeJS scripts if you'd rather write code.

***

### The Coliseum

A dedicated dimension (`ultimatemodadditions:coliseum`) with a real Roman amphitheatre — not a flat world. The mod builds it procedurally the first time the dimension loads: sand arena, podium wall, five tiers of stands, an outer wall with two levels of arches, four gates and a return portal. Fall off the edge and you get put back on the sand instead of dying in the void.

**Getting in is a build and a gate.** You raise a portal frame exactly like a nether portal, but out of **crying obsidian** — 4×5 at minimum, up to 23×23, corners included. Then you right-click each of the four corners with a **Coliseum Key**; the game counts them off for you, and on the fourth the key is consumed and the portal lights up.

Even lit, it will not take just anyone. To step through you need **100 experience levels**, which you keep. Everyone is checked individually as they enter, so you cannot drag an underprepared friend in behind you — the portal simply refuses them and tells them what they are short on. Leaving is free, and there is an already-lit portal home by the north gate. Packs can raise, lower or drop the requirement in the config.

The dimension runs an active **difficulty profile** that scales every mob in it: health, damage, speed, armour, knockback resistance, follow range, dropped XP, coins, loot-bag chance, permanent potion effects and equipment rolls. Four profiles ship with the mod (`easy`, `normal`, `hard`, `nightmare`) and it scales further with the number of players online.

**Spawners** are placed in a ring across the arena floor when it's built, one mob type each so the fight stays varied — twelve by default, from zombies and creepers to blazes, witches and endermen. Which types appear, how many spawners there are and every spawner setting (delays, mobs per batch, player range, spawn radius) live in the config, and unknown ids are skipped with a log warning so you can list mobs from other mods safely. Don't want them? Set `enabled = false` and run `/uma spawners clear` to wipe the ones already placed. `/uma spawners place` puts them back.

### Argentine coins

Nine denominations: **10, 20, 50, 100, 200, 500, 1000, 10000 and 20000**. From 10 to 500 they're _mangos_, from 1000 up they're _lucas_.

No conversion recipes. Right-click a coin and it sweeps every loose coin in your inventory into the fewest possible coins; sneak + right-click breaks one down into smaller change. The **Exchange Stand** takes any combination of coins and gives correct change automatically.

### Loot bags

Eight tiers: **common, uncommon, epic, mythic, legendary, omega, food and enchantment**. Right-click to open one, sneak to open the whole stack. Bags can roll guaranteed loot, a weighted random pool, vanilla loot tables, and **other bags** nested inside them.

Only common and uncommon can be bought at the Exchange Stand. The Coliseum Key can never be bought.

### Commands

`/uma` (or the shorter `/ums`):

*   **`/uma tps`** — real server performance, because most mods report it wrong: average TPS and MSPT over the last 100 ticks against the 50 ms budget, the worst single tick in that window, and MSPT broken down **per dimension**.
*   **`/uma ping [players]`** — ping for yourself or anyone else.
*   **`/uma spawners list | place | clear`** — count, re-place or wipe the arena spawners.
*   Plus `coliseum`, `difficulty`, `bag` and `rebuild_arena`.

***

## Everything is a datapack

The whole content side of the mod is JSON under `data/<your_namespace>/uma/…`, reloaded with `/reload`. The namespace is yours — the mod only cares about the folder.

| Folder                  |What it controls                                                                                      |
| ----------------------- |----------------------------------------------------------------------------------------------------- |
| <code>uma/difficulty/</code> |Mob scaling profiles: health, damage, armour, dropped XP, coins, permanent potion effects, gear rolls |
| <code>uma/mob_rules/</code> |Which mob uses which difficulty, in which dimensions, and what it drops                               |
| <code>uma/loot_bags/</code> |The contents of each bag                                                                              |
| <code>uma/loot_bag_additions/</code> |Extra drops poured into bags that already exist                                                       |
| <code>uma/trades/</code> |Exchange Stand offers                                                                                 |

**Adding loot to the bags is the part most packs want.** A file in `uma/loot_bag_additions/` adds to bags that already exist without rewriting them, so your entries stack on top of the mod's or on top of another pack's. You can target one bag, a list of bags, a whole rarity, or every bag at once, and add both guaranteed loot and weighted random entries.

> **Recommended: [OpenLoader](https://www.curseforge.com/minecraft/mc-mods/open-loader)**
> 
> A normal datapack lives inside one world's `datapacks/` folder, which means you have to copy it into every new world. OpenLoader lets you put the same files in **`config/openloader/data/`** and they load in _every_ world automatically — much better for a modpack or a server.

**KubeJS works too.** If it is installed the mod adds a global `UMA` binding with a chained-builder API for all five folders — `UMA.difficulty()`, `UMA.mobRule()`, `UMA.lootBag()`, `UMA.bagDrop()` and `UMA.trade()`. It goes in `kubejs/server_scripts/`, because the data loads with the datapack. Bad entries are logged with their id and skipped, so one typo never breaks the rest of the pack.

**The wiki has all of it**: every field of every file with its type and default, the whole KubeJS API method by method, and a complete worked example — one file of each kind, in JSON and then the same thing in KubeJS.

***

## Requeriments:

· Minecraft 1.21.1

· NeoForge 21.1.240

## Looking for the skill system?

It used to live here and now has a mod of its own: **Ultimate Skills**, a node tree where every node, connection, bonus and effect is defined in JSON or KubeJS. The two mods are independent — install either one on its own, or both.