package nadiendev.ultimatemodadditions.event;

import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = UMA.MODID)
public final class BeeEvents {

    public static final String ID = "bee";
    private static final String TAG = "uma_charged_bee";

    private static final class Active {
        private final ResourceKey<Level> dimension;
        private int rounds;
        private int wait;

        private Active(ResourceKey<Level> dimension, int rounds, int wait) {
            this.dimension = dimension;
            this.rounds = rounds;
            this.wait = wait;
        }
    }

    private static final Map<UUID, Active> ACTIVE = new HashMap<>();
    private static int checkClock;

    private BeeEvents() {
    }

    private static JsonObject config() {
        return EventConfigs.get(EventDefaults.BEE);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Bee bee) || !bee.getTags().contains(TAG)) {
            return;
        }
        if (!ACTIVE.containsKey(bee.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        JsonObject config = config();
        Item summoner = ItemSpec.item(JsonValues.string(config, "summoner_item", "minecraft:honeycomb_block"));
        if (summoner == null || !event.getItemStack().is(summoner) || !EventState.get(player.server).enabled(ID)
                || JsonValues.bool(config, "summon_needs_sneak", true) && !player.isShiftKeyDown()) {
            return;
        }
        if (!player.isCreative()) {
            event.getItemStack().shrink(1);
        }
        event.setCanceled(true);
        summon(player, player.getX(), player.getY() + 1.5D, player.getZ());
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickBees(server);
        JsonObject spawn = JsonValues.object(config(), "spawn");
        if (!JsonValues.bool(spawn, "enabled", true) || !EventState.get(server).enabled(ID)) {
            return;
        }
        checkClock++;
        if (checkClock < Math.max(20, JsonValues.integer(spawn, "check_ticks", 1200))) {
            return;
        }
        checkClock = 0;
        RandomSource random = server.overworld().getRandom();
        EventState state = EventState.get(server);
        long now = server.overworld().getGameTime();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || random.nextDouble() > JsonValues.decimal(spawn, "chance", 0.15D)) {
                continue;
            }
            String key = "bee_last_" + player.getUUID();
            if (now - state.timer(key, -1_000_000L) < JsonValues.integer(spawn, "player_cooldown_ticks", 12000)) {
                continue;
            }
            BlockPos spot = findSpot(player, spawn, random);
            if (spot == null) {
                continue;
            }
            state.setTimer(key, now);
            summon(player, spot.getX() + 0.5D, spot.getY() + 0.5D, spot.getZ() + 0.5D);
        }
    }

    public static boolean summon(ServerPlayer target, double x, double y, double z) {
        ServerLevel level = target.serverLevel();
        JsonObject config = config();
        Bee bee = EntityType.BEE.create(level);
        if (bee == null) {
            return false;
        }
        String custom = JsonValues.string(config, "name", "");
        MutableComponent name = custom.isEmpty() ? Component.translatable("entity.ultimatemodadditions.charged_bee")
                : Component.literal(custom);
        bee.moveTo(x, y, z, target.getRandom().nextFloat() * 360.0F, 0.0F);
        bee.setCustomName(name.copy().withStyle(ChatFormatting.YELLOW));
        bee.setCustomNameVisible(true);
        bee.setGlowingTag(true);
        bee.setPersistenceRequired();
        bee.addTag(TAG);
        int min = JsonValues.integer(config, "rounds_min", 5);
        int max = Math.max(min, JsonValues.integer(config, "rounds_max", 12));
        int rounds = min + target.getRandom().nextInt(max - min + 1);
        int wait = Math.max(1, JsonValues.integer(config, "wait_ticks", 60));
        ACTIVE.put(bee.getUUID(), new Active(level.dimension(), rounds, wait));
        if (!level.addFreshEntity(bee)) {
            ACTIVE.remove(bee.getUUID());
            return false;
        }
        if (JsonValues.bool(config, "announce", true)) {
            MinecraftServer server = target.server;
            Component line = Component.translatable("message.ultimatemodadditions.bee_appeared", name.copy()
                    .withStyle(ChatFormatting.GOLD), target.getDisplayName(), rounds).withStyle(ChatFormatting.WHITE);
            server.getPlayerList().broadcastSystemMessage(line, false);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0F, 1.0F);
            }
        }
        return true;
    }

    private static void tickBees(MinecraftServer server) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Active>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Active> entry = iterator.next();
            Active active = entry.getValue();
            ServerLevel level = server.getLevel(active.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            Entity found = level.getEntity(entry.getKey());
            if (found == null) {
                continue;
            }
            if (!(found instanceof Bee bee) || !bee.isAlive()) {
                iterator.remove();
                continue;
            }
            active.wait--;
            if (active.wait > 0) {
                continue;
            }
            JsonObject config = config();
            active.wait = Math.max(1, JsonValues.integer(config, "wait_ticks", 60));
            HolderLookup.Provider registries = server.registryAccess();
            RewardTable table = new RewardTable(config, registries, bee.getRandom());
            if (active.rounds <= 0) {
                finish(server, bee, table);
                iterator.remove();
                continue;
            }
            active.rounds--;
            if (!table.onlyManual()) {
                ItemStack prize = table.prize();
                if (!prize.isEmpty()) {
                    drop(level, bee, prize);
                }
            }
        }
    }

    private static void finish(MinecraftServer server, Bee bee, RewardTable table) {
        ServerLevel level = (ServerLevel) bee.level();
        table.coins().forEach(stack -> drop(level, bee, stack));
        table.manual(0).forEach(stack -> drop(level, bee, stack));
        if (JsonValues.bool(config(), "announce", true)) {
            server.getPlayerList().broadcastSystemMessage(Component.translatable("message.ultimatemodadditions.bee_left",
                    bee.getCustomName() == null ? bee.getName() : bee.getCustomName()).withStyle(ChatFormatting.WHITE), false);
        }
        bee.discard();
    }

    private static void drop(ServerLevel level, Bee bee, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, bee.getX(), bee.getY(), bee.getZ(), stack);
        item.setGlowingTag(true);
        level.addFreshEntity(item);
    }

    private static BlockPos findSpot(ServerPlayer player, JsonObject spawn, RandomSource random) {
        ServerLevel level = player.serverLevel();
        int near = JsonValues.integer(spawn, "min_distance", 4);
        int far = Math.max(near + 1, JsonValues.integer(spawn, "max_distance", 10));
        int attempts = Math.max(1, JsonValues.integer(spawn, "attempts", 30));
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(far * 2 + 1) - far;
            int dz = random.nextInt(far * 2 + 1) - far;
            int dy = random.nextInt(5) - 2;
            if (Math.abs(dx) < near && Math.abs(dz) < near) {
                continue;
            }
            BlockPos pos = player.blockPosition().offset(dx, dy, dz);
            if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return null;
    }
}
