package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatemodadditions.util.IntRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Consumer;

public record DropEntry(Optional<Item> item,
                        IntRange count,
                        float chance,
                        int weight,
                        float lootingBonus,
                        Optional<DataComponentPatch> components,
                        Optional<ResourceLocation> lootTable,
                        Optional<ResourceLocation> bag) {

    public static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(DropEntry::item),
            IntRange.CODEC.optionalFieldOf("count", IntRange.exactly(1)).forGetter(DropEntry::count),
            Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(DropEntry::chance),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(DropEntry::weight),
            Codec.FLOAT.optionalFieldOf("looting_bonus", 0.0F).forGetter(DropEntry::lootingBonus),
            DataComponentPatch.CODEC.optionalFieldOf("components").forGetter(DropEntry::components),
            ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(DropEntry::lootTable),
            ResourceLocation.CODEC.optionalFieldOf("bag").forGetter(DropEntry::bag)
    ).apply(instance, DropEntry::new));

    public boolean rollChance(RandomSource random, int looting) {
        float effective = chance + lootingBonus * looting;
        return effective >= 1.0F || random.nextFloat() < effective;
    }

    public void generate(ServerLevel level, BlockPos pos, RandomSource random, int looting, Consumer<ItemStack> output) {
        if (!rollChance(random, looting)) {
            return;
        }
        if (lootTable.isPresent()) {
            LootTable table = level.getServer().reloadableRegistries()
                    .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTable.get()));
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .create(LootContextParamSets.GIFT);
            table.getRandomItems(params, output);
        }
        if (bag.isPresent()) {
            ItemStack bagStack = nadiendev.ultimatemodadditions.currency.LootBagItem.create(bag.get(), count.sample(random));
            if (!bagStack.isEmpty()) {
                output.accept(bagStack);
            }
            return;
        }
        item.ifPresent(value -> {
            int amount = count.sample(random);
            while (amount > 0) {
                ItemStack stack = new ItemStack(value, Math.min(amount, value.getDefaultMaxStackSize()));
                components.ifPresent(stack::applyComponents);
                amount -= stack.getCount();
                output.accept(stack);
            }
        });
    }
}
