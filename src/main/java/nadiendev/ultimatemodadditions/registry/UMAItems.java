package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.coliseum.ColiseumKeyItem;
import nadiendev.ultimatemodadditions.currency.BagTier;
import nadiendev.ultimatemodadditions.currency.CoinItem;
import nadiendev.ultimatemodadditions.currency.Denomination;
import nadiendev.ultimatemodadditions.currency.LootBagItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UMAItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UMA.MODID);

    private static final Map<Denomination, DeferredItem<CoinItem>> COINS = new EnumMap<>(Denomination.class);
    private static final Map<BagTier, DeferredItem<LootBagItem>> BAGS = new EnumMap<>(BagTier.class);

    static {
        for (Denomination denomination : Denomination.values()) {
            COINS.put(denomination, ITEMS.registerItem(denomination.itemName(),
                    properties -> new CoinItem(properties, denomination),
                    new Item.Properties().rarity(coinRarity(denomination))));
        }
        for (BagTier tier : BagTier.values()) {
            BAGS.put(tier, ITEMS.registerItem(tier.itemName(),
                    properties -> new LootBagItem(properties, tier),
                    new Item.Properties().stacksTo(16).rarity(tier.rarity())));
        }
    }

    public static final DeferredItem<ColiseumKeyItem> COLISEUM_KEY = ITEMS.registerItem("coliseum_key",
            ColiseumKeyItem::new, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    private UMAItems() {
    }

    private static Rarity coinRarity(Denomination denomination) {
        if (denomination.value() >= 10_000) {
            return Rarity.RARE;
        }
        return denomination.value() >= 1_000 ? Rarity.UNCOMMON : Rarity.COMMON;
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static DeferredItem<CoinItem> coinHolder(Denomination denomination) {
        return COINS.get(denomination);
    }

    public static CoinItem coin(Denomination denomination) {
        return COINS.get(denomination).get();
    }

    public static CoinItem coinByTier(String tier) {
        return coin(Denomination.byKey(tier));
    }

    public static DeferredItem<LootBagItem> bagHolder(BagTier tier) {
        return BAGS.get(tier);
    }

    public static LootBagItem bag(BagTier tier) {
        return BAGS.get(tier).get();
    }

    public static List<DeferredItem<CoinItem>> coins() {
        List<DeferredItem<CoinItem>> list = new ArrayList<>();
        for (Denomination denomination : Denomination.values()) {
            list.add(COINS.get(denomination));
        }
        return list;
    }

    public static List<DeferredItem<LootBagItem>> bags() {
        List<DeferredItem<LootBagItem>> list = new ArrayList<>();
        for (BagTier tier : BagTier.values()) {
            list.add(BAGS.get(tier));
        }
        return list;
    }
}
