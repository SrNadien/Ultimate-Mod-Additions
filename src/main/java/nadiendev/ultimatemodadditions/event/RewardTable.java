package nadiendev.ultimatemodadditions.event;

import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.ban.BanService;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RewardTable {

    private final JsonObject config;
    private final HolderLookup.Provider registries;
    private final RandomSource random;

    public RewardTable(JsonObject config, HolderLookup.Provider registries, RandomSource random) {
        this.config = config;
        this.registries = registries;
        this.random = random;
    }

    public ItemStack prize() {
        List<String> excluded = JsonValues.strings(config, "excluded");
        ItemSpec spec = null;
        List<String> rare = JsonValues.strings(config, "rare_items");
        if (!rare.isEmpty() && random.nextDouble() < JsonValues.decimal(config, "rare_chance", 0.01D)) {
            spec = pick(rare, excluded);
        }
        if (spec == null) {
            spec = pick(JsonValues.strings(config, "items"), excluded);
        }
        return spec == null ? ItemStack.EMPTY : spec.stack();
    }

    public List<ItemStack> coins() {
        JsonObject coins = JsonValues.object(config, "coins");
        List<ItemSpec> pool = new ArrayList<>();
        for (String text : JsonValues.strings(coins, "items")) {
            ItemSpec spec = ItemSpec.parse(text, registries);
            if (spec != null) {
                pool.add(spec);
            }
        }
        if (pool.isEmpty()) {
            return List.of();
        }
        int min = JsonValues.integer(coins, "min", 1);
        int max = Math.max(min, JsonValues.integer(coins, "max", 5));
        int amount = min + random.nextInt(max - min + 1);
        Map<ItemSpec, Integer> counts = new HashMap<>();
        for (int i = 0; i < amount; i++) {
            counts.merge(pool.get(random.nextInt(pool.size())), 1, Integer::sum);
        }
        List<ItemStack> stacks = new ArrayList<>();
        counts.forEach((spec, count) -> stacks.add(spec.stack(count)));
        return stacks;
    }

    public List<ItemStack> manual(int level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (JsonObject drop : JsonValues.objects(config, "manual_drops")) {
            List<String> levels = JsonValues.strings(drop, "levels");
            if (level > 0 && !levels.isEmpty() && !levels.contains(String.valueOf(level))) {
                continue;
            }
            if (random.nextDouble() >= JsonValues.decimal(drop, "chance", 1.0D)) {
                continue;
            }
            ItemSpec spec = ItemSpec.parse(JsonValues.string(drop, "item", ""), registries);
            if (spec == null || BanService.banned(spec.item())) {
                continue;
            }
            int min = JsonValues.integer(drop, "min", 1);
            int max = Math.max(min, JsonValues.integer(drop, "max", min));
            stacks.add(spec.stack(min + random.nextInt(max - min + 1)));
        }
        return stacks;
    }

    public boolean onlyManual() {
        return JsonValues.bool(config, "only_manual", false);
    }

    private ItemSpec pick(List<String> entries, List<String> excluded) {
        List<ItemSpec> valid = new ArrayList<>();
        for (String text : entries) {
            ItemSpec spec = ItemSpec.parse(text, registries);
            if (spec == null || BanService.banned(spec.item())) {
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(spec.item()).toString();
            if (excluded.contains(id)) {
                continue;
            }
            valid.add(spec);
        }
        return valid.isEmpty() ? null : valid.get(random.nextInt(valid.size()));
    }
}
