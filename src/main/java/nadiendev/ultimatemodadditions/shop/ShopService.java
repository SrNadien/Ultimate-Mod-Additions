package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.config.UMAConfig.PayTarget;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.CashItem;
import nadiendev.ultimatemodadditions.currency.ExchangeLogic;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Payout;
import nadiendev.ultimatemodadditions.currency.Wealth;
import nadiendev.ultimatemodadditions.data.ItemStackKey;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import nadiendev.ultimatemodadditions.data.TradeOffer;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.network.payload.ShopActionPayload;
import nadiendev.ultimatemodadditions.network.payload.ShopSyncPayload;
import nadiendev.ultimatemodadditions.registry.UMAAttachments;
import nadiendev.ultimatemodadditions.registry.UMATags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ShopService {

    private static final Set<String> TRANSLATED = Set.of("bags", "general");
    private static final BigDecimal CENTS_PER_MANGO = BigDecimal.valueOf(100L);

    private ShopService() {
    }

    public static Map<ResourceLocation, ShopCategory> merged(MinecraftServer server) {
        Map<ResourceLocation, ShopCategory> merged = new LinkedHashMap<>(UMAData.shop());
        ModShops.fill(server).forEach((id, extra) -> {
            ShopCategory base = merged.get(id);
            if (base != null) {
                List<ShopItem> items = new ArrayList<>(base.items());
                items.addAll(extra);
                merged.put(id, new ShopCategory(base.name(), base.icon(), base.order(), items));
                return;
            }
            ShopLayout.Def def = ShopLayout.def(id.getPath());
            if (def != null && UMA.MODID.equals(id.getNamespace()) && !extra.isEmpty()) {
                Item icon = BuiltInRegistries.ITEM.get(ResourceLocation.parse(def.icon()));
                merged.put(id, new ShopCategory(categoryName(id), icon, def.order(), new ArrayList<>(extra)));
            }
        });
        addTrades(merged);
        merged.putAll(ModShops.get(server));
        scalePrices(merged);
        ShopEdits edits = ShopEdits.get(server);
        merged.putAll(edits.custom());
        edits.hidden().forEach(merged::remove);
        return merged;
    }

    private static void scalePrices(Map<ResourceLocation, ShopCategory> merged) {
        double inflation = 1.0D + UMAConfig.COMMON.shopInflationPercent.get() / 100.0D;
        double buyScale = inflation;
        double sellScale = UMAConfig.COMMON.shopSellMultiplier.get()
                * (UMAConfig.COMMON.shopInflationOnSell.get() ? inflation : 1.0D);
        if (buyScale == 1.0D && sellScale == 1.0D) {
            return;
        }
        merged.replaceAll((id, category) -> {
            List<ShopItem> items = new ArrayList<>(category.items().size());
            for (ShopItem item : category.items()) {
                if (item.stack().getItem() instanceof CashItem) {
                    items.add(item);
                } else {
                    items.add(new ShopItem(item.stack(), item.buy() * buyScale, item.sell() * sellScale, item.taxed()));
                }
            }
            return new ShopCategory(category.name(), category.icon(), category.order(), items);
        });
    }

    private static void addTrades(Map<ResourceLocation, ShopCategory> merged) {
        Map<ResourceLocation, List<ShopItem>> extra = new LinkedHashMap<>();
        for (TradeOffer offer : UMAData.trades().values()) {
            if (!offer.enabled() || !ExchangeLogic.purchasable(offer) || !offer.cost().isEmpty() || offer.price() <= 0) {
                continue;
            }
            ItemStack stack = offer.result().asStack();
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation target = ResourceLocation.fromNamespaceAndPath(UMA.MODID,
                    offer.category().toLowerCase(Locale.ROOT));
            extra.computeIfAbsent(target, key -> new ArrayList<>())
                    .add(new ShopItem(stack.copyWithCount(1), (double) offer.price() / stack.getCount(), 0.0D, true));
        }
        extra.forEach((id, items) -> {
            ShopCategory base = merged.get(id);
            if (base == null) {
                merged.put(id, new ShopCategory(categoryName(id), items.get(0).stack().getItem(), 50, items));
                return;
            }
            List<ShopItem> combined = new ArrayList<>(base.items());
            for (ShopItem item : items) {
                combined.removeIf(existing -> ItemStackKey.same(existing.stack(), item.stack()));
                combined.add(item);
            }
            merged.put(id, new ShopCategory(base.name(), base.icon(), base.order(), combined));
        });
    }

    private static String categoryName(ResourceLocation id) {
        String path = id.getPath();
        if (TRANSLATED.contains(path) || ShopLayout.IDS.contains(path)) {
            return "shop.ultimatemodadditions.category." + path;
        }
        String spaced = path.replace('_', ' ').trim();
        return spaced.isEmpty() ? path : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    public static List<Map.Entry<ResourceLocation, ShopCategory>> categories(MinecraftServer server) {
        List<Map.Entry<ResourceLocation, ShopCategory>> list = new ArrayList<>(merged(server).entrySet());
        list.sort(Comparator.<Map.Entry<ResourceLocation, ShopCategory>>comparingInt(entry -> entry.getValue().order())
                .thenComparing(entry -> entry.getKey().toString()));
        return list;
    }

    public static BigInteger balance(ServerPlayer player) {
        if (UMAConfig.COMMON.bankEnabled.get()) {
            return BankService.balance(player.server, player.getUUID());
        }
        return Money.totalBig(player);
    }

    private static boolean charge(ServerPlayer player, BigInteger cents) {
        if (UMAConfig.COMMON.bankEnabled.get()) {
            BigInteger taken = BankService.withdraw(player.server, player.getUUID(), cents);
            if (taken.compareTo(cents) < 0) {
                BankService.deposit(player.server, player.getUUID(), taken);
                return false;
            }
            return true;
        }
        return Money.take(player, Wealth.clamp(cents));
    }

    private static BigInteger cents(double mangos, int times, double factor) {
        return BigDecimal.valueOf(mangos).multiply(CENTS_PER_MANGO).multiply(BigDecimal.valueOf(times))
                .multiply(BigDecimal.valueOf(factor)).setScale(0, RoundingMode.DOWN).toBigInteger();
    }

    public static void open(ServerPlayer player) {
        send(player, true, false, false, true);
    }

    public static void openStand(ServerPlayer player) {
        send(player, true, true, false, true);
    }

    public static void openTiendita(ServerPlayer player) {
        send(player, true, true, true, true);
    }

    public static void send(ServerPlayer player, boolean open, boolean stand, boolean tiendita, boolean reload) {
        List<ShopSyncPayload.Entry> entries = new ArrayList<>();
        if (open || reload) {
            for (Map.Entry<ResourceLocation, ShopCategory> entry : categories(player.server)) {
                entries.add(new ShopSyncPayload.Entry(entry.getKey(), entry.getValue()));
            }
        }
        PacketDistributor.sendToPlayer(player, new ShopSyncPayload(open, stand, tiendita, reload, ShopPermissions.canAdd(player),
                ShopPermissions.canRemove(player), balance(player).toString(),
                UMAConfig.COMMON.shopSellCommission.get(), entries));
    }

    public static void handle(ServerPlayer player, ShopActionPayload payload) {
        if (!UMAConfig.COMMON.shopEnabled.get()) {
            return;
        }
        boolean edited = false;
        switch (payload.action()) {
            case BUY -> trade(player, payload, true);
            case SELL -> trade(player, payload, false);
            case ADD_ITEM -> edited = addItem(player, payload);
            case EDIT_PRICE -> edited = editPrice(player, payload);
            case REMOVE_ITEM -> edited = removeItem(player, payload);
            case ADD_CATEGORY -> edited = addCategory(player, payload);
            case REMOVE_CATEGORY -> edited = removeCategory(player, payload);
        }
        send(player, false, false, false, edited);
    }

    private static void trade(ServerPlayer player, ShopActionPayload payload, boolean buying) {
        ShopCategory category = merged(player.server).get(payload.category());
        if (category == null || payload.index() < 0 || payload.index() >= category.items().size()) {
            return;
        }
        ShopItem item = category.items().get(payload.index());
        int amount = Math.max(1, Math.min(payload.amount(), UMAConfig.COMMON.shopMaxAmount.get()));
        if (buying) {
            buy(player, item, amount);
        } else {
            sell(player, item, amount);
        }
    }

    private static boolean denied(ServerPlayer player, boolean allowed, String node) {
        if (!allowed) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_no_permission", node),
                    ChatFormatting.RED);
        }
        return !allowed;
    }

    private static boolean addItem(ServerPlayer player, ShopActionPayload payload) {
        if (denied(player, ShopPermissions.canAdd(player), ShopPermissions.ADD_NODE)) {
            return false;
        }
        ShopCategory category = merged(player.server).get(payload.category());
        ItemStack held = player.getMainHandItem();
        if (category == null) {
            return false;
        }
        if (held.isEmpty()) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_admin_need_hand"),
                    ChatFormatting.RED);
            return false;
        }
        if (payload.buy() <= 0.0D && payload.sell() <= 0.0D) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_admin_need_price"),
                    ChatFormatting.RED);
            return false;
        }
        ItemStack template = held.copyWithCount(1);
        List<ShopItem> items = new ArrayList<>(category.items());
        items.removeIf(existing -> ItemStackKey.same(existing.stack(), template));
        items.add(new ShopItem(template, Math.max(0.0D, payload.buy()), Math.max(0.0D, payload.sell()), true));
        ShopEdits.get(player.server).put(payload.category(),
                new ShopCategory(category.name(), category.icon(), category.order(), items));
        message(player, Component.translatable("message.ultimatemodadditions.shop_admin_item_added",
                template.getHoverName(), Component.translatable(category.name())), ChatFormatting.GREEN);
        return true;
    }

    private static boolean editPrice(ServerPlayer player, ShopActionPayload payload) {
        if (denied(player, ShopPermissions.canAdd(player), ShopPermissions.ADD_NODE)) {
            return false;
        }
        ShopCategory category = merged(player.server).get(payload.category());
        if (category == null || payload.index() < 0 || payload.index() >= category.items().size()) {
            return false;
        }
        ShopItem old = category.items().get(payload.index());
        double buy = payload.buy() < 0.0D ? old.buy() : payload.buy();
        double sell = payload.sell() < 0.0D ? old.sell() : payload.sell();
        List<ShopItem> items = new ArrayList<>(category.items());
        items.set(payload.index(), new ShopItem(old.stack(), buy, sell, old.taxed()));
        ShopEdits.get(player.server).put(payload.category(),
                new ShopCategory(category.name(), category.icon(), category.order(), items));
        message(player, Component.translatable("message.ultimatemodadditions.shop_admin_price_updated",
                old.stack().getHoverName()), ChatFormatting.GREEN);
        return true;
    }

    private static boolean removeItem(ServerPlayer player, ShopActionPayload payload) {
        if (denied(player, ShopPermissions.canRemove(player), ShopPermissions.REMOVE_NODE)) {
            return false;
        }
        ShopCategory category = merged(player.server).get(payload.category());
        if (category == null || payload.index() < 0 || payload.index() >= category.items().size()) {
            return false;
        }
        List<ShopItem> items = new ArrayList<>(category.items());
        ShopItem removed = items.remove(payload.index());
        ShopEdits.get(player.server).put(payload.category(),
                new ShopCategory(category.name(), category.icon(), category.order(), items));
        message(player, Component.translatable("message.ultimatemodadditions.shop_admin_item_removed",
                removed.stack().getHoverName()), ChatFormatting.YELLOW);
        return true;
    }

    private static boolean addCategory(ServerPlayer player, ShopActionPayload payload) {
        if (denied(player, ShopPermissions.canAdd(player), ShopPermissions.ADD_NODE)) {
            return false;
        }
        String name = payload.text().trim();
        if (name.isEmpty()) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_admin_need_name"),
                    ChatFormatting.RED);
            return false;
        }
        ItemStack held = player.getMainHandItem();
        Map<ResourceLocation, ShopCategory> existing = merged(player.server);
        String slug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (slug.isEmpty()) {
            slug = "zone";
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(UMA.MODID, "custom_" + slug);
        for (int n = 2; existing.containsKey(id); n++) {
            id = ResourceLocation.fromNamespaceAndPath(UMA.MODID, "custom_" + slug + "_" + n);
        }
        int order = existing.values().stream().mapToInt(ShopCategory::order).max().orElse(0) + 1;
        ShopEdits.get(player.server).put(id, new ShopCategory(name,
                held.isEmpty() ? Items.CHEST : held.getItem(), order, List.of()));
        message(player, Component.translatable("message.ultimatemodadditions.shop_admin_zone_added", name),
                ChatFormatting.GREEN);
        return true;
    }

    private static boolean removeCategory(ServerPlayer player, ShopActionPayload payload) {
        if (denied(player, ShopPermissions.canRemove(player), ShopPermissions.REMOVE_NODE)) {
            return false;
        }
        ShopCategory category = merged(player.server).get(payload.category());
        if (category == null) {
            return false;
        }
        boolean fromPack = UMAData.shop().containsKey(payload.category())
                || ModShops.get(player.server).containsKey(payload.category());
        ShopEdits.get(player.server).remove(payload.category(), fromPack);
        message(player, Component.translatable("message.ultimatemodadditions.shop_admin_zone_removed",
                Component.translatable(category.name())), ChatFormatting.YELLOW);
        return true;
    }

    private static void buy(ServerPlayer player, ShopItem item, int amount) {
        if (!item.buyable() || item.stack().is(UMATags.NOT_PURCHASABLE)) {
            message(player, Component.translatable("message.ultimatemodadditions.trade_forbidden"), ChatFormatting.RED);
            return;
        }
        BigInteger unit = cents(item.buy(), 1, 1.0D);
        if (unit.signum() <= 0) {
            return;
        }
        BigInteger affordable = balance(player).divide(unit);
        int times = affordable.min(BigInteger.valueOf(amount)).intValue();
        BigInteger total = unit.multiply(BigInteger.valueOf(times));
        if (times <= 0 || !charge(player, total)) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_too_poor",
                    Money.format(unit)), ChatFormatting.RED);
            return;
        }
        boolean overflow = give(player, item.stack(), times);
        message(player, Component.translatable("message.ultimatemodadditions.shop_bought", times,
                item.stack().getHoverName(), Money.format(total)), ChatFormatting.GREEN);
        if (overflow) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_dropped"),
                    ChatFormatting.YELLOW);
        }
    }

    private static void sell(ServerPlayer player, ShopItem item, int amount) {
        if (!item.sellable()) {
            return;
        }
        int owned = count(player, item.stack());
        int times = Math.min(owned, amount);
        if (times <= 0) {
            message(player, Component.translatable("message.ultimatemodadditions.shop_none_to_sell",
                    item.stack().getHoverName()), ChatFormatting.RED);
            return;
        }
        remove(player, item.stack(), times);
        double factor = item.taxed() ? Math.max(0.0D, 1.0D - UMAConfig.COMMON.shopSellCommission.get() / 100.0D) : 1.0D;
        BigInteger revenue = cents(item.sell(), times, factor);
        BigInteger paid = Payout.give(player, revenue, PayTarget.ACCOUNT);
        message(player, Component.translatable("message.ultimatemodadditions.shop_sold", times,
                item.stack().getHoverName(), Money.format(paid)), ChatFormatting.GREEN);
    }

    private static boolean give(ServerPlayer player, ItemStack template, int amount) {
        boolean overflow = false;
        int remaining = amount;
        while (remaining > 0) {
            int part = Math.min(remaining, Math.max(1, template.getMaxStackSize()));
            ItemStack copy = template.copyWithCount(part);
            player.getInventory().add(copy);
            if (!copy.isEmpty()) {
                player.drop(copy, false);
                overflow = true;
            }
            remaining -= part;
        }
        return overflow;
    }

    private static int count(ServerPlayer player, ItemStack template) {
        int found = 0;
        for (ItemStack stack : slots(player)) {
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                found += stack.getCount();
            }
        }
        return found;
    }

    private static void remove(ServerPlayer player, ItemStack template, int amount) {
        int remaining = amount;
        for (ItemStack stack : slots(player)) {
            if (remaining <= 0) {
                break;
            }
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
    }

    private static List<ItemStack> slots(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        List<ItemStack> stacks = new ArrayList<>(inventory.items);
        stacks.addAll(inventory.offhand);
        return stacks;
    }

    private static void message(ServerPlayer player, Component text, ChatFormatting color) {
        player.displayClientMessage(text.copy().withStyle(color), true);
    }
}
