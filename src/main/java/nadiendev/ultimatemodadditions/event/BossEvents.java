package nadiendev.ultimatemodadditions.event;

import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = UMA.MODID)
public final class BossEvents {

    public static final String ID = "boss";
    private static final String BOSS_TAG = "uma_boss";
    private static final String MINION_TAG = "uma_boss_minion";
    private static final String LEVEL_TAG = "uma_boss_x";
    private static final String ABILITY_TAG = "uma_ability_";
    private static final String REDUCTION = "uma_boss_reduction";
    private static final List<String> ABILITIES = List.of("lightning", "minions", "shockwave", "regeneration", "shield",
            "fire_rain", "wither_aura");

    private static final Map<UUID, LivingEntity> BOSSES = new HashMap<>();
    private static int checkClock;
    private static long abilityClock;

    private BossEvents() {
    }

    private static JsonObject config() {
        return EventConfigs.get(EventDefaults.BOSS);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living
                && living.getTags().contains(BOSS_TAG)) {
            BOSSES.put(living.getUUID(), living);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            BOSSES.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent event) {
        BOSSES.clear();
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        EventState state = EventState.get(server);
        runAbilities(server);
        JsonObject config = config();
        checkClock++;
        if (checkClock < Math.max(1, JsonValues.integer(config, "check_ticks", 100))) {
            return;
        }
        checkClock = 0;
        if (!state.enabled(ID)) {
            return;
        }
        long day = server.overworld().getDayTime() / 24000L;
        long last = state.timer("boss_last_day", -1_000_000L);
        if (day < JsonValues.integer(config, "first_day", 20)
                || day - last < JsonValues.integer(config, "min_days_between", 20)
                || server.overworld().getRandom().nextDouble() >= JsonValues.decimal(config, "chance", 0.001D)) {
            return;
        }
        if (summon(server, null)) {
            state.setTimer("boss_last_day", day);
        }
    }

    public static boolean summon(MinecraftServer server, ServerPlayer chosen) {
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isSpectator()) {
                candidates.add(player);
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        RandomSource random = server.overworld().getRandom();
        ServerPlayer target = chosen != null ? chosen : candidates.get(random.nextInt(candidates.size()));
        JsonObject config = config();
        List<EntityType<?>> pool = pool(config);
        if (pool.isEmpty()) {
            return false;
        }
        ServerLevel level = target.serverLevel();
        for (int attempt = 0; attempt < 12; attempt++) {
            EntityType<?> type = pool.get(random.nextInt(pool.size()));
            Entity created = type.create(level);
            if (!(created instanceof LivingEntity boss)) {
                if (created != null) {
                    created.discard();
                }
                continue;
            }
            BlockPos spot = findSpot(level, target, config, random);
            if (spot == null) {
                boss.discard();
                continue;
            }
            return spawn(server, level, boss, spot, config, random);
        }
        return false;
    }

    private static boolean spawn(MinecraftServer server, ServerLevel level, LivingEntity boss, BlockPos spot,
                                 JsonObject config, RandomSource random) {
        List<Integer> levels = new ArrayList<>();
        JsonObject root = config;
        if (root.has("levels") && root.get("levels").isJsonArray()) {
            root.getAsJsonArray("levels").forEach(entry -> levels.add(entry.getAsInt()));
        }
        if (levels.isEmpty()) {
            levels.add(5);
        }
        int multiplier = levels.get(random.nextInt(levels.size()));
        int abilityCount = JsonValues.integer(JsonValues.object(config, "abilities_per_level"), String.valueOf(multiplier), 1);
        List<String> enabled = new ArrayList<>();
        JsonObject abilities = JsonValues.object(config, "abilities");
        for (String ability : ABILITIES) {
            if (JsonValues.bool(JsonValues.object(abilities, ability), "enabled", true)) {
                enabled.add(ability);
            }
        }
        List<String> chosen = new ArrayList<>();
        while (chosen.size() < abilityCount && !enabled.isEmpty()) {
            chosen.add(enabled.remove(random.nextInt(enabled.size())));
        }
        boss.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        boss.addTag(BOSS_TAG);
        boss.addTag(LEVEL_TAG + multiplier);
        chosen.forEach(ability -> boss.addTag(ABILITY_TAG + ability));
        if (boss instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        boss.setGlowingTag(true);
        AttributeInstance health = boss.getAttribute(Attributes.MAX_HEALTH);
        double baseHealth = boss.getMaxHealth();
        double targetHealth = baseHealth * multiplier;
        double realHealth = Math.min(targetHealth, JsonValues.integer(config, "max_health_cap", 1024));
        if (health != null) {
            health.setBaseValue(realHealth);
        }
        boss.getPersistentData().putDouble(REDUCTION, targetHealth / Math.max(1.0D, realHealth));
        AttributeInstance attack = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(Math.max(2.0D, JsonValues.decimal(config, "attack_per_level", 4.0D) * multiplier));
        }
        boss.setHealth(boss.getMaxHealth());
        Component base = boss.getName();
        boss.setCustomName(Component.translatable("message.ultimatemodadditions.boss_name", multiplier, base)
                .withStyle(ChatFormatting.RED));
        boss.setCustomNameVisible(true);
        BOSSES.put(boss.getUUID(), boss);
        if (!level.addFreshEntity(boss)) {
            BOSSES.remove(boss.getUUID());
            return false;
        }
        announce(server, level, boss, base, multiplier, chosen, spot, config);
        return true;
    }

    private static void announce(MinecraftServer server, ServerLevel level, LivingEntity boss, Component base, int multiplier,
                                 List<String> abilities, BlockPos spot, JsonObject config) {
        Component dimension = Component.literal(level.dimension().location().toString());
        String where = spot.getX() + ", " + spot.getY() + ", " + spot.getZ();
        List<Component> names = new ArrayList<>();
        abilities.forEach(ability -> names.add(Component.translatable("boss.ultimatemodadditions.ability." + ability)));
        Component list = Component.empty();
        for (int i = 0; i < names.size(); i++) {
            list = list.copy().append(i == 0 ? names.get(i) : Component.literal(", ").append(names.get(i)));
        }
        server.getPlayerList().broadcastSystemMessage(Component.translatable("message.ultimatemodadditions.boss_appeared",
                multiplier, base, dimension, where, list).withStyle(ChatFormatting.RED), false);
        if (JsonValues.bool(config, "announce_title", true)) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 120, 20));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(dimension.getString() + " - " + where)
                        .withStyle(ChatFormatting.GOLD)));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(
                        "message.ultimatemodadditions.boss_title", multiplier, base).withStyle(ChatFormatting.RED)));
            }
        }
    }

    private static List<EntityType<?>> pool(JsonObject config) {
        Set<EntityType<?>> found = new LinkedHashSet<>();
        for (String tag : JsonValues.strings(config, "boss_tags")) {
            ResourceLocation id = ResourceLocation.tryParse(tag);
            if (id == null) {
                continue;
            }
            BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, id)).ifPresent((HolderSet.Named<EntityType<?>> set) -> {
                for (Holder<EntityType<?>> holder : set) {
                    found.add(holder.value());
                }
            });
        }
        for (String extra : JsonValues.strings(config, "extra_bosses")) {
            ResourceLocation id = ResourceLocation.tryParse(extra);
            if (id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                found.add(BuiltInRegistries.ENTITY_TYPE.get(id));
            }
        }
        List<String> excluded = JsonValues.strings(config, "excluded_bosses");
        List<EntityType<?>> pool = new ArrayList<>();
        for (EntityType<?> type : found) {
            if (!excluded.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())) {
                pool.add(type);
            }
        }
        return pool;
    }

    private static BlockPos findSpot(ServerLevel level, ServerPlayer target, JsonObject config, RandomSource random) {
        int min = JsonValues.integer(config, "radius_min", 120);
        int max = Math.max(min + 1, JsonValues.integer(config, "radius_max", 300));
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = min + random.nextDouble() * (max - min);
            int x = (int) Math.floor(target.getX() + Math.cos(angle) * radius);
            int z = (int) Math.floor(target.getZ() + Math.sin(angle) * radius);
            level.getChunk(x >> 4, z >> 4);
            if (level.dimensionType().hasCeiling()) {
                for (int dy = 0; dy <= 24; dy++) {
                    for (int sign = -1; sign <= 1; sign += 2) {
                        BlockPos pos = new BlockPos(x, target.getBlockY() + dy * sign, z);
                        if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                                && !level.isEmptyBlock(pos.below())) {
                            return pos;
                        }
                    }
                }
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > level.getMinBuildHeight() + 1) {
                return new BlockPos(x, y, z);
            }
        }
        return null;
    }

    private static void runAbilities(MinecraftServer server) {
        if (BOSSES.isEmpty()) {
            return;
        }
        abilityClock++;
        if (abilityClock % 20L != 0L) {
            return;
        }
        JsonObject abilities = JsonValues.object(config(), "abilities");
        for (LivingEntity boss : new ArrayList<>(BOSSES.values())) {
            if (!boss.isAlive() || !(boss.level() instanceof ServerLevel level)) {
                continue;
            }
            for (String ability : ABILITIES) {
                if (!boss.getTags().contains(ABILITY_TAG + ability)) {
                    continue;
                }
                int every = Math.max(20, JsonValues.integer(JsonValues.object(abilities, ability), "every_ticks", 100));
                if (abilityClock % every != 0L) {
                    continue;
                }
                cast(level, boss, ability);
            }
        }
    }

    private static List<ServerPlayer> playersNear(ServerLevel level, LivingEntity boss, double range) {
        List<ServerPlayer> near = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isCreative() && !player.isSpectator() && player.distanceToSqr(boss) <= range * range) {
                near.add(player);
            }
        }
        return near;
    }

    private static void cast(ServerLevel level, LivingEntity boss, String ability) {
        switch (ability) {
            case "lightning" -> {
                for (ServerPlayer player : playersNear(level, boss, 32.0D)) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) {
                        bolt.moveTo(player.getX(), player.getY(), player.getZ());
                        level.addFreshEntity(bolt);
                    }
                    break;
                }
            }
            case "minions" -> {
                minion(level, boss, EntityType.ZOMBIE, 3.0D, 0.0D);
                minion(level, boss, EntityType.SKELETON, -3.0D, 0.0D);
                minion(level, boss, EntityType.ZOMBIE, 0.0D, 3.0D);
            }
            case "shockwave" -> {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, boss.getX(), boss.getY() + 1.0D, boss.getZ(), 1, 0, 0, 0, 0);
                for (ServerPlayer player : playersNear(level, boss, 8.0D)) {
                    player.hurt(level.damageSources().explosion(boss, boss), 8.0F);
                    player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 6, true, true));
                }
            }
            case "regeneration" -> boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 3, true, false));
            case "shield" -> {
                boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 3, true, false));
                boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 9, true, false));
            }
            case "fire_rain" -> {
                for (ServerPlayer player : playersNear(level, boss, 32.0D)) {
                    SmallFireball fireball = new SmallFireball(level, boss, new Vec3(0.0D, -1.2D, 0.0D));
                    fireball.setPos(player.getX(), player.getY() + 12.0D, player.getZ());
                    level.addFreshEntity(fireball);
                    break;
                }
            }
            case "wither_aura" -> {
                for (ServerPlayer player : playersNear(level, boss, 10.0D)) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1, true, true));
                }
            }
            default -> {
            }
        }
    }

    private static void minion(ServerLevel level, LivingEntity boss, EntityType<? extends Mob> type, double dx, double dz) {
        Mob minion = type.create(level);
        if (minion == null) {
            return;
        }
        minion.moveTo(boss.getX() + dx, boss.getY(), boss.getZ() + dz, boss.getYRot(), 0.0F);
        minion.addTag(MINION_TAG);
        minion.setCustomName(Component.translatable("entity.ultimatemodadditions.boss_minion"));
        level.addFreshEntity(minion);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !entity.getTags().contains(BOSS_TAG)) {
            return;
        }
        double reduction = entity.getPersistentData().getDouble(REDUCTION);
        if (reduction > 1.0D) {
            event.setAmount((float) (event.getAmount() / reduction));
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        Set<String> tags = event.getEntity().getTags();
        if (tags.contains(BOSS_TAG) || tags.contains(MINION_TAG)) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss.level().isClientSide() || !boss.getTags().contains(BOSS_TAG) || !(boss.level() instanceof ServerLevel level)) {
            return;
        }
        BOSSES.remove(boss.getUUID());
        MinecraftServer server = level.getServer();
        server.getPlayerList().broadcastSystemMessage(Component.translatable("message.ultimatemodadditions.boss_defeated",
                boss.getDisplayName()).withStyle(ChatFormatting.GREEN), false);
        int multiplier = 5;
        for (String tag : boss.getTags()) {
            if (tag.startsWith(LEVEL_TAG)) {
                try {
                    multiplier = Integer.parseInt(tag.substring(LEVEL_TAG.length()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        JsonObject rewards = JsonValues.object(config(), "rewards");
        RewardTable table = new RewardTable(rewards, server.registryAccess(), boss.getRandom());
        List<ItemStack> drops = new ArrayList<>();
        if (!table.onlyManual()) {
            int tries = JsonValues.integer(JsonValues.object(rewards, "tries"), String.valueOf(multiplier), 1);
            for (int i = 0; i < tries; i++) {
                ItemStack prize = table.prize();
                if (!prize.isEmpty()) {
                    drops.add(prize);
                }
            }
        }
        drops.addAll(table.coins());
        drops.addAll(table.manual(multiplier));
        for (ItemStack stack : drops) {
            level.addFreshEntity(new ItemEntity(level, boss.getX(), boss.getY() + 0.5D, boss.getZ(), stack));
        }
        server.getPlayerList().broadcastSystemMessage(Component.translatable("message.ultimatemodadditions.boss_dropped",
                drops.size()).withStyle(ChatFormatting.GOLD), false);
    }
}
