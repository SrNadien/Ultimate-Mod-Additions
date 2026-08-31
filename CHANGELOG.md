# Changelog

## Ultimate Mod Additions 1.0.1 — 2026-08-31

### Additions

- **Coliseum portal.** A frame built exactly like a nether portal but out of **crying obsidian**,
  from 4×5 up to 23×23. Right-click each of the four corners with the Coliseum Key; on the fourth
  the key is consumed and the portal lights up. Breaking a frame block puts it out, like a nether
  portal. The arena has an already-lit portal home next to the north gate.
- **Portal requirement.** Going through demands vanilla experience levels, checked per player, so
  nobody can be dragged in. Configurable in the new `[coliseum.portal]` section.
- **Arena spawners.** Twelve spawners are placed in a ring across the arena floor when it is built,
  one mob type each so the fight stays varied. Every setting lives in the new `[coliseum.spawners]`
  section: which entities, how many, spawn delays, mobs per batch, nearby cap, player range and
  spawn radius. Entity ids that do not exist are skipped with a warning instead of breaking.
- **`/uma spawners list | place | clear`.** Count the spawners in the coliseum, re-place them from
  the config, or wipe every one of them including hand-placed ones.
- **Spanish.** `es_es`, `es_mx` and `es_ar` all ship now. Minecraft does not fall back between
  Spanish variants, so each one is emitted separately.

### Changes

- **The skill system moved to its own mod: Ultimate Skills.** Everything skill-related is gone from
  here — the eight skills, skill experience, skill requirements, the skill tome, the skills screen,
  the `K` keybind, `/uma skills`, the `[skills]` config section and the `UMA.skill()`,
  `UMA.skillXp()` and `UMA.skillRequirement()` KubeJS builders. The two mods are independent now.
- **Trades no longer take `requirements`.** The field is simply ignored, so older datapacks still
  load; the requirement just stops applying.
- **The Coliseum Gateway block is gone.** Its job is done by the crying obsidian portal, so the
  block, its recipe and its item were removed.
- **Loot bags no longer drop the skill tome.** The five bags that carried it now hand out
  experience bottles instead, scaled by rarity: 2–4 in uncommon up to 16–32 in omega.
- **Language files are generated.** Both locales come out of a `LanguageProvider` in
  `datagen/providers/` instead of hand-written JSON.

### Fixes

- **Coins would not merge or split when you were aiming at a block.** Only `use` was implemented,
  which Minecraft calls solely when you are *not* pointing at a block — so it worked aiming at the
  sky and did nothing the rest of the time. `useOn` now runs the same logic.
- **The coliseum could load as an empty dimension.** The "arena already built" flag survived
  independently of the dimension, so after a rename the flag said built while the new dimension was
  untouched. The mod now checks that the arena is really there and rebuilds it if it is not.
- **The exchange screen showed the blurred world through its panel.** The panel was translucent, so
  vanilla's menu blur bled through it. The panel is opaque now, the screen no longer blurs the
  world behind it, and the text has shadows.

---

## Ultimate Mod Additions 1.0.0

### Additions

- **Coliseum dimension** with a Roman amphitheatre the mod builds procedurally: sand arena, podium
  wall, five tiers of stands, an outer wall with two decks of arches, four gates and lighting. Fall
  off the edge and you are put back on the sand instead of dying in the void.
- **Difficulty profiles** (`uma/difficulty/`) that scale every mob in the dimension: health, damage,
  speed, armour, toughness, knockback resistance, follow range, dropped experience, coins, loot-bag
  chance, permanent potion effects and equipment rolls. `easy`, `normal`, `hard` and `nightmare`
  ship with the mod, and it scales further with the number of players online.
- **Mob rules** (`uma/mob_rules/`) deciding which mob uses which difficulty, in which dimensions,
  and what it drops — items, whole loot tables, coins or bags, with looting bonuses and item
  components.
- **Argentine currency**: nine denominations from 10 to 20000, *mangos* below 1000 and *lucas* above.
  Right-click merges all your loose change into the fewest coins; sneak + right-click breaks one
  into smaller ones.
- **Loot bags** in eight rarities, from common to enchantment. Guaranteed loot, a weighted random
  pool, vanilla loot tables and other bags nested inside them. Right-click opens one, sneak opens
  the stack.
- **Loot bag additions** (`uma/loot_bag_additions/`) that pour extra drops into bags that already
  exist without rewriting them, targeting one bag, a list, a whole rarity or every bag at once.
- **Exchange stand** with offers from `uma/trades/`, paid with any mix of coins and returning
  change. Only common and uncommon bags can be bought, and anything in the
  `ultimatemodadditions:not_purchasable` tag is refused.
- **KubeJS integration**: a global `UMA` binding with chained builders for every data type, injected
  again on each `/reload`.
- **`/uma tps`** reporting what most mods report badly: average TPS and MSPT over the last 100 ticks
  against the 50 ms budget, the worst single tick in that window, and MSPT broken down per
  dimension.
- **`/uma ping`**, plus `coliseum`, `difficulty`, `bag` and `rebuild_arena`.
- **JEI integration** showing bag contents and stand offers.
