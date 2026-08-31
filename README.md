# Ultimate Mod Additions

NeoForge 21.1.240 · Minecraft 1.21.1 · modid `ultimatemodadditions`

Coliseo como dimensión propia con un anfiteatro construido de verdad, mobs escalados por dificultad,
botín editable, monedas con denominaciones argentinas, bolsas de botín por rareza, sistema de
monedas y bolsas de botín. Todo se define en JSON y, si KubeJS está
instalado, también por script.

## Wiki

La documentación completa de los JSON y de la API de KubeJS está en la **wiki de GitHub**, en
español y en inglés. Los archivos fuente viven en `wiki/` fuera del control de versiones, porque la
wiki de GitHub es un repositorio aparte.

## Contenido

| Cosa | Id |
| --- | --- |
| Dimensión coliseo | `ultimatemodadditions:coliseum` |
| Portal del coliseo | `ultimatemodadditions:coliseum_gateway` |
| Puesto de intercambio | `ultimatemodadditions:exchange_stand` |
| Monedas | `coin_10`, `coin_20`, `coin_50`, `coin_100`, `coin_200`, `coin_500`, `coin_1000`, `coin_10000`, `coin_20000` |
| Bolsas | `loot_bag_common`, `loot_bag_uncommon`, `loot_bag_epic`, `loot_bag_mythic`, `loot_bag_legendary`, `loot_bag_omega`, `loot_bag_food`, `loot_bag_enchantment` |

## Monedas

Las denominaciones son las de la moneda argentina: 10, 20, 50, 100, 200, 500, 1000, 10000 y 20000.
Del 10 al 500 son **mangos** y del 1000 al 20000 son **lucas**, en los dos idiomas, así que `coin_1000` es
"1 luca" y `coin_20000` es "20 lucas".

No hay recetas de conversión: se manejan con la propia moneda en la mano.

- **Clic derecho** junta todo el dinero suelto del inventario y lo devuelve en la menor cantidad
  posible de monedas, mostrando el total.
- **Agachado + clic derecho** rompe una moneda en las denominaciones inmediatamente menores.

El precio de una oferta del puesto de intercambio se paga con cualquier combinación de monedas y
devuelve el vuelto automáticamente.

## Configuración global

`config/ultimatemodadditions-common.toml`. Interruptores generales; el detalle va en JSON.

- `[coliseum]` — activar el coliseo, si el botín custom solo se aplica dentro, si se conservan los
  drops vanilla, dificultad por defecto, escalado por número de jugadores y la construcción del
  anfiteatro (`build_arena`, `arena_radius`, `arena_floor_y`).
- `[mob_scaling]` — activar el escalado, si solo aplica dentro del coliseo, multiplicadores globales
  y tope de vida.
- `[coins]` / `[loot_bags]` — activar, si solo dentro del coliseo, probabilidad y cantidades base,
  y si solo cuentan las muertes hechas por un jugador.
  (`xp_multiplier`) y corazones extra por niveles totales (`levels_per_bonus_heart`,
  `max_bonus_hearts`).

## El coliseo

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
