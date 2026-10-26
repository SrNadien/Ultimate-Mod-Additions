package nadiendev.ultimatemodadditions.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = UMA.MODID)
public final class NpcEvents {

    public static final String ID = "npc_missions";
    private static final String TAG = "uma_npc";
    private static final String LEVEL_KEY = "uma_npc_level";
    private static final String ITEM_KEY = "uma_npc_item";
    private static final String COUNT_KEY = "uma_npc_count";

    private static int clock;

    private NpcEvents() {
    }

    private static JsonObject config() {
        return EventConfigs.get(EventDefaults.NPC);
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        JsonObject config = config();
        clock++;
        if (clock < Math.max(20, JsonValues.integer(config, "spawn_period_ticks", 6000))) {
            return;
        }
        clock = 0;
        if (!EventState.get(server).enabled(ID)) {
            return;
        }
        List<String> dimensions = JsonValues.strings(config, "dimensions");
        int radius = JsonValues.integer(config, "search_radius", 48);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            if (player.isSpectator() || !dimensions.contains(level.dimension().location().toString())) {
                continue;
            }
            AABB area = player.getBoundingBox().inflate(radius);
            if (!level.getEntitiesOfClass(Mob.class, area, mob -> mob.getTags().contains(TAG)).isEmpty()) {
                continue;
            }
            spawnInFront(player, config);
        }
    }

    public static boolean spawnInFront(ServerPlayer player, JsonObject config) {
        ServerLevel level = player.serverLevel();
        RandomSource random = player.getRandom();
        int min = JsonValues.integer(config, "distance_min", 6);
        int max = Math.max(min, JsonValues.integer(config, "distance_max", 10));
        double angle = player.getYRot() * Math.PI / 180.0D + Math.PI / 2.0D + (random.nextDouble() - 0.5D) * 0.6D;
        double radius = min + random.nextDouble() * (max - min);
        int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
        int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 1) {
            y = player.getBlockY();
        }
        return summon(level, new BlockPos(x, y, z), config, random);
    }

    private static boolean summon(ServerLevel level, BlockPos pos, JsonObject config, RandomSource random) {
        List<EntityType<?>> mobs = new ArrayList<>();
        for (String id : JsonValues.strings(config, "mobs")) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
                mobs.add(BuiltInRegistries.ENTITY_TYPE.get(key));
            }
        }
        if (mobs.isEmpty()) {
            return false;
        }
        Entity created = mobs.get(random.nextInt(mobs.size())).create(level);
        if (!(created instanceof Mob npc)) {
            if (created != null) {
                created.discard();
            }
            return false;
        }
        Mission mission = newMission(config, random);
        if (mission == null) {
            npc.discard();
            return false;
        }
        List<String> names = JsonValues.strings(config, "names");
        String name = names.isEmpty() ? "NPC" : names.get(random.nextInt(names.size()));
        JsonObject level2 = JsonValues.object(JsonValues.object(config, "levels"), mission.level);
        npc.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        npc.addTag(TAG);
        npc.getPersistentData().putString(LEVEL_KEY, mission.level);
        npc.getPersistentData().putString(ITEM_KEY, BuiltInRegistries.ITEM.getKey(mission.item).toString());
        npc.getPersistentData().putInt(COUNT_KEY, mission.count);
        npc.setNoAi(true);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setPersistenceRequired();
        npc.setCustomName(Component.translatable("entity.ultimatemodadditions.npc_mission",
                JsonValues.string(level2, "name", mission.level), name).withStyle(color(level2)));
        npc.setCustomNameVisible(true);
        return level.addFreshEntity(npc);
    }

    private static ChatFormatting color(JsonObject level) {
        ChatFormatting found = ChatFormatting.getByName(JsonValues.string(level, "color", "green"));
        return found == null || !found.isColor() ? ChatFormatting.GREEN : found;
    }

    private record Mission(String level, Item item, int count) {
    }

    private static Mission newMission(JsonObject config, RandomSource random) {
        JsonObject levels = JsonValues.object(config, "levels");
        int total = 0;
        for (Map.Entry<String, JsonElement> entry : levels.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                total += Math.max(0, JsonValues.integer(entry.getValue().getAsJsonObject(), "weight", 1));
            }
        }
        String chosen = null;
        int roll = total <= 0 ? 0 : random.nextInt(total);
        for (Map.Entry<String, JsonElement> entry : levels.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            roll -= Math.max(0, JsonValues.integer(entry.getValue().getAsJsonObject(), "weight", 1));
            chosen = entry.getKey();
            if (roll < 0) {
                break;
            }
        }
        if (chosen == null) {
            return null;
        }
        List<Object[]> options = rows(JsonValues.object(levels, chosen), "deliveries");
        if (options.isEmpty()) {
            return null;
        }
        Object[] row = options.get(random.nextInt(options.size()));
        int min = (Integer) row[1];
        int max = Math.max(min, (Integer) row[2]);
        return new Mission(chosen, (Item) row[0], min + random.nextInt(max - min + 1));
    }

    private static List<Object[]> rows(JsonObject level, String key) {
        List<Object[]> rows = new ArrayList<>();
        JsonElement element = level.get(key);
        if (element == null || !element.isJsonArray()) {
            return rows;
        }
        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonArray() || entry.getAsJsonArray().size() < 3) {
                continue;
            }
            JsonArray row = entry.getAsJsonArray();
            try {
                Item item = ItemSpec.item(row.get(0).getAsString());
                if (item != null) {
                    rows.add(new Object[]{item, row.get(1).getAsInt(), row.get(2).getAsInt()});
                }
            } catch (RuntimeException ignored) {
            }
        }
        return rows;
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!target.getTags().contains(TAG)) {
            return;
        }
        event.setCanceled(true);
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        talk(player, target);
    }

    private static void talk(ServerPlayer player, Entity npc) {
        JsonObject config = config();
        EventState state = EventState.get(player.server);
        String levelId = npc.getPersistentData().getString(LEVEL_KEY);
        Item item = ItemSpec.item(npc.getPersistentData().getString(ITEM_KEY));
        int count = npc.getPersistentData().getInt(COUNT_KEY);
        if (levelId.isEmpty() || item == null || count <= 0) {
            Mission fresh = newMission(config, player.getRandom());
            if (fresh == null) {
                return;
            }
            levelId = fresh.level;
            item = fresh.item;
            count = fresh.count;
            npc.getPersistentData().putString(LEVEL_KEY, levelId);
            npc.getPersistentData().putString(ITEM_KEY, BuiltInRegistries.ITEM.getKey(item).toString());
            npc.getPersistentData().putInt(COUNT_KEY, count);
        }
        JsonObject level = JsonValues.object(JsonValues.object(config, "levels"), levelId);
        String key = player.getUUID() + ":" + npc.getUUID();
        Component itemName = new ItemStack(item).getHoverName();
        String saved = state.progress(key);
        int progress = 0;
        if (saved == null) {
            state.setProgress(key, "0");
            player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.npc_describe",
                    JsonValues.string(level, "name", levelId), count, itemName).withStyle(color(level)));
            return;
        }
        try {
            progress = Integer.parseInt(saved);
        } catch (NumberFormatException ignored) {
        }
        int missing = count - progress;
        int taken = take(player, item, missing);
        if (taken <= 0) {
            player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.npc_missing", missing, itemName)
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        progress += taken;
        if (progress >= count) {
            state.removeProgress(key);
            reward(player, npc, level);
        } else {
            state.setProgress(key, String.valueOf(progress));
            player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.npc_partial", taken,
                    count - progress, itemName).withStyle(ChatFormatting.YELLOW));
        }
    }

    private static int take(ServerPlayer player, Item item, int wanted) {
        Inventory inventory = player.getInventory();
        int left = wanted;
        for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                int amount = Math.min(stack.getCount(), left);
                stack.shrink(amount);
                left -= amount;
            }
        }
        if (left < wanted) {
            player.inventoryMenu.broadcastFullState();
        }
        return wanted - left;
    }

    private static void reward(ServerPlayer player, Entity npc, JsonObject level) {
        RandomSource random = player.getRandom();
        List<Object[]> prizes = rows(level, "prizes");
        List<Component> given = new ArrayList<>();
        int count = JsonValues.integer(level, "prize_count", 1);
        for (int i = 0; i < count && !prizes.isEmpty(); i++) {
            Object[] prize = prizes.remove(random.nextInt(prizes.size()));
            int min = (Integer) prize[1];
            int max = Math.max(min, (Integer) prize[2]);
            ItemStack stack = new ItemStack((Item) prize[0], min + random.nextInt(max - min + 1));
            given.add(Component.literal(stack.getCount() + " x ").append(stack.getHoverName()));
            if (!player.getInventory().add(stack.copy())) {
                player.drop(stack, false);
            }
        }
        double[] xp = JsonValues.range(level, "xp", 0, 0);
        player.giveExperiencePoints((int) (xp[0] + random.nextDouble() * (xp[1] - xp[0])));
        Component list = Component.empty();
        for (int i = 0; i < given.size(); i++) {
            list = list.copy().append(i == 0 ? given.get(i) : Component.literal(", ").append(given.get(i)));
        }
        player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.npc_reward",
                JsonValues.string(level, "name", ""), list).withStyle(ChatFormatting.GREEN));
        if (npc.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, npc.getX(), npc.getY() + 1.0D, npc.getZ(), 25, 0.4D, 0.6D, 0.4D, 0.1D);
        }
        npc.discard();
    }
}
