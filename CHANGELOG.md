# Changelog

## Ultimate Mod Additions 1.0.2 — 2026-10-16

### Additions

- **Argentine money.** Seven coins and ten bills with textures cut from the real ones:
  `5_centavos`, `10_centavos`, `25_centavos`, `50_centavos`, `2_pe`, `5_pe`, `10_pe`, bills
  `2_mangos` to `100_mangos` and `1_lucas`, `2_lucas`, `10_lucas`, `20_lucas`. 1 peso is 1 mango and
  1 luca is 1000 mangos; balances are shown as "1 luca 688 mangos". Coins and bills of the same
  number are worth the same.
- **Wallet.** Right click to open it. It holds a balance instead of item slots, so it can keep up to
  100 quintillion (`[wallet] max_balance_mangos`): deposit all your loose cash with one button and
  withdraw set amounts back as bills and coins. The balance counts toward everything you buy. Opening
  an empty one says "esta re seco xD". Withdraw buttons, a per-click withdraw cap, whether loose cash
  is spent first and how many wallets count are all in `[wallet]`.
- **ATM and debit card.** A new ATM block (iron, glass and redstone) takes your Visa debit card in its
  slot and manages your bank account: deposit all your cash and wallets, withdraw set amounts, or ask
  for a new card (the price is `[bank] card_price_mangos`). Accounts are per player, hold up to
  `[bank] max_balance_mangos`, and the card only works for its owner.
- **Real 3D models.** The debit card, the wallet, the ATM and the exchange stand use the exact models
  supplied, converted to OBJ and loaded through NeoForge's OBJ loader. The wallet scan is reduced to
  about 30,000 triangles (from 243,000) and textures are capped at 1024 px; everything else is as it
  came. The ATM and the exchange stand now face the way they are placed.
- **Pay at the exchange stand with the card.** With your card in the inventory the stand can charge
  your account: `[bank] stall_card_mode` is `FALLBACK` (cash first), `FIRST` or `OFF`.
- **Claim rent (FTB Chunks and FTB Teams).** Every team that holds claims pays rent every `[rent] period_days`
  (real days, or game days with `use_game_days`): a flat `base_price_mangos` plus `price_per_chunk_mangos`
  for each claimed chunk, taken from the owner's bank account and from the other members if needed. The
  first period is free, the team is warned `warn_days` before, and a failed payment starts a grace period
  of `grace_days` after which every claim is removed. The rent pauses while the server is off. `/rent`
  shows the state, pays early, and operators can `extend` or `reset` it.
- **FTB Quests money reward.** A new reward type, Money, pays an amount in mangos into the player's bank
  account or as loose cash.
- **`/shop`.** Opens the exchange stand from anywhere. Permission level, and whether you must be next to a
  stand, are in `[shop]`.
- **Jobs and rent screens.** `/jobs` now opens a screen with every job, its level and experience bar,
  Join and Leave buttons, and a tooltip listing what it pays; `/jobs list` keeps the chat version. `/rent`
  opens a screen with the team's claims, the price, the countdown and a Pay now button; `/rent status`
  keeps the chat version.
- **`/jobs`.** Miner, woodcutter, farmer, hunter and fisher jobs defined in `uma/jobs/*.json`. Players hold
  up to `[jobs] max_per_player`, level up, and earn more per level. Pay goes to the bank account or as cash
  (`[jobs] pay_to`), and income and experience multipliers are in the config.
- **`/money`.** `/money balance [player]`, `/money pay <player> <amount>` and, for operators,
  `/money give|take|set <players> <amount>` on the ATM account. Amounts are in mangos with optional
  decimals; the suffix `l` means lucas, so `1.5l` is 1500 mangos.
- **Preview tooltip.** Hovering a coin, a bill or the wallet shows a large picture of it
  (`[coins] show_preview_tooltip`).
- **Money from mobs, everywhere.** Every mob rolls each denomination on its own, from 70 % for 5
  centavos down to 1 % for lucas. Chances, a global multiplier, a boss multiplier, the count per roll
  and a dimension blacklist live in `[coins]`.
- **Money in loot chests.** A 0.01 % chance (`[coins.chests] chance`) of one bill or coin in any loot
  chest of any mod, Lootr chests included, with table whitelist and blacklist regexes.
- **El Tío To Flashero.** A villager profession that works at the exchange stand and swaps coins for
  bills and the other way round. Name, uses, experience and the trade list are in `[villager]`.
- **Exchange stand.** With no money the balance reads "estás re seco xD" instead of 0.

### Changes

- **The nine generic coins are gone.** `coin_10` … `coin_20000` are aliased to the closest new
  denomination by value when a world loads, so `coin_200` and `coin_500` become `100_mangos`.
- **Balances are stored in centavos.** Trade prices in datapacks stay in mangos.
- **`[coins]` config.** `only_in_coliseum` and `only_from_player_kills` now default to false, and
  `drop_chance`, `min_per_kill` and `max_per_kill` are replaced by per-denomination chances. Loot
  bags have their own `only_from_player_kills`.
- **Recipes, item models, block states, the exchange stand loot table, tags, the loot modifier and the
  Coliseum dimension are generated** by datagen instead of written by hand.
- **Loot bags and mob rules** use the new ids: `"tier": "10_mangos"`, `"1_lucas"` and so on. Numeric
  tiers and `bronze`, `silver`, `gold`, `platinum` still work.

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
