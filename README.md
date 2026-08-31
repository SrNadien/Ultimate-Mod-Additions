## Ultimate Mod Additions

A content mod built around one idea: **nothing is hardcoded**. The coliseum, the mobs that spawn in
it, what they drop, what the shop sells — all of it lives in JSON files
that any datapack can override, or in KubeJS scripts if you'd rather write code.

---

### The Coliseum

A dedicated dimension (`ultimatemodadditions:coliseum`) with a real Roman amphitheatre — not a flat
world. The mod builds it procedurally the first time the dimension loads: sand arena, podium wall,
five tiers of stands, an outer wall with two levels of arches, four gates and a return portal. Fall
off the edge and you get put back on the sand instead of dying in the void.

**Getting in is a build and a gate.** You raise a portal frame exactly like a nether portal, but out
of **crying obsidian** — 4×5 at minimum, up to 23×23, corners included. Then you right-click each of
the four corners with a **Coliseum Key**; the game counts them off for you, and on the fourth the key
is consumed and the portal lights up.

Even lit, it will not take just anyone. To step through you need **100 experience levels**, which you
keep. Everyone is checked individually as they enter, so you cannot drag an underprepared friend in
behind you — the portal simply refuses them and tells them what they are short on. Leaving is free,
and there is an already-lit portal home by the north gate. Packs can raise, lower or drop the
requirement in the config.

The dimension runs an active **difficulty profile** that scales every mob in it: health, damage,
speed, armour, knockback resistance, follow range, dropped XP, coins, loot-bag chance, permanent
potion effects and equipment rolls. Four profiles ship with the mod (`easy`, `normal`, `hard`,
`nightmare`) and it scales further with the number of players online.

**Spawners** are placed in a ring across the arena floor when it's built, one mob type each so the
fight stays varied — twelve by default, from zombies and creepers to blazes, witches and endermen.
Which types appear, how many spawners there are and every spawner setting (delays, mobs per batch,
player range, spawn radius) live in the config, and unknown ids are skipped with a log warning so
you can list mobs from other mods safely. Don't want them? Set `enabled = false` and run
`/uma spawners clear` to wipe the ones already placed. `/uma spawners place` puts them back.

### Argentine coins

Nine denominations: **10, 20, 50, 100, 200, 500, 1000, 10000 and 20000**. From 10 to 500 they're
*mangos*, from 1000 up they're *lucas*.

No conversion recipes. Right-click a coin and it sweeps every loose coin in your inventory into the
fewest possible coins; sneak + right-click breaks one down into smaller change. The **Exchange
Stand** takes any combination of coins and gives correct change automatically.

### Loot bags

Eight tiers: **common, uncommon, epic, mythic, legendary, omega, food and enchantment**. Right-click
to open one, sneak to open the whole stack. Bags can roll guaranteed loot, a weighted random pool,
vanilla loot tables, and **other bags** nested inside them.

Only common and uncommon can be bought at the Exchange Stand. The Coliseum Key can never be bought.

### Commands

`/uma` (or the shorter `/ums`):

- **`/uma tps`** — real server performance, because most mods report it wrong: average TPS and MSPT
  over the last 100 ticks against the 50 ms budget, the worst single tick in that window, and MSPT
  broken down **per dimension**.
- **`/uma ping [players]`** — ping for yourself or anyone else.
- **`/uma spawners list | place | clear`** — count, re-place or wipe the arena spawners.
- Plus `coliseum`, `difficulty`, `bag` and `rebuild_arena`.

---

## Everything is a datapack

The whole content side of the mod is JSON under `data/<your_namespace>/uma/…`, reloaded with
`/reload`. The namespace is yours — the mod only cares about the folder.

| Folder | What it controls |
| --- | --- |
<<<<<<< HEAD
| Dimensión coliseo | `ultimatemodadditions:coliseum` |
| Portal del coliseo | `ultimatemodadditions:coliseum_gateway` |
| Puesto de intercambio | `ultimatemodadditions:exchange_stand` |
| Monedas | `coin_10`, `coin_20`, `coin_50`, `coin_100`, `coin_200`, `coin_500`, `coin_1000`, `coin_10000`, `coin_20000` |
| Bolsas | `loot_bag_common`, `loot_bag_uncommon`, `loot_bag_epic`, `loot_bag_mythic`, `loot_bag_legendary`, `loot_bag_omega`, `loot_bag_food`, `loot_bag_enchantment` |
=======
| `uma/difficulty/` | Mob scaling profiles: health, damage, armour, dropped XP, coins, permanent potion effects, gear rolls |
| `uma/mob_rules/` | Which mob uses which difficulty, in which dimensions, and what it drops |
| `uma/loot_bags/` | The contents of each bag |
| `uma/loot_bag_additions/` | Extra drops poured into bags that already exist |
| `uma/trades/` | Exchange Stand offers |
>>>>>>> bcc20ddc878bb284d5cbca012a6bb923688d6f37

**Adding loot to the bags is the part most packs want.** A file in `uma/loot_bag_additions/` adds
to bags that already exist without rewriting them, so your entries stack on top of the mod's or on
top of another pack's. You can target one bag, a list of bags, a whole rarity, or every bag at once,
and add both guaranteed loot and weighted random entries.

> **Recommended: [OpenLoader](https://www.curseforge.com/minecraft/mc-mods/open-loader)**
>
> A normal datapack lives inside one world's `datapacks/` folder, which means you have to copy it
> into every new world. OpenLoader lets you put the same files in **`config/openloader/data/`** and
> they load in *every* world automatically — much better for a modpack or a server.

**KubeJS works too.** If it is installed the mod adds a global `UMA` binding with a chained-builder
API for all five folders — `UMA.difficulty()`, `UMA.mobRule()`, `UMA.lootBag()`, `UMA.bagDrop()` and
`UMA.trade()`. It goes in `kubejs/server_scripts/`, because the data loads with the datapack. Bad
entries are logged with their id and skipped, so one typo never breaks the rest of the pack.

**The wiki has all of it**: every field of every file with its type and default, the whole KubeJS
API method by method, and a complete worked example — one file of each kind, in JSON and then the
same thing in KubeJS.

---

## Requeriments:

· Minecraft 1.21.1

<<<<<<< HEAD
- `[coliseum]` — activar el coliseo, si el botín custom solo se aplica dentro, si se conservan los
  drops vanilla, dificultad por defecto, escalado por número de jugadores y la construcción del
  anfiteatro (`build_arena`, `arena_radius`, `arena_floor_y`).
- `[mob_scaling]` — activar el escalado, si solo aplica dentro del coliseo, multiplicadores globales
  y tope de vida.
- `[coins]` / `[loot_bags]` — activar, si solo dentro del coliseo, probabilidad y cantidades base,
  y si solo cuentan las muertes hechas por un jugador.
  (`xp_multiplier`) y corazones extra por niveles totales (`levels_per_bonus_heart`,
  `max_bonus_hearts`).
=======
· NeoForge 21.1.240
>>>>>>> bcc20ddc878bb284d5cbca012a6bb923688d6f37

## Looking for the skill system?

<<<<<<< HEAD
La dimensión ya no es un mundo plano: el generador es vacío y el mod construye un anfiteatro romano
la primera vez que carga la dimensión. Tiene arena de arena, muro de podio, cinco gradas escalonadas,
muralla exterior con arcos en dos niveles, cuatro puertas en cruz, iluminación y un portal de vuelta
junto a la puerta norte.

Se entra con `/uma coliseum enter` o con el portal del coliseo, y siempre se aparece sobre la arena,
nunca bajo la bedrock. Si un jugador cae por fuera del anfiteatro, se lo devuelve a la arena en
lugar de dejarlo caer al vacío.

`/uma rebuild_arena` (permiso 2) lo vuelve a construir si se cambió el radio o la altura en la
config, o si alguien lo destrozó.

## Datos JSON

Todo va bajo `data/<cualquier_namespace>/uma/…`, así que sirve un datapack, una carpeta de
`kubejs/data/` o el propio mod. Se recarga con `/reload`.

### `uma/difficulty/<id>.json`

```json
{
  "name": "Dificil",
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

Ranuras válidas en `equipment`: `mainhand`, `offhand`, `head`, `chest`, `legs`, `feet`.
`duration: -1` es permanente. Vienen `easy`, `normal`, `hard` y `nightmare` de serie.

### `uma/mob_rules/<id>.json`

La primera regla que encaja gana; las de `priority` más alta se comprueban antes.

```json
{
  "priority": 0,
  "match": {
    "entities": ["minecraft:zombie"],
    "tags": ["minecraft:skeletons"],
    "regex": ["^minecraft:.*_zombie$"],
    "categories": ["monster"],
    "exclude": ["minecraft:villager"],
    "boss": false,
    "baby": false
  },
  "dimensions": ["ultimatemodadditions:coliseum"],
  "difficulty": "ultimatemodadditions:hard",
  "replace_vanilla_drops": true,
  "experience_multiplier": 2.0,
  "drops": [
    { "item": "minecraft:diamond", "count": { "min": 1, "max": 3 }, "chance": 0.5, "looting_bonus": 0.1 },
    { "loot_table": "minecraft:chests/simple_dungeon", "chance": 0.1 },
    { "bag": "ultimatemodadditions:epic", "chance": 0.02 }
  ],
  "coins": [ { "tier": "500", "count": { "min": 1, "max": 3 }, "chance": 0.5 } ],
  "bags":  [ { "bag": "ultimatemodadditions:common", "chance": 0.06 } ]
}
```

- `match` vacío = cualquier mob. `categories` acepta `monster`, `creature`, `ambient`, etc.
- `dimensions` vacío = todas las dimensiones.
- `tier` de una moneda es la denominación (`"10"`, `"500"`, `"20000"`). Se aceptan los nombres
  viejos `bronze`, `silver` y `gold`, que equivalen a 10, 100 y 1000.
- Se puede escribir la dificultad en línea con `"inline_difficulty": { … }` en vez de `difficulty`.
- `count` acepta un número suelto (`"count": 3`) o `{ "min": …, "max": … }`.
- Toda entrada de botín admite `components` con el formato de componentes de 1.21.

### `uma/loot_bags/<id>.json`

```json
{
  "name": "Bolsa epica",
  "tier": "epic",
  "color": 12213503,
  "rolls": { "min": 3, "max": 5 },
  "unique_rolls": true,
  "announce": false,
  "sort_order": 30,
  "open_sound": "ultimatemodadditions:loot_bag_open",
  "guaranteed": [ { "item": "ultimatemodadditions:coin_500", "count": { "min": 1, "max": 3 } } ],
  "entries": [
    { "item": "minecraft:diamond_block", "count": 1, "weight": 16 },
    { "bag": "ultimatemodadditions:enchantment", "count": 1, "weight": 8 },
    { "item": "minecraft:enchanted_book", "weight": 8,
      "components": { "minecraft:stored_enchantments": { "levels": { "minecraft:sharpness": 4 } } } }
  ]
}
```

`tier` decide qué item de bolsa se usa y por tanto la textura y la rareza. Rarezas disponibles:

| tier | Se compra en el puesto |
| --- | --- |
| `common` | sí |
| `uncommon` | sí |
| `epic` | no |
| `mythic` | no |
| `legendary` | no |
| `omega` | no |
| `food` | no |
| `enchantment` | no |

Puedes crear todas las bolsas que quieras: cada una es un id distinto sobre uno de esos ocho items.
`unique_rolls: true` impide que salga dos veces la misma entrada. `announce: true` avisa a todo el
servidor al abrirla. `rare` sigue aceptándose como alias de `epic` para datapacks viejos.

### `uma/loot_bag_additions/<id>.json`

Para **añadir** botín a bolsas que ya existen sin reescribirlas. Se aplican después de cargar todas
las bolsas, así que funcionan sobre las del mod, las de otro datapack o las de KubeJS.

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

- `bag`, `bags` y `tiers` se suman: la adición se aplica a toda bolsa que encaje con cualquiera
  de los tres. `"tiers": ["all"]` alcanza a todas.
- `entries` se suma al pool aleatorio, `guaranteed` al botín fijo.
- `replace: true` borra el contenido previo de la bolsa antes de añadir el nuevo.

### `uma/trades/<id>.json`

```json
{
  "name": "Bolsa poco comun",
  "category": "bags",
  "sort_order": 20,
  "price": 700,
  "cost": [ { "item": "minecraft:emerald", "count": 4 } ],
  "result": { "bag": "ultimatemodadditions:uncommon", "count": 1 },
  "enabled": true
}
```

- `price` es dinero: se paga con cualquier combinación de monedas y se devuelve el vuelto.
- `cost` sigue existiendo para pedir objetos concretos además del precio. Se pueden usar los dos.
- `result` acepta `item` + `components`, o `bag` para entregar una bolsa concreta.

El puesto **rechaza** las ofertas cuyo resultado no se puede comprar, aunque estén definidas:
bolsas de rareza `epic` o superior, bolsas de comida y encantamiento, y cualquier item que esté en
la etiqueta `ultimatemodadditions:not_purchasable` (donde ya está la llave del coliseo). Esas
ofertas tampoco se muestran en la GUI ni en JEI.

## KubeJS (opcional)

Si KubeJS está instalado se añade el binding global `UMA` en los scripts de servidor. Los datos
se vuelven a inyectar en cada `/reload`, así que no hay que preocuparse por el orden de carga.

```js
// kubejs/server_scripts/coliseo.js
UMA.addDifficulty('mipack:brutal', {
  name: 'Brutal',
  health_multiplier: 10,
  damage_multiplier: 4,
  coin_multiplier: 8
})

UMA.addMobRule('mipack:zombis', {
  priority: 50,
  match: { entities: ['minecraft:zombie', 'minecraft:husk'] },
  dimensions: ['ultimatemodadditions:coliseum'],
  difficulty: 'mipack:brutal',
  replace_vanilla_drops: true,
  drops: [{ item: 'minecraft:netherite_scrap', count: { min: 1, max: 2 }, chance: 0.25 }],
  coins: [{ tier: '1000', count: 1, chance: 0.2 }]
})

UMA.addLootBag('mipack:mitica', {
  name: 'Bolsa mitica',
  tier: 'mythic',
  rolls: { min: 5, max: 7 },
  entries: [{ item: 'minecraft:netherite_block', weight: 1 }]
})

// Añadir botín a bolsas que ya existen, sin reescribirlas
UMA.addBagDrop('mipack:cobre_en_todas', {
  tiers: ['all'],
  entries: [{ item: 'minecraft:copper_ingot', count: { min: 2, max: 8 }, weight: 10 }]
})

UMA.addBagDrop('mipack:extra_legendaria', {
  bag: 'ultimatemodadditions:legendary',
  guaranteed: [{ item: 'ultimatemodadditions:coin_20000', count: 1 }]
})

UMA.addTrade('mipack:mitica', {
  price: 50000,
  result: { bag: 'mipack:mitica' }
})


Cada método acepta un objeto JS o una cadena JSON. Los ids sin namespace se resuelven a
`ultimatemodadditions:`.

Como KubeJS también carga `kubejs/data/`, puedes dejar los mismos JSON en
`kubejs/data/mipack/uma/mob_rules/…` sin escribir nada de script. Vale para todas las carpetas,
incluida `uma/loot_bag_additions/`.

## Comandos

`/ums` es un alias de `/uma`, así que todo funciona con los dos nombres.

```
/uma tps
/uma ping [jugadores]
/uma coliseum enter | leave
/uma rebuild_arena
/uma difficulty get | list | set <id>
/uma bag <jugadores> <bolsa> [cantidad]
```

- `tps` muestra los TPS reales, el MSPT medio de los últimos 100 ticks con su presupuesto, el peor
  tick del periodo y el MSPT de cada dimensión por separado. Los colores marcan cuándo el servidor
  va justo o directamente en rojo.
- `ping` sin argumentos muestra el tuyo; con selector, el de cada jugador.
- `rebuild_arena` y `difficulty set` piden permiso 2.

`difficulty set` cambia la dificultad activa del coliseo para toda la partida; es lo que usan las
reglas que no declaran una dificultad propia.

## JEI

Si JEI está instalado se añaden dos categorías: **Bolsas de botín** (contenido posible de cada
bolsa) y **Puesto de intercambio** (solo las ofertas que se pueden comprar de verdad). Ambas se
rellenan al sincronizar el datapack, así que reflejan lo que haya cargado el servidor.

## Compilar

```
gradlew build
```

Necesita JDK 21.

`gradle.properties` controla las integraciones:

- `compat_kubejs` — compila el módulo de KubeJS. Si su maven no responde, se puede poner en
  `false` y el módulo simplemente no se compila.
- `dev_runtime_mods` — mete KubeJS y Rhino en los `runClient` / `runServer` de
  desarrollo, para poder probar los scripts sin armar un modpack.
=======
It used to live here and now has a mod of its own: **Ultimate Skills**, a node tree where every
node, connection, bonus and effect is defined in JSON or KubeJS. The two mods are independent —
install either one on its own, or both.
>>>>>>> bcc20ddc878bb284d5cbca012a6bb923688d6f37
