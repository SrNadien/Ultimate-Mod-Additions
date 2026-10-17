package nadiendev.ultimatemodadditions.jobs;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.data.JobAction;
import nadiendev.ultimatemodadditions.registry.UMAAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Set;

@EventBusSubscriber(modid = UMA.MODID)
public final class JobEvents {

    private static final String SPAWNER_TAG = "uma_spawner_mob";

    private JobEvents() {
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        BlockState state = event.getState();
        JobsService.award(player, JobAction.Type.BREAK_BLOCK,
                action -> action.matchesBlock(state) && (!action.fullyGrown() || grown(state)));
    }

    private static boolean grown(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        return true;
    }

    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.SPAWNER) {
            event.getEntity().addTag(SPAWNER_TAG);
        }
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player) || event.getEntity() instanceof Player) {
            return;
        }
        if (UMAConfig.COMMON.jobsIgnoreSpawnerMobs.get() && event.getEntity().getTags().contains(SPAWNER_TAG)) {
            return;
        }
        JobsService.award(player, JobAction.Type.KILL_ENTITY,
                action -> action.matchesEntity(event.getEntity().getType()));
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        for (ItemStack drop : event.getDrops()) {
            JobsService.award(player, JobAction.Type.FISH, action -> action.matchesItem(drop));
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState state = event.getPlacedBlock();
        JobsService.award(player, JobAction.Type.PLACE_BLOCK, action -> action.matchesBlock(state));
    }

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack crafted = event.getCrafting();
        JobsService.award(player, JobAction.Type.CRAFT, Math.min(64, crafted.getCount()),
                action -> action.matchesItem(crafted));
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack smelted = event.getSmelting();
        JobsService.award(player, JobAction.Type.SMELT, Math.min(64, smelted.getCount()),
                action -> action.matchesItem(smelted));
    }

    @SubscribeEvent
    public static void onBrew(PlayerBrewedPotionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack potion = event.getStack();
        JobsService.award(player, JobAction.Type.BREW, action -> action.matchesItem(potion));
    }

    @SubscribeEvent
    public static void onEnchant(PlayerEnchantItemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack enchanted = event.getEnchantedItem();
        JobsService.award(player, JobAction.Type.ENCHANT, action -> action.matchesItem(enchanted));
    }

    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (!(event.getCausedByPlayer() instanceof ServerPlayer player) || event.getChild() == null) {
            return;
        }
        JobsService.award(player, JobAction.Type.BREED, action -> action.matchesEntity(event.getChild().getType()));
    }

    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        if (!(event.getTamer() instanceof ServerPlayer player)) {
            return;
        }
        JobsService.award(player, JobAction.Type.TAME, action -> action.matchesEntity(event.getAnimal().getType()));
    }

    @SubscribeEvent
    public static void onExplore(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) {
            return;
        }
        int limit = UMAConfig.COMMON.jobsExploreLimit.get();
        if (limit <= 0 || JobsService.progress(player).isEmpty()) {
            return;
        }
        Set<String> explored = player.getData(UMAAttachments.EXPLORED);
        String key = player.level().dimension().location() + "@" + new ChunkPos(player.blockPosition()).toLong();
        if (explored.size() < limit && explored.add(key)) {
            JobsService.award(player, JobAction.Type.EXPLORE, action -> true);
        }
    }
}
