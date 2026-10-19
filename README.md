## Ultimate Mod Additions

A content mod built around one idea: **nothing is hardcoded**. The coliseum, the mobs that spawn in it, what they drop, what the shop sells — all of it lives in JSON files that any datapack can override, or in KubeJS scripts if you'd rather write code.

***

### The Coliseum

A dedicated dimension (`ultimatemodadditions:coliseum`) with a real Roman amphitheatre — not a flat world. The mod builds it procedurally the first time the dimension loads: sand arena, podium wall, five tiers of stands, an outer wall with two levels of arches, four gates and a return portal. Fall off the edge and you get put back on the sand instead of dying in the void.

**Getting in is a build and a gate.** You raise a portal frame exactly like a nether portal, but out of **crying obsidian** — 4×5 at minimum, up to 23×23, corners included. Then you right-click each of the four corners with a **Coliseum Key**; the game counts them off for you, and on the fourth the key is consumed and the portal lights up.

Even lit, it will not take just anyone. To step through you need **100 experience levels**, which you keep. Everyone is checked individually as they enter, so you cannot drag an underprepared friend in behind you — the portal simply refuses them and tells them what they are short on. Leaving is free, and there is an already-lit portal home by the north gate. Packs can raise, lower or drop the requirement in the config.

The dimension runs an active **difficulty profile** that scales every mob in it: health, damage, speed, armour, knockback resistance, follow range, dropped XP, coins, loot-bag chance, permanent potion effects and equipment rolls. Four profiles ship with the mod (`easy`, `normal`, `hard`, `nightmare`) and it scales further with the number of players online.

**Spawners** are placed in a ring across the arena floor when it's built, one mob type each so the fight stays varied — twelve by default, from zombies and creepers to blazes, witches and endermen. Which types appear, how many spawners there are and every spawner setting (delays, mobs per batch, player range, spawn radius) live in the config, and unknown ids are skipped with a log warning so you can list mobs from other mods safely. Don't want them? Set `enabled = false` and run `/uma spawners clear` to wipe the ones already placed. `/uma spawners place` puts them back.

### Argentine money

Seven coins and ten bills with the real Argentine artwork. Coins: **5, 10, 25 and 50 centavos** and **2, 5 and 10 pesos**. Bills: **2, 5, 10, 20, 50 and 100 mangos** and **1, 2, 10 and 20 lucas**. One peso is one mango and one luca is 1000 mangos; balances read like "1 luca 688 mangos". A coin and a bill of the same number are worth the same.

No conversion recipes. Right-click a coin or bill and it sweeps every loose piece of cash in your inventory into the fewest possible; sneak + right-click breaks one down into smaller change. The **Exchange Stand** takes any combination and gives correct change automatically, and shows "estás re seco xD" when you have nothing.

The **wallet** (leather and a gold ingot) keeps a balance of up to 100 quintillion that counts toward everything you buy. The **ATM** (iron, glass and redstone) takes your personal **Visa debit card** and manages your bank account, which the exchange stand can charge too; `/uma money` checks, pays and, for operators, gives money to an account. Mobs drop coins and bills anywhere, from 70 % for 5 centavos down to 1 % for lucas, loot chests of any mod (Lootr included) can hide one at 0.01 %, and **El Tío To Flashero**, a villager who works at the exchange stand, swaps coins for bills. Every chance, count, slot and trade is in the config.

### Loot bags

Eight tiers: **common, uncommon, epic, mythic, legendary, omega, food and enchantment**. Right-click to open one, sneak to open the whole stack. Bags can roll guaranteed loot, a weighted random pool, vanilla loot tables, and **other bags** nested inside them.

Only common and uncommon can be bought at the Exchange Stand. The Coliseum Key can never be bought.

### Commands

`/uma` (or the shorter `/ums`):

*   **`/uma tps`** — real server performance, because most mods report it wrong: average TPS and MSPT over the last 100 ticks against the 50 ms budget, the worst single tick in that window, and MSPT broken down **per dimension**.
*   **`/uma ping [players]`** — ping for yourself or anyone else.
*   **`/uma spawners list | place | clear`** — count, re-place or wipe the arena spawners.
*   Plus `coliseum`, `difficulty`, `bag` and `rebuild_arena`.

And on their own:

*   **`/uma help [section]`** — says what each command is for; it only lists the ones you can use.
*   **`/uma money balance | pay | give | take | set`** — bank accounts. Operators can give, take or set money on any account.
*   **`/uma shop`** — opens the exchange stand from anywhere (can be limited to the stand's surroundings in the config).
*   **`/uma jobs`** — list, join and leave jobs, with clickable buttons. Jobs pay for breaking blocks, killing mobs and fishing, level up and pay more. Each one is a JSON file in `uma/jobs/`.
*   **`/uma rent`** — with FTB Chunks and FTB Teams: your team's claim rent, `status`, `pay`, and for operators `extend` and `reset`.

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

## Credits

3D models, converted to OBJ and loaded with NeoForge's OBJ loader:

· **Credit card** by Ayat Djadja, CC BY 4.0: https://sketchfab.com/3d-models/credit-card-4c4117d766f342cc9e0fa3d79e9de654

· **Market stall** by Kisielev Mikhail, CC BY 4.0: https://sketchfab.com/3d-models/market-stall-6bb15dc7056241bfac6ee747fc866e45

· **ATM terminal** and **wallet**, supplied by the mod author.

The conversion scripts are in `tools/model-conversion`.

## Looking for the skill system?

It used to live here and now has a mod of its own: **Ultimate Skills**, a node tree where every node, connection, bonus and effect is defined in JSON or KubeJS. The two mods are independent — install either one on its own, or both.