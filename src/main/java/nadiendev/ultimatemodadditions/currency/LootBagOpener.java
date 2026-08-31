package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.data.DropEntry;
import nadiendev.ultimatemodadditions.data.LootBagDefinition;
import nadiendev.ultimatemodadditions.data.UMAData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class LootBagOpener {

    private LootBagOpener() {
    }

    public static boolean open(ServerPlayer player, ResourceLocation bagId) {
        LootBagDefinition definition = UMAData.lootBags().get(bagId);
        if (definition == null) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        RandomSource random = player.getRandom();
        BlockPos pos = player.blockPosition();
        List<ItemStack> results = new ArrayList<>();

        for (DropEntry entry : definition.guaranteed()) {
            entry.generate(level, pos, random, 0, results::add);
        }

        List<DropEntry> pool = new ArrayList<>(definition.entries());
        int rolls = definition.rolls().sample(random);
        for (int i = 0; i < rolls && !pool.isEmpty(); i++) {
            DropEntry picked = pickWeighted(pool, random);
            if (picked == null) {
                break;
            }
            if (definition.unique()) {
                pool.remove(picked);
            }
            picked.generate(level, pos, random, 0, results::add);
        }

        for (ItemStack stack : results) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        definition.openSound()
                .flatMap(id -> BuiltInRegistries.SOUND_EVENT.getOptional(id))
                .ifPresent(sound -> playSound(level, player, sound));

        if (definition.announce()) {
            String bagName = definition.name().orElse(bagId.getPath());
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("message.ultimatemodadditions.bag_announce",
                            player.getDisplayName(), Component.literal(bagName)).withStyle(ChatFormatting.GOLD), false);
        }
        return true;
    }

    private static void playSound(ServerLevel level, ServerPlayer player, SoundEvent sound) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static DropEntry pickWeighted(List<DropEntry> pool, RandomSource random) {
        int total = 0;
        for (DropEntry entry : pool) {
            total += Math.max(0, entry.weight());
        }
        if (total <= 0) {
            return pool.isEmpty() ? null : pool.get(random.nextInt(pool.size()));
        }
        int roll = random.nextInt(total);
        for (DropEntry entry : pool) {
            roll -= Math.max(0, entry.weight());
            if (roll < 0) {
                return entry;
            }
        }
        return pool.get(pool.size() - 1);
    }
}
