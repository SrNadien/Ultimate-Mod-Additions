package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.coliseum.ColiseumKeyItem;
import nadiendev.ultimatemodadditions.currency.FlasheroSpawnEggItem;
import nadiendev.ultimatemodadditions.currency.BagTier;
import nadiendev.ultimatemodadditions.currency.CashItem;
import nadiendev.ultimatemodadditions.currency.DebitCardItem;
import nadiendev.ultimatemodadditions.currency.Denomination;
import nadiendev.ultimatemodadditions.currency.LootBagItem;
import nadiendev.ultimatemodadditions.currency.WalletItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UMAItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UMA.MODID);

    private static final Map<Denomination, DeferredItem<CashItem>> CASH = new EnumMap<>(Denomination.class);
    private static final Map<BagTier, DeferredItem<LootBagItem>> BAGS = new EnumMap<>(BagTier.class);

    static {
        for (Denomination denomination : Denomination.values()) {
            CASH.put(denomination, ITEMS.registerItem(denomination.itemName(),
                    properties -> new CashItem(properties, denomination),
                    new Item.Properties().rarity(cashRarity(denomination))));
        }
        for (BagTier tier : BagTier.values()) {
            BAGS.put(tier, ITEMS.registerItem(tier.itemName(),
                    properties -> new LootBagItem(properties, tier),
                    new Item.Properties().stacksTo(16).rarity(tier.rarity())));
        }
    }

    public static final DeferredItem<WalletItem> WALLET = ITEMS.registerItem("wallet",
            WalletItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<DebitCardItem> DEBIT_CARD = ITEMS.registerItem("debit_card",
            DebitCardItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final DeferredItem<ColiseumKeyItem> COLISEUM_KEY = ITEMS.registerItem("coliseum_key",
            ColiseumKeyItem::new, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<FlasheroSpawnEggItem> FLASHERO_SPAWN_EGG = ITEMS.registerItem("flashero_spawn_egg",
            FlasheroSpawnEggItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    private UMAItems() {
    }

    private static Rarity cashRarity(Denomination denomination) {
        if (denomination.cents() >= 1_000_000L) {
            return Rarity.RARE;
        }
        return denomination.cents() >= 100_000L ? Rarity.UNCOMMON : Rarity.COMMON;
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static DeferredItem<CashItem> cashHolder(Denomination denomination) {
        return CASH.get(denomination);
    }

    public static CashItem cash(Denomination denomination) {
        return CASH.get(denomination).get();
    }

    public static CashItem cashByKey(String key) {
        return cash(Denomination.byKey(key));
    }

    public static DeferredItem<LootBagItem> bagHolder(BagTier tier) {
        return BAGS.get(tier);
    }

    public static LootBagItem bag(BagTier tier) {
        return BAGS.get(tier).get();
    }

    public static List<DeferredItem<CashItem>> allCash() {
        List<DeferredItem<CashItem>> list = new ArrayList<>(CASH.values());
        list.sort(Comparator.comparingLong(holder -> holder.get().cents()));
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
