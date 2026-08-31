package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class UMALangProvider extends LanguageProvider {

    private final String locale;

    public UMALangProvider(PackOutput output, String locale) {
        super(output, UMA.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("en_us")) {
            english();
        } else {
            spanish();
        }
    }

    private void english() {
        add("itemGroup.ultimatemodadditions", "Ultimate Mod Additions");
        add("item.ultimatemodadditions.coin_10", "10 Mangos");
        add("item.ultimatemodadditions.coin_20", "20 Mangos");
        add("item.ultimatemodadditions.coin_50", "50 Mangos");
        add("item.ultimatemodadditions.coin_100", "100 Mangos");
        add("item.ultimatemodadditions.coin_200", "200 Mangos");
        add("item.ultimatemodadditions.coin_500", "500 Mangos");
        add("item.ultimatemodadditions.coin_1000", "1 Luca");
        add("item.ultimatemodadditions.coin_10000", "10 Lucas");
        add("item.ultimatemodadditions.coin_20000", "20 Lucas");
        add("item.ultimatemodadditions.loot_bag_common", "Common Loot Bag");
        add("item.ultimatemodadditions.loot_bag_uncommon", "Uncommon Loot Bag");
        add("item.ultimatemodadditions.loot_bag_epic", "Epic Loot Bag");
        add("item.ultimatemodadditions.loot_bag_mythic", "Mythic Loot Bag");
        add("item.ultimatemodadditions.loot_bag_legendary", "Legendary Loot Bag");
        add("item.ultimatemodadditions.loot_bag_omega", "Omega Loot Bag");
        add("item.ultimatemodadditions.loot_bag_food", "Food Loot Bag");
        add("item.ultimatemodadditions.loot_bag_enchantment", "Enchantment Loot Bag");
        add("item.ultimatemodadditions.coliseum_key", "Coliseum Key");
        add("block.ultimatemodadditions.coliseum_portal", "Coliseum Portal");
        add("block.ultimatemodadditions.exchange_stand", "Exchange Stand");
        add("money.ultimatemodadditions.mango", "%s mango");
        add("money.ultimatemodadditions.mangos", "%s mangos");
        add("money.ultimatemodadditions.luca", "%s luca");
        add("money.ultimatemodadditions.lucas", "%s lucas");
        add("money.ultimatemodadditions.combined", "%s and %s");
        add("jei.ultimatemodadditions.loot_bag", "Loot Bags");
        add("jei.ultimatemodadditions.exchange", "Exchange Stand");
        add("container.ultimatemodadditions.exchange", "Exchange Stand");
        add("bag_tier.ultimatemodadditions.common", "Common");
        add("bag_tier.ultimatemodadditions.uncommon", "Uncommon");
        add("bag_tier.ultimatemodadditions.epic", "Epic");
        add("bag_tier.ultimatemodadditions.mythic", "Mythic");
        add("bag_tier.ultimatemodadditions.legendary", "Legendary");
        add("bag_tier.ultimatemodadditions.omega", "Omega");
        add("bag_tier.ultimatemodadditions.food", "Food");
        add("bag_tier.ultimatemodadditions.enchantment", "Enchantment");
        add("tooltip.ultimatemodadditions.coin_value", "Value: %s");
        add("tooltip.ultimatemodadditions.coin_hint", "Right click to merge your coins. Sneak click to break one down.");
        add("tooltip.ultimatemodadditions.bag_tier", "Rarity: %s");
        add("tooltip.ultimatemodadditions.bag_open", "Right click to open. Sneak to open the whole stack.");
        add("message.ultimatemodadditions.unknown_bag", "No such loot bag: %s");
        add("message.ultimatemodadditions.bag_announce", "%s opened a %s");
        add("message.ultimatemodadditions.trade_locked", "You do not meet this offer's requirements");
        add("message.ultimatemodadditions.trade_forbidden", "This item cannot be bought here");
        add("message.ultimatemodadditions.trade_too_poor", "You need %s");
        add("message.ultimatemodadditions.money_total", "You are carrying %s");
        add("message.ultimatemodadditions.money_split", "Broke down %s");
        add("message.ultimatemodadditions.no_coliseum", "The Coliseum dimension is not loaded");
        add("message.ultimatemodadditions.portal_invalid", "This crying obsidian is not the corner of a valid frame");
        add("message.ultimatemodadditions.portal_corner", "Corner %s of %s");
        add("message.ultimatemodadditions.portal_lit", "The portal opens");
        add("message.ultimatemodadditions.portal_already_lit", "This portal is already open");
        add("message.ultimatemodadditions.portal_locked", "The portal rejects you");
        add("message.ultimatemodadditions.portal_need_levels", "Experience: level %s of %s");
        add("tooltip.ultimatemodadditions.coliseum_key", "Opens a crying obsidian portal to the Coliseum.");
        add("tooltip.ultimatemodadditions.coliseum_key_corners", "Right-click the four corners of the frame.");
        add("screen.ultimatemodadditions.no_offers", "No offers available");
        add("screen.ultimatemodadditions.balance", "Wallet: %s");
        add("screen.ultimatemodadditions.free", "Free");
        add("key.categories.ultimatemodadditions", "Ultimate Mod Additions");
        add("command.ultimatemodadditions.difficulty_current", "Current difficulty: %s");
        add("command.ultimatemodadditions.difficulty_set", "Difficulty set to %s");
        add("command.ultimatemodadditions.unknown_difficulty", "Unknown difficulty: %s");
        add("command.ultimatemodadditions.tps_header", "Server performance");
        add("command.ultimatemodadditions.tps_line", "TPS %s - MSPT %s ms (budget %s ms)");
        add("command.ultimatemodadditions.mspt_peak", "Worst tick of the last 100: %s ms");
        add("command.ultimatemodadditions.tps_dimension", "%s: %s ms");
        add("command.ultimatemodadditions.ping", "%s: %s ms");
        add("command.ultimatemodadditions.arena_built", "The Coliseum arena was rebuilt");
        add("command.ultimatemodadditions.arena_failed", "The arena could not be rebuilt");
        add("command.ultimatemodadditions.no_coliseum", "The Coliseum dimension is not loaded");
        add("command.ultimatemodadditions.spawners_count", "There are %s spawners in the Coliseum");
        add("command.ultimatemodadditions.spawners_placed", "Placed %s spawners in the arena");
        add("command.ultimatemodadditions.spawners_cleared", "Removed %s spawners from the Coliseum");
        add("command.ultimatemodadditions.spawners_disabled", "Arena spawners are turned off in the config");
        add("command.ultimatemodadditions.spawners_none", "No spawners were placed. Check count and entities in the config");
    }

    private void spanish() {
        add("itemGroup.ultimatemodadditions", "Ultimate Mod Additions");
        add("item.ultimatemodadditions.coin_10", "10 mangos");
        add("item.ultimatemodadditions.coin_20", "20 mangos");
        add("item.ultimatemodadditions.coin_50", "50 mangos");
        add("item.ultimatemodadditions.coin_100", "100 mangos");
        add("item.ultimatemodadditions.coin_200", "200 mangos");
        add("item.ultimatemodadditions.coin_500", "500 mangos");
        add("item.ultimatemodadditions.coin_1000", "1 luca");
        add("item.ultimatemodadditions.coin_10000", "10 lucas");
        add("item.ultimatemodadditions.coin_20000", "20 lucas");
        add("item.ultimatemodadditions.loot_bag_common", "Bolsa comun");
        add("item.ultimatemodadditions.loot_bag_uncommon", "Bolsa poco comun");
        add("item.ultimatemodadditions.loot_bag_epic", "Bolsa epica");
        add("item.ultimatemodadditions.loot_bag_mythic", "Bolsa mitica");
        add("item.ultimatemodadditions.loot_bag_legendary", "Bolsa legendaria");
        add("item.ultimatemodadditions.loot_bag_omega", "Bolsa omega");
        add("item.ultimatemodadditions.loot_bag_food", "Bolsa de comida");
        add("item.ultimatemodadditions.loot_bag_enchantment", "Bolsa de encantamientos");
        add("item.ultimatemodadditions.coliseum_key", "Llave del coliseo");
        add("block.ultimatemodadditions.coliseum_portal", "Portal del coliseo");
        add("block.ultimatemodadditions.exchange_stand", "Puesto de intercambio");
        add("money.ultimatemodadditions.mango", "%s mango");
        add("money.ultimatemodadditions.mangos", "%s mangos");
        add("money.ultimatemodadditions.luca", "%s luca");
        add("money.ultimatemodadditions.lucas", "%s lucas");
        add("money.ultimatemodadditions.combined", "%s con %s");
        add("jei.ultimatemodadditions.loot_bag", "Bolsas de botin");
        add("jei.ultimatemodadditions.exchange", "Puesto de intercambio");
        add("container.ultimatemodadditions.exchange", "Puesto de intercambio");
        add("bag_tier.ultimatemodadditions.common", "Comun");
        add("bag_tier.ultimatemodadditions.uncommon", "Poco comun");
        add("bag_tier.ultimatemodadditions.epic", "Epica");
        add("bag_tier.ultimatemodadditions.mythic", "Mitica");
        add("bag_tier.ultimatemodadditions.legendary", "Legendaria");
        add("bag_tier.ultimatemodadditions.omega", "Omega");
        add("bag_tier.ultimatemodadditions.food", "Comida");
        add("bag_tier.ultimatemodadditions.enchantment", "Encantamiento");
        add("tooltip.ultimatemodadditions.coin_value", "Valor: %s");
        add("tooltip.ultimatemodadditions.coin_hint", "Clic derecho junta tus monedas. Agachado rompe una en sueltos.");
        add("tooltip.ultimatemodadditions.bag_tier", "Rareza: %s");
        add("tooltip.ultimatemodadditions.bag_open", "Clic derecho para abrir. Agachado abre todas.");
        add("message.ultimatemodadditions.unknown_bag", "Esa bolsa no existe: %s");
        add("message.ultimatemodadditions.bag_announce", "%s ha abierto una %s");
        add("message.ultimatemodadditions.trade_locked", "No cumples los requisitos de este intercambio");
        add("message.ultimatemodadditions.trade_forbidden", "Esto no se puede comprar aca");
        add("message.ultimatemodadditions.trade_too_poor", "Te faltan %s");
        add("message.ultimatemodadditions.money_total", "Tenes %s");
        add("message.ultimatemodadditions.money_split", "Rompiste %s en sueltos");
        add("message.ultimatemodadditions.no_coliseum", "La dimension del coliseo no esta cargada");
        add("message.ultimatemodadditions.portal_invalid", "Esta obsidiana llorosa no es la esquina de un marco valido");
        add("message.ultimatemodadditions.portal_corner", "Esquina %s de %s");
        add("message.ultimatemodadditions.portal_lit", "El portal se abre");
        add("message.ultimatemodadditions.portal_already_lit", "Este portal ya esta abierto");
        add("message.ultimatemodadditions.portal_locked", "El portal te rechaza");
        add("message.ultimatemodadditions.portal_need_levels", "Experiencia: nivel %s de %s");
        add("tooltip.ultimatemodadditions.coliseum_key", "Abre un portal de obsidiana llorosa al coliseo.");
        add("tooltip.ultimatemodadditions.coliseum_key_corners", "Clic derecho en las cuatro esquinas del marco.");
        add("screen.ultimatemodadditions.no_offers", "No hay intercambios disponibles");
        add("screen.ultimatemodadditions.balance", "Billetera: %s");
        add("screen.ultimatemodadditions.free", "Gratis");
        add("key.categories.ultimatemodadditions", "Ultimate Mod Additions");
        add("command.ultimatemodadditions.difficulty_current", "Dificultad actual: %s");
        add("command.ultimatemodadditions.difficulty_set", "Dificultad cambiada a %s");
        add("command.ultimatemodadditions.unknown_difficulty", "Dificultad desconocida: %s");
        add("command.ultimatemodadditions.tps_header", "Rendimiento del servidor");
        add("command.ultimatemodadditions.tps_line", "TPS %s - MSPT %s ms (limite %s ms)");
        add("command.ultimatemodadditions.mspt_peak", "Peor tick de los ultimos 100: %s ms");
        add("command.ultimatemodadditions.tps_dimension", "%s: %s ms");
        add("command.ultimatemodadditions.ping", "%s: %s ms");
        add("command.ultimatemodadditions.arena_built", "El coliseo se reconstruyo");
        add("command.ultimatemodadditions.arena_failed", "No se pudo reconstruir el coliseo");
        add("command.ultimatemodadditions.no_coliseum", "La dimension del coliseo no esta cargada");
        add("command.ultimatemodadditions.spawners_count", "Hay %s generadores en el coliseo");
        add("command.ultimatemodadditions.spawners_placed", "Se pusieron %s generadores en la arena");
        add("command.ultimatemodadditions.spawners_cleared", "Se sacaron %s generadores del coliseo");
        add("command.ultimatemodadditions.spawners_disabled", "Los generadores de la arena estan apagados en la config");
        add("command.ultimatemodadditions.spawners_none", "No se puso ninguno. Fijate count y entities en la config");
    }
}
