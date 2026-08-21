# Ultimate Mod Additions Wiki

NeoForge 21.1.240 · Minecraft 1.21.1 · mod id `ultimatemodadditions`

Every piece of content in this mod is defined in JSON and, if KubeJS is installed, in scripts too.
Nothing is hardcoded: difficulties, drops, bags, trades and skills can all be changed from a
datapack without recompiling anything.

The examples on this page use the namespace **`matdien_teaks:`**. Use your own — the mod never
looks at the namespace, only at the folder.

## Index

| Section | What it covers |
| --- | --- |
| [Datapacks](#datapacks) | Where the files go, how they reload, shared types |
| [Difficulties](#difficulties) | `uma/difficulty` — mob scaling |
| [Mob rules](#mob-rules) | `uma/mob_rules` — what each mob drops and how hard it is |
| [Loot bags](#loot-bags) | `uma/loot_bags` and `uma/loot_bag_additions` |
| [Trades](#trades) | `uma/trades` — the exchange stand |
| [Skills](#skills) | `uma/skills`, `uma/skill_xp`, `uma/skill_requirements` |
| [Skill tables](#skill-tables) | What every level of the eight built-in skills gives |
| [Currency](#currency) | The nine denominations and how they work |
| [Coliseum](#coliseum) | The dimension, the amphitheatre, the portal and the key |
| [KubeJS](#kubejs) | The `UMA` binding and all of its methods |
| [Commands](#commands) | `/uma` and `/ums` |
| [Configuration](#configuration) | The config TOML |
| [Full example](#full-example) | One file of every kind, in JSON and in KubeJS |

## What the mod adds

| Thing | Id |
| --- | --- |
| Coliseum dimension | `ultimatemodadditions:coliseum` |
| Coliseum portal | `ultimatemodadditions:coliseum_portal` |
| Exchange stand | `ultimatemodadditions:exchange_stand` |
| Coliseum key | `ultimatemodadditions:coliseum_key` |
| Skill tome | `ultimatemodadditions:skill_tome` |
| Coins | `coin_10` … `coin_20000` (nine denominations) |
| Bags | eight tiers, from `loot_bag_common` to `loot_bag_enchantment` |

---

## Datapacks

### Where the files go

Everything lives under `data/<any_namespace>/uma/…`. The namespace does not matter, use your own.
Three places work:

- a normal datapack, in `world/datapacks/matdien_teaks/data/matdien_teaks/uma/…`
- the KubeJS folder, in `kubejs/data/matdien_teaks/uma/…`
- the mod itself, if you are building it

They reload with `/reload`. No restart needed.

### Folders

| Folder | What it defines |
| --- | --- |
| `uma/difficulty/` | Mob scaling profiles |
| `uma/mob_rules/` | Which mob uses which difficulty, and what it drops |
| `uma/loot_bags/` | The contents of each bag |
| `uma/loot_bag_additions/` | Extra drops added to bags that already exist |
| `uma/trades/` | Exchange stand offers |
| `uma/skills/` | Skills and their perks |
| `uma/skill_xp/` | Where each skill's experience comes from |
| `uma/skill_requirements/` | What is locked until a given level |

### Shared types

#### Integer range

Wherever this page says *range*, you can write a plain number or an object:

```json
"count": 3
"count": { "min": 1, "max": 4 }
```

#### `match` — item and block filter

```json
"match": {
  "items": ["minecraft:diamond_pickaxe"],
  "tags": ["c:tools/pickaxe"],
  "blocks": ["minecraft:stone"],
  "block_tags": ["minecraft:mineable/pickaxe"],
  "regex": ["^minecraft:.*_ore$"],
  "exclude": ["minecraft:bedrock"]
}
```

- `items` and `tags` are checked against items; `blocks` and `block_tags` against blocks.
- `regex` runs on the full id (`minecraft:diamond`) and is only consulted when nothing matched by
  list or tag.
- `exclude` beats everything else.

#### `entity` — entity filter

```json
"entity": {
  "entities": ["minecraft:zombie"],
  "tags": ["minecraft:skeletons"],
  "regex": ["^minecraft:.*_zombie$"],
  "categories": ["monster"],
  "exclude": ["minecraft:villager"],
  "boss": true,
  "baby": false
}
```

- `categories` accepts `monster`, `creature`, `ambient`, `water_creature`, `axolotls`,
  `underground_water_creature`, `water_ambient` and `misc`.
- `boss` looks at the `c:bosses` tag.
- If you leave `entities`, `tags` and `regex` empty it matches **any** entity that passes the
  remaining filters.

#### `components`

Any entry that hands out an item accepts `components`, using the 1.21 component format:

```json
{
  "item": "minecraft:enchanted_book",
  "components": {
    "minecraft:stored_enchantments": { "levels": { "minecraft:mending": 1 } }
  }
}
```

---

## Difficulties

`data/<namespace>/uma/difficulty/<id>.json`

A difficulty profile is a bundle of multipliers applied to a mob when it spawns. `easy`, `normal`,
`hard` and `nightmare` ship with the mod.

```json
{
  "name": "Hard",
  "health_multiplier": 2.5,
  "damage_multiplier": 1.75,
  "speed_multiplier": 1.1,
  "armor_bonus": 4.0,
  "armor_toughness_bonus": 2.0,
  "knockback_resistance": 0.2,
  "follow_range_bonus": 12.0,
  "experience_multiplier": 2.0,
  "coin_multiplier": 2.0,
  "bag_chance_bonus": 0.05,
  "extra_loot_rolls": 1,
  "glowing": false,
  "effects": [
    { "effect": "minecraft:strength", "amplifier": 0, "duration": -1, "chance": 1.0 }
  ],
  "equipment": {
    "mainhand": { "item": "minecraft:netherite_sword", "chance": 0.35, "drop_chance": 0.02 }
  }
}
```

### Fields

| Field | Type | Default | What it does |
| --- | --- | --- | --- |
| `name` | string | — | Display name, optional |
| `health_multiplier` | double | 1.0 | Multiplies max health |
| `damage_multiplier` | double | 1.0 | Multiplies attack damage |
| `speed_multiplier` | double | 1.0 | Multiplies movement speed |
| `armor_bonus` | double | 0.0 | Adds armor |
| `armor_toughness_bonus` | double | 0.0 | Adds armor toughness |
| `knockback_resistance` | double | 0.0 | Adds knockback resistance (0–1) |
| `follow_range_bonus` | double | 0.0 | Adds follow range |
| `experience_multiplier` | double | 1.0 | Multiplies dropped experience |
| `coin_multiplier` | double | 1.0 | Multiplies dropped coins |
| `bag_chance_bonus` | double | 0.0 | Adds to the loot bag chance |
| `extra_loot_rolls` | int | 0 | Extra loot rolls |
| `glowing` | boolean | false | Leaves the mob glowing |
| `effects` | list | empty | Permanent or timed potion effects |
| `equipment` | object | empty | Gear per slot |

### `effects`

```json
{ "effect": "minecraft:strength", "amplifier": 0, "duration": -1, "chance": 1.0 }
```

`duration: -1` is permanent. `chance` runs from 0 to 1.

### `equipment`

The keys are slots: `mainhand`, `offhand`, `head`, `chest`, `legs`, `feet`.

```json
"equipment": {
  "head":  { "item": "minecraft:netherite_helmet", "chance": 0.5, "drop_chance": 0.01 },
  "chest": { "item": "minecraft:netherite_chestplate", "chance": 0.5 }
}
```

| Field | Default | What it does |
| --- | --- | --- |
| `item` | required | What gets equipped |
| `count` | 1 | Amount |
| `chance` | 1.0 | Chance the mob carries it |
| `drop_chance` | 0.085 | Chance it drops on death |
| `components` | — | Item components |

### How the difficulty is chosen

1. If the matching mob rule carries `difficulty`, that profile is used.
2. If it carries `inline_difficulty`, that inline profile is used.
3. Otherwise the Coliseum's active difficulty is used (`/uma difficulty set`).

See [Mob rules](#mob-rules).

---

## Mob rules

`data/<namespace>/uma/mob_rules/<id>.json`

They decide which difficulty each mob uses and what it drops when it dies. They are checked from
highest to lowest `priority`; the first one that matches wins.

```json
{
  "priority": 0,
  "match": { "categories": ["monster"] },
  "dimensions": ["ultimatemodadditions:coliseum"],
  "difficulty": "ultimatemodadditions:hard",
  "replace_vanilla_drops": true,
  "experience_multiplier": 2.0,
  "stop_matching": false,
  "drops": [
    { "item": "minecraft:diamond", "count": { "min": 1, "max": 3 }, "chance": 0.5, "looting_bonus": 0.1 },
    { "loot_table": "minecraft:chests/simple_dungeon", "chance": 0.1 },
    { "bag": "ultimatemodadditions:epic", "chance": 0.02 }
  ],
  "coins": [ { "tier": "500", "count": { "min": 1, "max": 3 }, "chance": 0.5 } ],
  "bags":  [ { "bag": "ultimatemodadditions:common", "chance": 0.06 } ]
}
```

### Fields

| Field | Type | Default | What it does |
| --- | --- | --- | --- |
| `priority` | int | 0 | Higher is checked first |
| `match` | entity filter | anything | Which mobs it applies to |
| `dimensions` | list of ids | empty = all | Which dimensions it applies in |
| `difficulty` | id | — | Difficulty profile to use |
| `inline_difficulty` | object | — | An inline profile instead of `difficulty` |
| `replace_vanilla_drops` | boolean | false | Wipes the mob's vanilla loot |
| `drops` | list | empty | Custom loot |
| `coins` | list | empty | Coins it drops |
| `bags` | list | empty | Bags it drops |
| `experience_multiplier` | double | 1.0 | Multiplies dropped experience |
| `stop_matching` | boolean | false | Stops the search even if nothing applies |

The filter goes in `match` and uses the [entity filter](#entity--entity-filter) format.

### `drops`

Every entry can hand out an item, a loot table or a bag.

| Field | Default | What it does |
| --- | --- | --- |
| `item` | — | Item to drop |
| `count` | 1 | Amount (range) |
| `chance` | 1.0 | Chance, 0 to 1 |
| `weight` | 1 | Only used inside bags |
| `looting_bonus` | 0.0 | Added to `chance` per level of Looting |
| `components` | — | Item components |
| `loot_table` | — | Rolls a whole loot table |
| `bag` | — | Hands out a bag by id |

If `bag` is present, `item` is ignored.

### `coins`

```json
{ "tier": "500", "count": { "min": 1, "max": 3 }, "chance": 0.5 }
```

`tier` is the denomination: `"10"`, `"20"`, `"50"`, `"100"`, `"200"`, `"500"`, `"1000"`, `"10000"`
or `"20000"`. The old names `bronze`, `silver` and `gold` are still accepted and mean 10, 100 and
1000.

The final amount is multiplied by the difficulty's `coin_multiplier`.

### `bags`

```json
{ "bag": "ultimatemodadditions:common", "count": 1, "chance": 0.06 }
```

The difficulty's `bag_chance_bonus` is added to `chance`.

---

## Loot bags

### `uma/loot_bags/<id>.json`

Every file is one bag. Make as many as you like: `tier` decides which of the eight items it rides
on, and with that its texture and rarity.

```json
{
  "name": "Epic Bag",
  "tier": "epic",
  "color": 12213503,
  "sort_order": 30,
  "rolls": { "min": 3, "max": 5 },
  "unique_rolls": true,
  "announce": false,
  "open_sound": "ultimatemodadditions:loot_bag_open",
  "guaranteed": [
    { "item": "ultimatemodadditions:coin_500", "count": { "min": 1, "max": 3 } }
  ],
  "entries": [
    { "item": "minecraft:diamond_block", "count": 1, "weight": 16 },
    { "bag": "ultimatemodadditions:enchantment", "count": 1, "weight": 8 }
  ]
}
```

| Field | Type | Default | What it does |
| --- | --- | --- | --- |
| `name` | string | — | Display name of the bag |
| `tier` | string | `common` | Rarity, see the table below |
| `rolls` | range | 1 | How many entries get rolled |
| `guaranteed` | list | empty | Fixed loot, always drops |
| `entries` | list | empty | Random pool, rolled by `weight` |
| `unique_rolls` | boolean | false | Stops the same entry rolling twice |
| `color` | int | 0xFFFFFF | Decimal colour for the name |
| `open_sound` | id | — | Sound played when opened |
| `announce` | boolean | false | Tells the whole server when it is opened |
| `sort_order` | int | 0 | Order in JEI |

Entries use the same format as the `drops` of a [mob rule](#drops), including `loot_table` and
`bag` for nested bags.

### Tiers

| `tier` | Base item | Buyable at the stand |
| --- | --- | --- |
| `common` | `loot_bag_common` | yes |
| `uncommon` | `loot_bag_uncommon` | yes |
| `epic` | `loot_bag_epic` | no |
| `mythic` | `loot_bag_mythic` | no |
| `legendary` | `loot_bag_legendary` | no |
| `omega` | `loot_bag_omega` | no |
| `food` | `loot_bag_food` | no |
| `enchantment` | `loot_bag_enchantment` | no |

`rare` is still accepted as an alias of `epic` for older datapacks.

### `uma/loot_bag_additions/<id>.json`

For **adding** loot to bags that already exist without rewriting them. They are applied after
every bag has loaded, so they work on the mod's bags, another datapack's, or KubeJS ones.

```json
{
  "bag": "ultimatemodadditions:common",
  "bags": ["ultimatemodadditions:uncommon"],
  "tiers": ["legendary", "omega"],
  "entries": [
    { "item": "minecraft:copper_ingot", "count": { "min": 2, "max": 6 }, "weight": 12 }
  ],
  "guaranteed": [
    { "item": "ultimatemodadditions:coin_50", "count": 1 }
  ],
  "replace": false
}
```

| Field | Default | What it does |
| --- | --- | --- |
| `bag` | — | One specific bag |
| `bags` | empty | Several specific bags |
| `tiers` | empty | Every bag of those rarities; `["all"]` reaches all of them |
| `entries` | empty | Added to the random pool |
| `guaranteed` | empty | Added to the fixed loot |
| `replace` | false | When `true`, wipes the previous contents before adding |

`bag`, `bags` and `tiers` add up: the addition applies to every bag that matches any of the three.

### Opening them

Right-click opens one. Sneak opens the whole stack. The loot goes to the inventory and whatever
does not fit drops on the floor.

---

## Trades

`data/<namespace>/uma/trades/<id>.json`

These are the offers of the **exchange stand** (`ultimatemodadditions:exchange_stand`).

```json
{
  "name": "Uncommon Bag",
  "category": "bags",
  "sort_order": 20,
  "price": 700,
  "cost": [ { "item": "minecraft:emerald", "count": 4 } ],
  "result": { "bag": "ultimatemodadditions:uncommon", "count": 1 },
  "requirements": [ { "skill": "ultimatemodadditions:magic", "level": 5 } ],
  "stock": -1,
  "enabled": true
}
```

| Field | Type | Default | What it does |
| --- | --- | --- | --- |
| `name` | string | the result's name | Label in the list |
| `category` | string | `general` | Free-form grouping |
| `price` | int | 0 | Price in money |
| `cost` | list | empty | Specific items that must also be paid |
| `result` | object | required | What is handed over |
| `sort_order` | int | 0 | Order in the list |
| `requirements` | list | empty | Skill levels demanded |
| `stock` | int | -1 | Reserved, -1 is unlimited |
| `enabled` | boolean | true | When `false` the offer does not show up |

### `price` and `cost`

`price` is paid with **any** mix of coins and gives change back. `cost` demands exact items. Both
can be used at once.

```json
"cost": [ { "item": "minecraft:emerald", "count": 4 } ]
```

### `result`

```json
"result": { "item": "minecraft:golden_apple", "count": 1 }
"result": { "bag": "ultimatemodadditions:common", "count": 1 }
```

| Field | Default | What it does |
| --- | --- | --- |
| `item` | — | Item to hand over |
| `count` | 1 | Amount |
| `bag` | — | Hands over a bag by id; beats `item` |
| `components` | — | Item components |

### What cannot be bought

The stand **rejects** certain offers even if you define them, and does not show them in the screen
or in JEI:

- Bags of rarity `epic` or above, plus the food and enchantment ones. Only `common` and `uncommon`
  can be bought.
- Anything in the `ultimatemodadditions:not_purchasable` tag. The coliseum key is in there by
  default.

To allow or block more things, edit that tag from a datapack:

```json
{
  "replace": false,
  "values": ["matdien_teaks:my_item"]
}
```

### The trading screen

Right-click the stand to open it. Clicking a row buys one; shift-clicking buys eight. The top
right shows how much money you are carrying.

---

## Skills

Opened with **K**. There are two ways to level up and they coexist: spend points, or earn
experience by using the skill.

The concrete numbers for the eight built-in skills are in [Skill tables](#skill-tables).

### The skills screen

One row per skill: icon, name in its colour, `level / max`, the blue experience bar toward the next
level, and on the right a `+ N` button with what levelling up costs, green when you can afford it.
Clicking the row levels up; the mouse wheel scrolls if there are more skills than visible rows.

The row tooltip shows, in order: level, the description, the **bonus per level** of each trait with
how much you have accumulated right now, the experience, what the next level costs, and the full
perk list — unlocked ones in green, missing ones in grey.

All eight built-in skills have their traits set to `start_level: 2`, so **every level from 2 up
adds something**, and on top of that levels 5, 10, 15, 20, 25 and 30 unlock a perk.

### `uma/skills/<id>.json`

```json
{
  "name": "Mining",
  "icon": "minecraft:iron_pickaxe",
  "color": 11579576,
  "max_level": 32,
  "sort_order": 30,
  "base_point_cost": 1,
  "point_cost_growth": 0.15,
  "xp_base": 30,
  "xp_growth": 1.2,
  "description": ["Advanced picks and mining speed."],
  "traits": [
    { "attribute": "minecraft:player.block_break_speed", "per_level": 0.02,
      "operation": "add_multiplied_base", "start_level": 2 }
  ],
  "perks": [
    { "level": 5, "name": "Quick Pick", "description": ["+10% mining speed"],
      "attribute": "minecraft:player.block_break_speed", "amount": 0.1,
      "operation": "add_multiplied_base" }
  ]
}
```

| Field | Type | Default | What it does |
| --- | --- | --- | --- |
| `name` | string | required | Display name |
| `icon` | item id | `minecraft:book` | Icon in the list |
| `color` | int | 0xFFFFFF | Colour of the name |
| `max_level` | int | 32 | Cap, also limited by the config |
| `description` | list of strings | empty | Tooltip lines |
| `sort_order` | int | 0 | Order in the list |
| `base_point_cost` | int | 1 | Base cost in points |
| `point_cost_growth` | double | 0.0 | How much the cost climbs per level |
| `xp_base` | int | 0 | 0 = points only; above 0 turns experience on |
| `xp_growth` | double | 1.35 | How fast the experience requirement grows |
| `traits` | list | empty | Bonuses that scale with the level |
| `perks` | list | empty | Fixed bonuses that unlock |
| `effects` | list | empty | Permanent potion effects |

Point cost of level N: `base_point_cost + point_cost_growth * (N - 1)`.
Experience for level N: `xp_base * xp_growth^(N - 2)`.

#### `traits`

They scale with every level.

```json
{ "attribute": "minecraft:generic.attack_damage", "per_level": 0.2,
  "operation": "add_value", "start_level": 2 }
```

`operation` can be `add_value`, `add_multiplied_base` or `add_multiplied_total`.
`start_level` is the level it starts counting from.

#### `perks`

A fixed bonus that shows up on reaching `level`. It can have no `attribute` and be text only.

```json
{ "level": 10, "name": "Ore Sense", "description": ["+0.5 luck"],
  "attribute": "minecraft:generic.luck", "amount": 0.5, "operation": "add_value" }
```

#### `effects` — potion effects

Permanent potion effects, granted while you hold the level. They refresh every second, so they
survive death, dimension changes and `/effect clear`.

```json
"effects": [
  { "effect": "minecraft:haste", "level": 10 },
  { "effect": "minecraft:speed", "level": 15, "amplifier": 0,
    "levels_per_amplifier": 6, "max_amplifier": 2 },
  { "effect": "minecraft:fire_resistance", "level": 30, "particles": true, "icon": true }
]
```

| Field | Default | What it does |
| --- | --- | --- |
| `effect` | required | Effect id; any works, including modded ones |
| `level` | 1 | Skill level it is granted from |
| `amplifier` | 0 | Base amplifier; 0 is level I |
| `levels_per_amplifier` | 0 | Every this many levels above `level`, the amplifier goes up by one |
| `max_amplifier` | 9 | Amplifier cap |
| `particles` | false | Shows the effect's particles |
| `icon` | true | Shows the icon in the corner |

With `levels_per_amplifier` the example above gives Speed I at 15, II at 21, and stops there
because of `max_amplifier`.

If you already have the same effect at a **higher** amplifier from another source, the skill will
not override it. Effects show up in the skills screen tooltip, green for the ones you have and grey
for the ones you do not.

### `uma/skill_xp/<id>.json`

Where the experience comes from.

```json
{
  "skill": "ultimatemodadditions:mining",
  "actions": ["break"],
  "match": { "block_tags": ["minecraft:mineable/pickaxe"] },
  "amount": 2,
  "chance": 1.0,
  "priority": 0
}
```

| Action | When | What it is checked against |
| --- | --- | --- |
| `break` | breaking a block | `match` against the block |
| `place` | placing a block | `match` against the block |
| `craft` | crafting | `match` against the result |
| `smelt` | smelting | `match` against the result |
| `kill` | killing an entity | `entity` against the victim |
| `damage_taken` | taking damage | nothing; `amount` is multiplied by the damage |
| `sprint` | sprinting, once per second | nothing |

`chance` lets a rule grant experience only some of the time. Without `actions` it applies to all
of them.

### `uma/skill_requirements/<id>.json`

What is locked until a given level.

```json
{
  "priority": 10,
  "match": { "tags": ["c:tools/pickaxe"] },
  "entity": { "boss": true },
  "requirements": [ { "skill": "ultimatemodadditions:mining", "level": 8 } ],
  "actions": ["use", "attack", "harvest", "place", "equip", "craft", "attack_entity"]
}
```

| Action | What it blocks |
| --- | --- |
| `use` | right-clicking with the item |
| `attack` | swinging with the item in hand |
| `harvest` | breaking blocks (by item or by block) |
| `place` | placing the block |
| `equip` | wearing it; armour un-equips itself |
| `craft` | crafting it; the result is destroyed before reaching the inventory |
| `attack_entity` | hitting entities that match `entity` |

If you do not meet it, the action is cancelled and a message tells you what is missing.

### Points and perks

- Points are earned by gaining vanilla experience levels (`points_per_player_level`) and from the
  **skill tome**.
- **Total levels** grant extra hearts, controlled by `levels_per_bonus_heart` and
  `max_bonus_hearts`.
- The eight built-in skills: attack, defense, mining, gathering, building, farming, agility and
  magic.

---

## Skill tables

What every skill gives, level by level. These are the shipped values: any datapack or script can
change them, see [Skills](#skills).

All of them run from **level 1 to 32**. Level 1 is the starting level and gives nothing; **from 2
to 32 every level adds its bonus**, levels 5, 10, 15, 20, 25 and 30 unlock a named perk, and at two
more levels you gain a **permanent potion effect**.

### Effects

Each skill grants two effects, a different one at each level. They are permanent: they refresh
every second, so they survive death, dimension changes and `/effect clear`.

| Skill | First effect | Second effect |
| --- | --- | --- |
| Attack | 5 · **Strength I** | 25 · **Haste I** |
| Defense | 10 · **Resistance I** | 25 · **Fire Resistance** |
| Mining | 5 · **Haste I** | 20 · **Night Vision** |
| Gathering | 10 · **Luck I** | 25 · **Water Breathing** |
| Building | 15 · **Jump Boost I** | 30 · **Slow Falling** |
| Farming | 15 · **Luck I** | 30 · **Regeneration I** |
| Agility | 5 · **Speed I** | 20 · **Slow Falling** |
| Magic | 10 · **Night Vision** | 30 · **Fire Resistance** |

Some effects repeat across skills on purpose, but they **do not stack**: two sources of the same
effect at the same amplifier count as one. If you have the same effect stronger from somewhere
else, the skill will not override it.

They are changed from a datapack or KubeJS like anything else, see
[Skills](#effects--potion-effects).

### Summary

| Skill | Levels with | Bonus per level | At level 32 |
| --- | --- | --- | --- |
| [Attack](#attack) | killing enemies | +0.2 damage | +11.2 damage, +35% attack speed |
| [Defense](#defense) | taking damage | +0.2 armor | +12.2 armor, 55% knockback resistance |
| [Mining](#mining) | mining stone and ores | +2% mining speed | +127% mining speed, +1.5 luck |
| [Gathering](#gathering) | chopping and digging | +0.05 luck | +3.55 luck, +33% mining speed |
| [Building](#building) | placing blocks | +0.05 reach | +4.55 reach, +3 hearts |
| [Farming](#farming) | harvesting crops | +0.1 health | +7.55 hearts, +1.5 luck |
| [Agility](#agility) | sprinting | +0.5% speed | +29.5% speed, +6 safe fall |
| [Magic](#magic) | crafting magic items | +0.2 health | +9.1 hearts, +2 damage |

On top of all that come the **hearts from total levels**: every 12 levels summed across all skills
grant one extra heart, up to 10. See `levels_per_bonus_heart` in the
[configuration](#configuration).

### Cost to level

All eight can be levelled two ways at once: spending **points** or earning the skill's own
**experience**. Points come from gaining vanilla experience levels and from tomes.

| Skill | Cost at level 2 | Cost at level 32 | Points to reach 32 | XP at level 2 | XP at level 32 | Total XP |
| --- | --- | --- | --- | --- | --- | --- |
| Building | 1 | 4 | 82 | 25 | 3584 | 23,358 |
| Farming | 1 | 4 | 82 | 25 | 3584 | 23,358 |
| Mining | 1 | 6 | 106 | 30 | 7121 | 42,576 |
| Gathering | 1 | 6 | 106 | 30 | 7121 | 42,576 |
| Attack | 1 | 6 | 106 | 40 | 15,590 | 86,273 |
| Defense | 1 | 6 | 106 | 45 | 17,539 | 97,056 |
| Agility | 1 | 7 | 130 | 60 | 48,468 | 242,096 |
| Magic | 2 | 10 | 190 | 70 | 115,185 | 526,313 |

Magic and Agility are deliberately the most expensive. Attack, Defense and Magic at 32 are what the
[coliseum portal](#requirements-to-pass-through) demands: **402 points** or **709,642 skill
experience**, or a mix of both.

---

### Attack

Iron sword icon. Levels by **killing enemies**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.2 attack damage** per level (+6.2 total at 32) |
| 5 | **Firm Strike** — +1 attack damage |
| 10 | **Swift Edge** — +10% attack speed |
| 15 | **Fury** — +1.5 attack damage |
| 20 | **Reach** — +0.5 entity interaction range |
| 25 | **Executioner** — +2.5 attack damage |
| 30 | **Gladiator** — +25% attack speed |

**Effects:** level 5 Strength I · level 25 Haste I

**Total at 32:** +11.2 damage · +35% attack speed · +0.5 reach

### Defense

Iron chestplate icon. Levels by **taking damage**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.2 armor** per level (+6.2 total at 32) |
| 8 – 32 | **+1% knockback resistance** per level (+25% total at 32) |
| 5 | **Tough Skin** — +2 armor |
| 10 | **Endurance** — +2 armor toughness |
| 15 | **Roots** — +10% knockback resistance |
| 20 | **Rampart** — +4 armor |
| 25 | **Titan** — +2 hearts |
| 30 | **Unbreakable** — +20% knockback resistance |

**Effects:** level 10 Resistance I · level 25 Fire Resistance

**Total at 32:** +12.2 armor · +2 toughness · 55% knockback resistance · +2 hearts

It is the only skill with two per-level bonuses, and the second one only starts at level 8.

### Mining

Iron pickaxe icon. Levels by **mining stone and ores**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+2% mining speed** per level (+62% total at 32) |
| 5 | **Quick Pick** — +10% mining speed |
| 10 | **Ore Sense** — +0.5 luck |
| 15 | **Digger** — +20% mining speed |
| 20 | **Long Arm** — +1 block interaction range |
| 25 | **Rich Vein** — +1 luck |
| 30 | **Master Miner** — +35% mining speed |

**Effects:** level 5 Haste I · level 20 Night Vision

**Total at 32:** +127% mining speed · +1.5 luck · +1 reach

### Gathering

Iron axe icon. Levels by **chopping and digging**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.05 luck** per level (+1.55 total at 32) |
| 5 | **Quick Hands** — +8% mining speed |
| 10 | **Hunter's Eye** — +0.5 luck |
| 15 | **Carrier** — +1 heart |
| 20 | **Scout** — +3% movement speed |
| 25 | **Bountiful Loot** — +1.5 luck |
| 30 | **Master Gatherer** — +25% mining speed |

**Effects:** level 10 Luck I · level 25 Water Breathing

**Total at 32:** +3.55 luck · +33% mining speed · +1 heart · +3% speed

Luck drives vanilla loot tables, so this is the drop-farming skill.

### Building

Bricks icon. Levels by **placing blocks**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.05 block interaction range** per level (+1.55 total at 32) |
| 5 | **Scaffold** — +0.5 block interaction range |
| 10 | **Foundations** — +1 heart |
| 15 | **Architect** — +1 block interaction range |
| 20 | **Sure Footing** — +0.5 step height |
| 25 | **Master Builder** — +1.5 block interaction range |
| 30 | **Own Coliseum** — +2 hearts |

**Effects:** level 15 Jump Boost I · level 30 Slow Falling

**Total at 32:** +4.55 block interaction range · +3 hearts · +0.5 step height

At +4.55 the reach goes from 4.5 to over 9 blocks: it is the skill that changes building the most.

### Farming

Iron hoe icon. Levels by **harvesting crops**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.1 max health** per level (+3.1 total at 32) |
| 5 | **Harvester** — +5% mining speed |
| 10 | **Good Pasture** — +1 heart |
| 15 | **Abundance** — +0.5 luck |
| 20 | **Farmer** — +2 hearts |
| 25 | **Golden Harvest** — +1 luck |
| 30 | **Lord of the Field** — +3 hearts |

**Effects:** level 15 Luck I · level 30 Regeneration I

**Total at 32:** +7.55 hearts · +1.5 luck · +5% mining speed

It is the cheapest to level and the second biggest health boost.

### Agility

Feather icon. Levels by **sprinting**, once per second.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.5% movement speed** per level (+15.5% total at 32) |
| 5 | **Light Feet** — +2% movement speed |
| 10 | **Landing** — +2 blocks of safe fall |
| 15 | **Sprint** — +4% movement speed |
| 20 | **High Step** — +0.5 step height |
| 25 | **Soft Fall** — +4 blocks of safe fall |
| 30 | **Shadow** — +8% movement speed |

**Effects:** level 5 Speed I · level 20 Slow Falling

**Total at 32:** +29.5% movement speed · +6 blocks of safe fall · +0.5 step height

With +6 safe fall you survive a 9 block drop instead of 3.

### Magic

Enchanting table icon. Levels by **crafting magic items**.

| Level | What it gives |
| --- | --- |
| 2 – 32 | **+0.2 max health** per level (+6.2 total at 32) |
| 5 | **Arcane Spark** — +1 heart |
| 10 | **Aura** — +0.5 luck |
| 15 | **Vitality** — +2 hearts |
| 20 | **Arcane Eye** — +1 block interaction range |
| 25 | **Power** — +2 attack damage |
| 30 | **Archmage** — +3 hearts |

**Effects:** level 10 Night Vision · level 30 Fire Resistance

**Total at 32:** +9.1 hearts · +0.5 luck · +1 reach · +2 damage

The most expensive of all, and the biggest health boost.

### How the numbers work

- **Bonus per level:** `per_level × (level − start_level + 1)`. With `start_level: 2`, level 32
  means 31 applications.
- **Point cost of level N:** `base_point_cost + point_cost_growth × (N − 1)`, rounded.
- **Experience for level N:** `xp_base × xp_growth^(N − 2)`, rounded.
- Percentages are `add_multiplied_base`: they apply to the attribute's base value, not to the
  total with gear.

The cap of 32 comes from each skill's `max_level`, further limited by `max_level` in the
[configuration](#configuration).

---

## Currency

The denominations are the Argentine ones: **10, 20, 50, 100, 200, 500, 1000, 10000 and 20000**.
From 10 to 500 they are **mangos**, from 1000 to 20000 they are **lucas** — Argentine slang for
"bucks" and "grand", kept in both languages.

| Item | Value | Name |
| --- | --- | --- |
| `coin_10` | 10 | 10 mangos |
| `coin_20` | 20 | 20 mangos |
| `coin_50` | 50 | 50 mangos |
| `coin_100` | 100 | 100 mangos |
| `coin_200` | 200 | 200 mangos |
| `coin_500` | 500 | 500 mangos |
| `coin_1000` | 1000 | 1 luca |
| `coin_10000` | 10000 | 10 lucas |
| `coin_20000` | 20000 | 20 lucas |

### How they work

There are no conversion recipes. You use them with a coin in hand:

- **Right-click** sweeps every loose coin in the inventory and hands it back as the fewest possible
  coins, telling you the total.
- **Sneak + right-click** breaks one coin down into the next denominations below it.

The price of an [exchange stand](#trades) offer is paid with any combination and gives change back
on its own.

### In datapacks

Wherever a mob rule asks for a denomination, it is written as text:

```json
"coins": [ { "tier": "500", "count": { "min": 1, "max": 3 }, "chance": 0.5 } ]
```

The old names `bronze`, `silver` and `gold` are still accepted and mean 10, 100 and 1000.

---

## Coliseum

`ultimatemodadditions:coliseum`

A dimension of its own with a Roman amphitheatre built by the mod. It is not a flat world: the
generator is empty and the arena raises itself the first time the dimension loads.

### What is in it

- A sand arena with a podium wall around it.
- Five tiers of stepped stands.
- An outer wall with arches on two decks.
- Four gates in a cross.
- Lighting and a return portal next to the north gate.

You get in through the **portal** or with `/uma coliseum enter`. You always land on the sand.

If you fall outside the amphitheatre the mod puts you back on the arena instead of letting you drop
into the void.

### The portal

It is built **exactly like a nether portal but out of crying obsidian**: a rectangular frame, 4×5
counting the frame at minimum (2×3 of opening), up to 23×23. The four corners have to be there.

To light it, **right-click each of the four corners with the [coliseum key](#the-key)**. The mod
tells you how many you have done (`Corner 2 of 4`). On the fourth one a key is consumed and the
portal opens. Order does not matter and you can walk around the frame between clicks.

If you break a block of the frame the portal goes out on its own, like a nether one.

Inside the coliseum there is an already-lit portal next to the north gate that takes you back where
you came from. That one asks for nothing.

#### Requirements to pass through

An open portal is not enough: it has to let *you* through. You need

- **Attack, Defense and Magic at max level**, and
- **100 vanilla experience levels**, which are not spent.

If you do not meet it the portal rejects you and lists in chat what you are missing and how far
along you are. It is checked **per player**: you cannot drag or push someone in who does not meet
it, because each player is checked on entry. Leaving the coliseum asks for nothing.

All of this is changed in `[coliseum.portal]`:

| Option | Default | What it does |
| --- | --- | --- |
| `enforce_requirements` | true | When `false` the portal lets anyone through |
| `required_skills` | attack, defense, magic | Skills that have to be maxed |
| `required_experience_levels` | 100 | Experience levels; they are not spent |

To test it without grinding: `/uma skills set @s ultimatemodadditions:attack 32` (and the same for
`defense` and `magic`), plus `/xp set @s 100 levels`.

### Arena spawners

When the arena is built, spawners are placed in a ring across the sand, each one on a block of
chiselled sandstone. Every spawner carries **one mob type**, handed out in order from the config
list, so all the types you list show up. The one that would land on the spawn point is skipped.

Twelve ship by default: zombie, skeleton, spider, creeper, husk, stray, witch, pillager, blaze,
wither skeleton, vindicator and enderman.

```
/uma spawners list
/uma spawners place
/uma spawners clear
```

`clear` **removes every** spawner in the coliseum, including any you placed by hand. `place` puts
them back according to the config, wiping the previous ones first. Both need permission level 2;
`list` is open to anyone.

To get rid of them for good, set `enabled = false` in `[coliseum.spawners]` and run
`/uma spawners clear`.

#### Spawner config

In `[coliseum.spawners]`:

| Option | Default | What it does |
| --- | --- | --- |
| `enabled` | true | Places spawners when the arena is built |
| `entities` | 12 hostile mobs | Which types to use, one per spawner |
| `count` | 12 | How many to place; 0 places none |
| `min_spawn_delay` | 200 | Shortest wait between batches, in ticks |
| `max_spawn_delay` | 600 | Longest wait between batches, in ticks |
| `spawn_count` | 4 | How many mobs each batch tries to place |
| `max_nearby_entities` | 8 | Stops once this many are already alive nearby |
| `required_player_range` | 24 | How close a player has to be |
| `spawn_range` | 6 | How far from the spawner mobs can appear |

Ids that do not exist are skipped with a warning in the log, so you can list mobs from other mods
without breaking anything when they are not installed.

Mobs from these spawners still go through the [mob rules](#mob-rules) and the active difficulty:
they come out scaled and drop coins and bags like any other.

### The key

`ultimatemodadditions:coliseum_key` is crafted from a gold ingot, an ender pearl and a gold nugget
in a column. It **cannot be bought** at the exchange stand: it sits in the
`ultimatemodadditions:not_purchasable` tag.

It is used to light the portal and is consumed on completing the four corners. One key opens one
portal; after that the portal stays open forever, until you break the frame.

### Difficulty

The dimension has an active difficulty that holds for the whole save, and it is used by every mob
rule that does not declare its own:

```
/uma difficulty get
/uma difficulty list
/uma difficulty set ultimatemodadditions:nightmare
```

See [Difficulties](#difficulties) and [Mob rules](#mob-rules).

### Rebuilding it

```
/uma rebuild_arena
```

Useful if you changed the radius or the height in the config, or if someone wrecked it. Needs
permission level 2.

### Coliseum config

In `[coliseum]`:

| Option | Default | What it does |
| --- | --- | --- |
| `enabled` | true | Turns the dimension on |
| `build_arena` | true | Builds the amphitheatre on load |
| `arena_radius` | 30 | Radius in blocks of the sand floor |
| `arena_floor_y` | 64 | Height of the floor |
| `player_count_scaling` | 0.25 | How much it scales per player |

---

## KubeJS

If KubeJS is installed, the mod adds the global binding `UMA`. **It only works in server scripts**
(`kubejs/server_scripts/`), because the data loads together with the datapack. Calling it from
`startup_scripts` or `client_scripts` means the entry is thrown away when the world loads, and the
mod warns you in the log. Entries are re-injected on every `/reload`, so load order is not
something you have to worry about.

The API is made of **chained builders**. Each one starts with `UMA.<type>('id')`, you hang methods
off it, and it ends with `.register()`. **Nothing happens without `.register()`.**

Ids without a namespace resolve to `ultimatemodadditions:`.

```js
UMA.lootBag('matdien_teaks:mythic')
  .name('Mythic Bag')
  .tier('mythic')
  .rolls(5, 7)
  .guaranteed('ultimatemodadditions:coin_10000', 1)
  .entry('minecraft:netherite_block', 1, 2, 10)
  .register()
```

### The eight builders

| Builder | Equivalent to |
| --- | --- |
| `UMA.difficulty(id)` | `uma/difficulty/` |
| `UMA.mobRule(id)` | `uma/mob_rules/` |
| `UMA.lootBag(id)` | `uma/loot_bags/` |
| `UMA.bagDrop(id)` | `uma/loot_bag_additions/` |
| `UMA.trade(id)` | `uma/trades/` |
| `UMA.skill(id)` | `uma/skills/` |
| `UMA.skillXp(id)` | `uma/skill_xp/` |
| `UMA.skillRequirement(id)` | `uma/skill_requirements/` |

### `UMA.difficulty(id)`

```js
UMA.difficulty('matdien_teaks:brutal')
  .name('Brutal')
  .health(10).damage(4).speed(1.2)
  .armor(8).armorToughness(4).knockbackResistance(0.5).followRange(16)
  .experience(3).coins(8).bagChance(0.1).extraRolls(2)
  .glowing(true)
  .effect('minecraft:strength', 1, -1, 1.0)
  .equipment('mainhand', 'minecraft:netherite_sword', 0.5, 0.02)
  .register()
```

`effect(id, amplifier, duration, chance)` — a duration of `-1` is permanent.
`equipment(slot, item, chance, dropChance)` — slots: `mainhand`, `offhand`, `head`, `chest`,
`legs`, `feet`.

### `UMA.mobRule(id)`

```js
UMA.mobRule('matdien_teaks:zombies')
  .priority(50)
  .entities('minecraft:zombie', 'minecraft:husk')
  .categories('monster')
  .boss(false)
  .dimensions('ultimatemodadditions:coliseum')
  .difficulty('matdien_teaks:brutal')
  .replaceVanillaDrops(true)
  .experienceMultiplier(2)
  .drop('minecraft:netherite_scrap', 1, 2, 0.25)
  .coin('1000', 1, 1, 0.2)
  .bag('ultimatemodadditions:common', 0.1)
  .register()
```

`drop(item, min, max, chance)` · `coin(denomination, min, max, chance)` · `bag(bagId, chance)`.

### `UMA.lootBag(id)`

```js
UMA.lootBag('matdien_teaks:mythic')
  .name('Mythic Bag')
  .tier('mythic')
  .color(0xFF5C5C)
  .rolls(5, 7)
  .uniqueRolls(true)
  .announce(true)
  .openSound('ultimatemodadditions:loot_bag_open')
  .sortOrder(40)
  .guaranteed('ultimatemodadditions:coin_10000', 1)
  .entry('minecraft:netherite_block', 1, 2, 10)
  .bagEntry('ultimatemodadditions:enchantment', 5)
  .lootTableEntry('minecraft:chests/end_city_treasure', 0.5, 3)
  .register()
```

`entry(item, weight)` or `entry(item, min, max, weight)`. `guaranteed(item)`,
`guaranteed(item, amount)` or `guaranteed(item, min, max)`.

### `UMA.bagDrop(id)`

Adds loot to bags that already exist, without rewriting them.

```js
UMA.bagDrop('matdien_teaks:copper_everywhere')
  .allTiers()
  .entry('minecraft:copper_ingot', 2, 8, 10)
  .register()

UMA.bagDrop('matdien_teaks:legendary_extra')
  .bag('ultimatemodadditions:legendary')
  .guaranteed('ultimatemodadditions:coin_20000', 1)
  .register()

UMA.bagDrop('matdien_teaks:high_tiers_only')
  .tiers('epic', 'mythic', 'legendary', 'omega')
  .entry('minecraft:nether_star', 1, 1, 4)
  .register()
```

`bag(id)`, `bags(...)` and `tiers(...)` add up. `allTiers()` is shorthand for `tiers('all')`.
`replace(true)` wipes the previous contents before adding.

### `UMA.trade(id)`

```js
UMA.trade('matdien_teaks:mythic')
  .name('Mythic Bag')
  .category('bags')
  .price(50000)
  .cost('minecraft:emerald', 8)
  .resultBag('matdien_teaks:mythic')
  .requires('ultimatemodadditions:magic', 5)
  .sortOrder(90)
  .register()
```

`result(item)` / `result(item, amount)` for items, `resultBag(id)` for bags. `price` is money and
is paid with any combination of coins, with change.

### `UMA.skill(id)`

```js
UMA.skill('matdien_teaks:alchemy')
  .name('Alchemy')
  .icon('minecraft:brewing_stand')
  .color(0x9B59B6)
  .maxLevel(20)
  .description('Potions and other strange things.')
  .sortOrder(90)
  .pointCost(2, 0.25)
  .experience(40, 1.25)
  .trait('minecraft:generic.luck', 0.05, 'add_value', 2)
  .perk(5, 'Distiller', 'minecraft:generic.luck', 0.5)
  .perk(10, 'Master', 'minecraft:generic.max_health', 4, 'add_value', '+2 hearts')
  .effect('minecraft:haste', 8)
  .effect('minecraft:water_breathing', 12, 0)
  .scalingEffect('minecraft:speed', 15, 0, 6, 2)
  .register()
```

`pointCost(base, growth)` · `experience(base, growth)` — without `experience` the skill only levels
with points. `trait` scales per level, `perk` is a fixed bonus on reaching that level.

`effect(id, level)` or `effect(id, level, amplifier)` grants a permanent potion effect from that
level. `scalingEffect(id, level, amplifier, levelsPerAmplifier, maxAmplifier)` makes it climb on
its own: the example gives Speed I at 15, II at 21, and stops there. `rawEffect({...})` takes the
whole object. See [Skills](#effects--potion-effects).

### `UMA.skillXp(id)`

```js
UMA.skillXp('matdien_teaks:alchemy_potions')
  .skill('matdien_teaks:alchemy')
  .actions('craft')
  .items('minecraft:potion')
  .amount(8)
  .chance(1.0)
  .register()

UMA.skillXp('matdien_teaks:mining')
  .skill('ultimatemodadditions:mining')
  .actions('break')
  .blockTags('minecraft:mineable/pickaxe')
  .amount(2)
  .register()

UMA.skillXp('matdien_teaks:kill_monsters')
  .skill('ultimatemodadditions:attack')
  .actions('kill')
  .categories('monster')
  .amount(6)
  .register()
```

Actions: `break`, `place`, `craft`, `smelt`, `kill`, `damage_taken`, `sprint`.

### `UMA.skillRequirement(id)`

```js
UMA.skillRequirement('matdien_teaks:potions')
  .items('minecraft:potion')
  .requires('matdien_teaks:alchemy', 4)
  .actions('use')
  .register()

UMA.skillRequirement('matdien_teaks:bosses')
  .boss(true)
  .requires('ultimatemodadditions:attack', 16)
  .actions('attack_entity')
  .priority(10)
  .register()
```

Actions: `use`, `attack`, `harvest`, `place`, `equip`, `craft`, `attack_entity`.

### Escape hatches

When a builder does not cover something, the `raw*` methods take an object as-is:

```js
UMA.mobRule('matdien_teaks:odd')
  .rawMatch({ regex: ['^minecraft:.*_zombie$'], exclude: ['minecraft:drowned'] })
  .register()

UMA.lootBag('matdien_teaks:books')
  .rawEntry({
    item: 'minecraft:enchanted_book',
    weight: 10,
    components: { 'minecraft:stored_enchantments': { levels: { 'minecraft:mending': 1 } } }
  })
  .register()
```

The older methods `UMA.addLootBag(id, obj)`, `UMA.addTrade(id, obj)` and friends are still there
and take the whole JSON. They are handy for porting datapacks as-is, but for writing from scratch
the builders are better.

### Ready-made examples

`run/kubejs/` has a `uma_ejemplos.js` in every folder, commented and tested:

- `server_scripts/uma_ejemplos.js` — everything UMA: bags, additions, trades, a difficulty, a mob
  rule, a skill with experience and requirements. It uses `minecraft:bedrock` and
  `minecraft:iron_golem_spawn_egg` as reference items.
- `startup_scripts/uma_ejemplos.js` — explains why UMA does not belong there and shows what does:
  registering new items with `StartupEvents.registry`, to hand them out through UMA bags later.
- `client_scripts/uma_ejemplos.js` — tooltips telling you what each item demands.

### Without writing scripts

KubeJS also loads `kubejs/data/`, so you can drop the JSON in
`kubejs/data/matdien_teaks/uma/mob_rules/…` and it works the same.

### When something is wrong

Format errors show up in the server log with the id of the entry, and that entry is dropped without
breaking the rest.

---

## Commands

`/ums` is an alias of `/uma`: everything works under both names.

```
/uma tps
/uma ping [players]
/uma coliseum enter | leave
/uma rebuild_arena
/uma spawners list | place | clear
/uma difficulty get | list | set <id>
/uma skills info
/uma skills points <players> <amount>
/uma skills set <players> <skill> <level>
/uma skills reset <players>
/uma bag <players> <bag> [amount]
```

### `tps`

Shows the server's real performance, which most mods report badly:

- Average **TPS** and **MSPT** over the last 100 ticks, against the 50 ms budget.
- The **worst tick** in that window.
- MSPT for **each dimension** separately.

The colours mark when it is getting tight (yellow) or bad (red).

### `ping`

With no arguments it shows yours. With a selector, everyone's. Green below 100 ms, yellow up to
250, red above.

### `coliseum`

`enter` takes you to the arena and remembers where you came from; `leave` puts you back there.

### `spawners`

`list` counts the spawners in the coliseum, `place` puts them back according to the config wiping
the existing ones, and `clear` removes them all. `place` and `clear` need permission level 2. See
[Coliseum](#arena-spawners).

### `rebuild_arena`

Rebuilds the amphitheatre. Useful if you changed the radius or the height in the config, or if
someone wrecked it. Needs permission level 2.

### `difficulty`

`get` shows the active difficulty, `list` shows the available ones, `set` changes it for the whole
save. It is the one used by mob rules that do not declare their own. `set` needs permission level
2.

### `skills`

`info` lists your skills and points. `points`, `set` and `reset` need permission level 2.

### `bag`

Hands out bags by id. Needs permission level 2.

---

## Configuration

`config/ultimatemodadditions-common.toml`

These are the broad switches. The content itself lives in [JSON](#datapacks), not here.

### `[coliseum]`

| Option | Default | What it does |
| --- | --- | --- |
| `enabled` | true | Turns the dimension on |
| `build_arena` | true | Builds the amphitheatre the first time |
| `arena_radius` | 30 | Radius of the sand floor |
| `arena_floor_y` | 64 | Height of the floor |
| `player_count_scaling` | 0.25 | Extra scaling per player |

Also the default difficulty and whether custom loot only applies inside the coliseum.

### `[coliseum.portal]`

| Option | Default | What it does |
| --- | --- | --- |
| `enforce_requirements` | true | When `false` the portal lets anyone through |
| `required_skills` | attack, defense, magic | Skills that have to be maxed |
| `required_experience_levels` | 100 | Experience levels; they are not spent |

See [Coliseum](#the-portal).

### `[coliseum.spawners]`

| Option | Default | What it does |
| --- | --- | --- |
| `enabled` | true | Places spawners when the arena is built |
| `entities` | 12 hostile mobs | Which types to use, one per spawner |
| `count` | 12 | How many to place; 0 places none |
| `min_spawn_delay` | 200 | Shortest wait between batches, in ticks |
| `max_spawn_delay` | 600 | Longest wait between batches, in ticks |
| `spawn_count` | 4 | How many mobs each batch tries to place |
| `max_nearby_entities` | 8 | Stops once this many are already alive nearby |
| `required_player_range` | 24 | How close a player has to be |
| `spawn_range` | 6 | How far from the spawner mobs can appear |

See [Coliseum](#arena-spawners).

### `[mob_scaling]`

Turns scaling on, whether it only applies inside the coliseum, global multipliers and a health cap.

### `[coins]` and `[loot_bags]`

Turn each system on, whether they only work inside the coliseum, base chance and amounts, and
whether only player kills count.

### `[skills]`

| Option | Default | What it does |
| --- | --- | --- |
| `enabled` | true | Turns skills on |
| `enforce_requirements` | true | Blocks items without the required level |
| `max_level` | 32 | Global cap, on top of each skill's own |
| `starting_points` | — | Points granted on first join |
| `points_per_player_level` | — | Points per vanilla experience level |
| `reset_on_death` | false | Resets skills on death |
| `xp_multiplier` | 1.0 | Multiplies all skill experience |
| `levels_per_bonus_heart` | 12 | Total levels per extra heart; 0 disables it |
| `max_bonus_hearts` | 10 | Cap on extra hearts; 0 removes it |

See [Skills](#skills).

---

## Full example

A small pack that touches everything: a difficulty, a mob rule, a bag, an addition to the bags that
already exist, an offer, and a skill with its experience and its requirement. All under the
`matdien_teaks` namespace.

It goes in `config/openloader/data/matdien_teaks/` with
[OpenLoader](https://www.curseforge.com/minecraft/mc-mods/open-loader), or in
`world/datapacks/matdien_teaks/data/matdien_teaks/` if you prefer a plain datapack.

### `uma/difficulty/brutal.json`

```json
{
  "name": "Brutal",
  "health_multiplier": 6.0,
  "damage_multiplier": 3.0,
  "speed_multiplier": 1.15,
  "armor_bonus": 8.0,
  "armor_toughness_bonus": 3.0,
  "knockback_resistance": 0.3,
  "follow_range_bonus": 16.0,
  "experience_multiplier": 3.0,
  "coin_multiplier": 4.0,
  "bag_chance_bonus": 0.08,
  "extra_loot_rolls": 1,
  "glowing": true,
  "effects": [
    { "effect": "minecraft:strength", "amplifier": 1, "duration": -1, "chance": 1.0 }
  ],
  "equipment": {
    "mainhand": { "item": "minecraft:netherite_sword", "chance": 0.4, "drop_chance": 0.02 },
    "head": { "item": "minecraft:netherite_helmet", "chance": 0.3 }
  }
}
```

### `uma/mob_rules/wither_skeletons.json`

```json
{
  "priority": 50,
  "match": { "entities": ["minecraft:wither_skeleton"] },
  "dimensions": ["ultimatemodadditions:coliseum"],
  "difficulty": "matdien_teaks:brutal",
  "replace_vanilla_drops": false,
  "experience_multiplier": 2.0,
  "drops": [
    { "item": "minecraft:netherite_scrap", "count": { "min": 1, "max": 2 }, "chance": 0.25,
      "looting_bonus": 0.05 }
  ],
  "coins": [
    { "tier": "1000", "count": 1, "chance": 0.35 }
  ],
  "bags": [
    { "bag": "matdien_teaks:boss_reward", "chance": 0.05 }
  ]
}
```

### `uma/loot_bags/boss_reward.json`

```json
{
  "name": "Boss Reward",
  "tier": "legendary",
  "color": 16733525,
  "sort_order": 45,
  "rolls": { "min": 3, "max": 5 },
  "unique_rolls": true,
  "announce": true,
  "open_sound": "ultimatemodadditions:loot_bag_open",
  "guaranteed": [
    { "item": "ultimatemodadditions:coin_20000", "count": { "min": 1, "max": 2 } }
  ],
  "entries": [
    { "item": "minecraft:netherite_ingot", "count": { "min": 1, "max": 2 }, "weight": 10 },
    { "item": "minecraft:elytra", "count": 1, "weight": 2 },
    { "bag": "ultimatemodadditions:enchantment", "count": 1, "weight": 6 },
    { "loot_table": "minecraft:chests/end_city_treasure", "weight": 4 }
  ]
}
```

### `uma/loot_bag_additions/copper_everywhere.json`

```json
{
  "tiers": ["all"],
  "entries": [
    { "item": "minecraft:copper_ingot", "count": { "min": 2, "max": 8 }, "weight": 10 }
  ],
  "replace": false
}
```

### `uma/trades/boss_reward.json`

```json
{
  "name": "Boss Reward",
  "category": "bags",
  "sort_order": 90,
  "price": 50000,
  "cost": [ { "item": "minecraft:emerald", "count": 8 } ],
  "result": { "bag": "matdien_teaks:boss_reward", "count": 1 },
  "requirements": [ { "skill": "ultimatemodadditions:attack", "level": 15 } ],
  "enabled": true
}
```

### `uma/skills/alchemy.json`

```json
{
  "name": "Alchemy",
  "icon": "minecraft:brewing_stand",
  "color": 10181046,
  "max_level": 20,
  "sort_order": 90,
  "base_point_cost": 2,
  "point_cost_growth": 0.25,
  "xp_base": 40,
  "xp_growth": 1.25,
  "description": ["Potions and other strange things."],
  "traits": [
    { "attribute": "minecraft:generic.luck", "per_level": 0.05, "start_level": 2 }
  ],
  "perks": [
    { "level": 5, "name": "Distiller", "description": ["+0.5 luck"],
      "attribute": "minecraft:generic.luck", "amount": 0.5 },
    { "level": 15, "name": "Master Alchemist", "description": ["+2 hearts"],
      "attribute": "minecraft:generic.max_health", "amount": 4.0 }
  ],
  "effects": [
    { "effect": "minecraft:water_breathing", "level": 8 },
    { "effect": "minecraft:night_vision", "level": 18 }
  ]
}
```

### `uma/skill_xp/alchemy_potions.json`

```json
{
  "skill": "matdien_teaks:alchemy",
  "actions": ["craft"],
  "match": { "items": ["minecraft:potion", "minecraft:splash_potion"] },
  "amount": 8,
  "chance": 1.0
}
```

### `uma/skill_requirements/potions.json`

```json
{
  "priority": 10,
  "match": { "items": ["minecraft:potion", "minecraft:splash_potion"] },
  "actions": ["use", "craft"],
  "requirements": [ { "skill": "matdien_teaks:alchemy", "level": 4 } ]
}
```

### The same thing in KubeJS

All of the above in a single file, `kubejs/server_scripts/matdien_teaks.js`:

```js
UMA.difficulty('matdien_teaks:brutal')
  .name('Brutal')
  .health(6).damage(3).speed(1.15)
  .armor(8).armorToughness(3).knockbackResistance(0.3).followRange(16)
  .experience(3).coins(4).bagChance(0.08).extraRolls(1)
  .glowing(true)
  .effect('minecraft:strength', 1, -1, 1.0)
  .equipment('mainhand', 'minecraft:netherite_sword', 0.4, 0.02)
  .register()

UMA.lootBag('matdien_teaks:boss_reward')
  .name('Boss Reward')
  .tier('legendary')
  .color(0xFF5555)
  .rolls(3, 5)
  .uniqueRolls(true)
  .announce(true)
  .guaranteed('ultimatemodadditions:coin_20000', 1, 2)
  .entry('minecraft:netherite_ingot', 1, 2, 10)
  .entry('minecraft:elytra', 2)
  .bagEntry('ultimatemodadditions:enchantment', 6)
  .lootTableEntry('minecraft:chests/end_city_treasure', 1.0, 4)
  .register()

UMA.mobRule('matdien_teaks:wither_skeletons')
  .priority(50)
  .entities('minecraft:wither_skeleton')
  .dimensions('ultimatemodadditions:coliseum')
  .difficulty('matdien_teaks:brutal')
  .experienceMultiplier(2)
  .drop('minecraft:netherite_scrap', 1, 2, 0.25)
  .coin('1000', 1, 1, 0.35)
  .bag('matdien_teaks:boss_reward', 0.05)
  .register()

UMA.bagDrop('matdien_teaks:copper_everywhere')
  .allTiers()
  .entry('minecraft:copper_ingot', 2, 8, 10)
  .register()

UMA.trade('matdien_teaks:boss_reward')
  .name('Boss Reward')
  .category('bags')
  .price(50000)
  .cost('minecraft:emerald', 8)
  .resultBag('matdien_teaks:boss_reward')
  .requires('ultimatemodadditions:attack', 15)
  .sortOrder(90)
  .register()

UMA.skill('matdien_teaks:alchemy')
  .name('Alchemy')
  .icon('minecraft:brewing_stand')
  .color(0x9B59B6)
  .maxLevel(20)
  .description('Potions and other strange things.')
  .sortOrder(90)
  .pointCost(2, 0.25)
  .experience(40, 1.25)
  .trait('minecraft:generic.luck', 0.05, 'add_value', 2)
  .perk(5, 'Distiller', 'minecraft:generic.luck', 0.5)
  .perk(15, 'Master Alchemist', 'minecraft:generic.max_health', 4, 'add_value', '+2 hearts')
  .effect('minecraft:water_breathing', 8)
  .effect('minecraft:night_vision', 18)
  .register()

UMA.skillXp('matdien_teaks:alchemy_potions')
  .skill('matdien_teaks:alchemy')
  .actions('craft')
  .items('minecraft:potion', 'minecraft:splash_potion')
  .amount(8)
  .register()

UMA.skillRequirement('matdien_teaks:potions')
  .items('minecraft:potion', 'minecraft:splash_potion')
  .actions('use', 'craft')
  .requires('matdien_teaks:alchemy', 4)
  .priority(10)
  .register()
```

`/reload` and you are done. Format errors show up in the server log with the id of the entry, and
that entry is dropped without breaking the rest.
