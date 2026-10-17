package nadiendev.ultimatemodadditions.currency;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.List;
import java.util.regex.Pattern;

public class CashChestModifier extends LootModifier {

    public static final MapCodec<CashChestModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, CashChestModifier::new));

    private static final List<String> EXCLUDED_PREFIXES = List.of("blocks/", "entities/", "gameplay/", "archaeology/");

    public CashChestModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!UMAConfig.COMMON.cashChestsEnabled.get() || !isChestLike(context)) {
            return loot;
        }
        ResourceLocation table = context.getQueriedLootTableId();
        if (!tableAllowed(table.toString())) {
            return loot;
        }
        if (context.getRandom().nextDouble() >= UMAConfig.COMMON.cashChestChance.get()) {
            return loot;
        }
        ItemStack cash = CashDrops.randomChestCash(context.getRandom());
        if (!cash.isEmpty()) {
            loot.add(cash);
        }
        return loot;
    }

    private static boolean isChestLike(LootContext context) {
        if (!context.hasParam(LootContextParams.ORIGIN)
                || context.hasParam(LootContextParams.BLOCK_STATE)
                || context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
            return false;
        }
        String path = context.getQueriedLootTableId().getPath();
        for (String prefix : EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    private static boolean tableAllowed(String table) {
        for (String pattern : UMAConfig.COMMON.cashChestBlacklist.get()) {
            if (matches(pattern, table)) {
                return false;
            }
        }
        List<? extends String> whitelist = UMAConfig.COMMON.cashChestWhitelist.get();
        if (whitelist.isEmpty()) {
            return true;
        }
        for (String pattern : whitelist) {
            if (matches(pattern, table)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String pattern, String table) {
        try {
            return Pattern.compile(pattern).matcher(table).matches();
        } catch (IllegalArgumentException exception) {
            UMA.LOGGER.warn("Invalid loot table pattern in the config: {}", pattern);
            return false;
        }
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
