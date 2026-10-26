package nadiendev.ultimatemodadditions.ban;

import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = UMA.MODID)
public final class BanEvents {

    private static final Map<UUID, Long> LAST_NOTICE = new HashMap<>();
    private static final Set<UUID> WATCHERS = new HashSet<>();

    private BanEvents() {
    }

    public static boolean toggleWatch(UUID player) {
        if (WATCHERS.remove(player)) {
            return false;
        }
        WATCHERS.add(player);
        return true;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BanSync.send(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_NOTICE.remove(id);
        WATCHERS.remove(id);
        BanService.forget(id);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        BanService.invalidate();
        if (event.getPlayer() != null) {
            BanSync.send(event.getPlayer());
        } else {
            BanSync.sendAll(event.getPlayerList().getServer());
        }
    }

    private static boolean blocked(ServerPlayer player, ItemStack stack, BanAction action) {
        return blocked(player, stack, action, ItemStack.EMPTY, null);
    }

    private static boolean blocked(ServerPlayer player, ItemStack stack, BanAction action, ItemStack other, String entity) {
        BanService.Denial denial = BanService.deny(new BanService.Check(player, stack, action, other, entity));
        if (denial == null) {
            return false;
        }
        notice(player, stack, denial, action);
        return true;
    }

    private static ItemStack blockStack(BlockState state) {
        return new ItemStack(state.getBlock());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 40 == 0 && BanService.consumeChange(server)) {
            BanApply.apply(server);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !BanService.enabled()) {
            return;
        }
        int interval = Math.max(1, JsonValues.integer(BanService.config(), "scan_interval_ticks", 10));
        if (player.tickCount % interval != 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        boolean changed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            boolean armor = slot >= Inventory.INVENTORY_SIZE && slot < Inventory.INVENTORY_SIZE + 4;
            BanAction reason = null;
            BanService.Denial denial = BanService.deny(new BanService.Check(player, stack, BanAction.PICKUP));
            if (denial != null) {
                reason = BanAction.PICKUP;
            } else if (armor) {
                denial = BanService.deny(new BanService.Check(player, stack, BanAction.EQUIP));
                if (denial != null) {
                    reason = BanAction.EQUIP;
                }
            }
            if (denial == null) {
                continue;
            }
            notice(player, stack, denial, reason);
            inventory.setItem(slot, ItemStack.EMPTY);
            changed = true;
        }
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            BanService.Denial denial = BanService.deny(new BanService.Check(player, carried, BanAction.PICKUP));
            if (denial != null) {
                notice(player, carried, denial, BanAction.PICKUP);
                player.containerMenu.setCarried(ItemStack.EMPTY);
                changed = true;
            }
        }
        changed |= holding(player);
        if (changed) {
            player.inventoryMenu.broadcastFullState();
        }
    }

    private static boolean holding(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        boolean changed = false;
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && blocked(player, main, BanAction.HOLD)) {
            for (int offset = 1; offset < 9; offset++) {
                int candidate = (inventory.selected + offset) % 9;
                ItemStack other = inventory.getItem(candidate);
                if (other.isEmpty() || BanService.deny(new BanService.Check(player, other, BanAction.HOLD)) == null) {
                    inventory.selected = candidate;
                    player.connection.send(new ClientboundSetCarriedItemPacket(candidate));
                    changed = true;
                    break;
                }
            }
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && blocked(player, off, BanAction.HOLD)) {
            ItemStack moved = off.copy();
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            if (!inventory.add(moved)) {
                player.drop(moved, false);
            }
            changed = true;
        }
        return changed;
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack result = event.getCrafting();
        if (blocked(player, result, BanAction.CRAFT)) {
            Set<Item> items = Set.of(result.getItem());
            result.setCount(0);
            player.server.execute(() -> {
                scrub(player, items);
                player.inventoryMenu.broadcastFullState();
            });
        }
    }

    @SubscribeEvent
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack result = event.getSmelting();
        if (blocked(player, result, BanAction.SMELT)) {
            Set<Item> items = Set.of(result.getItem());
            player.server.execute(() -> {
                scrub(player, items);
                player.inventoryMenu.broadcastFullState();
            });
        }
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && BanService.deny(new BanService.Check(player, event.getItemEntity().getItem(), BanAction.PICKUP)) != null) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getEntity().getItem();
        if (blocked(player, stack, BanAction.DROP)) {
            event.setCanceled(true);
            if (!player.getInventory().add(stack.copy())) {
                player.drop(stack.copy(), false, false);
            }
        }
    }

    @SubscribeEvent
    public static void onEquip(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSlot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return;
        }
        ItemStack to = event.getTo();
        if (!to.isEmpty() && blocked(player, to, BanAction.EQUIP)) {
            player.setItemSlot(event.getSlot(), ItemStack.EMPTY);
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && blocked(player, blockStack(event.getPlacedBlock()), BanAction.PLACE, player.getMainHandItem(), null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && blocked(player, blockStack(event.getState()), BanAction.BREAK, player.getMainHandItem(), null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && blocked(player, event.getItemStack(), BanAction.USE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState state = event.getLevel().getBlockState(event.getPos());
        ItemStack held = event.getItemStack();
        ItemStack clicked = blockStack(state);
        if (!held.isEmpty() && blocked(player, held, BanAction.USE, clicked, null)
                || !clicked.isEmpty() && blocked(player, clicked, BanAction.INTERACT, held, null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (!held.isEmpty() && blocked(player, held, BanAction.CLICK, blockStack(event.getLevel().getBlockState(event.getPos())), null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.getItemStack().isEmpty()
                && blocked(player, event.getItemStack(), BanAction.ENTITY_INTERACT, ItemStack.EMPTY, entityId(event.getTarget()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onArmorStand(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof ArmorStand stand)) {
            return;
        }
        ItemStack held = event.getItemStack();
        boolean deny = !held.isEmpty() && blocked(player, held, BanAction.ARMORSTAND);
        if (!deny && held.isEmpty()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack worn = stand.getItemBySlot(slot);
                if (!worn.isEmpty() && blocked(player, worn, BanAction.ARMORSTAND)) {
                    deny = true;
                    break;
                }
            }
        }
        if (deny) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.getMainHandItem().isEmpty()
                && blocked(player, player.getMainHandItem(), BanAction.ATTACK, ItemStack.EMPTY, entityId(event.getTarget()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getItem();
        UseAnim animation = stack.getUseAnimation();
        BanAction action = animation == UseAnim.EAT || animation == UseAnim.DRINK ? BanAction.CONSUME : BanAction.USE;
        if (blocked(player, stack, action)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSwap(LivingSwapItemsEvent.Hands event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (blocked(player, event.getItemSwappedToMainHand(), BanAction.SWAP)
                || blocked(player, event.getItemSwappedToOffHand(), BanAction.SWAP)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        String name = event.getName();
        if (name == null || name.isEmpty() || left.isEmpty() || name.equals(left.getHoverName().getString())) {
            return;
        }
        if (event.getPlayer() instanceof ServerPlayer player && blocked(player, left, BanAction.RENAME)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = event.getContainer();
        boolean changed = false;
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && blocked(player, stack, BanAction.DELETE)) {
                slot.set(ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            menu.broadcastChanges();
        }
    }

    @SubscribeEvent
    public static void onEntityDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !BanService.enabled()) {
            return;
        }
        String entity = entityId(event.getEntity());
        String dimension = level.dimension().location().toString();
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            if (BanService.deniesDrop(level.getServer(), dimension, iterator.next().getItem(), entity)) {
                iterator.remove();
            }
        }
    }

    private static String entityId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static void scrub(ServerPlayer player, Set<Item> items) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (items.contains(inventory.getItem(slot).getItem())) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        if (items.contains(player.containerMenu.getCarried().getItem())) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
    }

    public static void scrubEveryone(MinecraftServer server, BanRule rule) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inventory = player.getInventory();
            boolean changed = false;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty() && rule.target().matches(stack)) {
                    BanService.Denial denial = BanService.deny(new BanService.Check(player, stack, BanAction.PICKUP));
                    if (denial != null) {
                        notice(player, stack, denial, BanAction.PICKUP);
                        inventory.setItem(slot, ItemStack.EMPTY);
                        changed = true;
                    }
                }
            }
            if (changed) {
                player.inventoryMenu.broadcastFullState();
            }
        }
    }

    public static void notice(ServerPlayer player, ItemStack stack, BanService.Denial denial, BanAction action) {
        BanRule rule = denial.rule();
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String dimension = player.level().dimension().location().toString();
        BanLog.add(player.getGameProfile().getName(), key.toString(), action.id(), dimension);
        JsonObject config = BanService.config();
        long now = player.server.overworld().getGameTime();
        long cooldown = Math.max(0, JsonValues.integer(config, "notify_cooldown_ticks", 40));
        Long last = LAST_NOTICE.get(player.getUUID());
        boolean quiet = last != null && now - last < cooldown;
        if (!quiet) {
            LAST_NOTICE.put(player.getUUID(), now);
            send(player, stack, denial, action, key, dimension);
            feedback(player, JsonValues.object(config, "feedback"));
        }
        for (String command : rule.data().run()) {
            run(player, command, key, dimension);
        }
        if (rule.data().log() || JsonValues.bool(config, "log_all", false)) {
            watchers(player, key, action, dimension);
        }
    }

    private static String fill(String text, ServerPlayer player, ResourceLocation id, String itemName, String dimension, long remaining) {
        return text.replace("{player}", player.getGameProfile().getName()).replace("{item}", itemName).replace("{id}", id.toString())
                .replace("{dimension}", dimension).replace("{world}", dimension)
                .replace("{time}", Math.max(1L, (remaining + 999L) / 1000L) + "s");
    }

    private static void send(ServerPlayer player, ItemStack stack, BanService.Denial denial, BanAction action,
                             ResourceLocation key, String dimension) {
        BanRule rule = denial.rule();
        MutableComponent tag = Component.translatable(rule.disabled()
                ? "message.ultimatemodadditions.ban_tag_disabled" : "message.ultimatemodadditions.ban_tag_banned")
                .withStyle(rule.disabled() ? ChatFormatting.RED : ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        player.sendSystemMessage(tag.append(" ").append(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        if (denial.messages().isEmpty()) {
            player.sendSystemMessage(line(Component.translatable("message.ultimatemodadditions.ban_reason_" + action.id())
                    .withStyle(ChatFormatting.GRAY)));
        } else {
            for (String text : denial.messages()) {
                player.sendSystemMessage(line(Legacy.parse(fill(text, player, key, stack.getHoverName().getString(), dimension,
                        denial.remaining()))));
            }
        }
        player.sendSystemMessage(line(Component.translatable("message.ultimatemodadditions.ban_ask_staff")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
    }

    private static MutableComponent line(Component content) {
        return Component.literal("  \u00bb ").withStyle(ChatFormatting.DARK_GRAY).append(content);
    }

    private static void feedback(ServerPlayer player, JsonObject feedback) {
        String sound = JsonValues.string(feedback, "sound", "minecraft:entity.villager.no");
        ResourceLocation soundId = ResourceLocation.tryParse(sound);
        if (soundId != null && !sound.isEmpty()) {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getOptional(soundId).orElse(null);
            if (event != null) {
                player.playNotifySound(event, SoundSource.PLAYERS, (float) JsonValues.decimal(feedback, "volume", 1.0D),
                        (float) JsonValues.decimal(feedback, "pitch", 1.0D));
            }
        }
        ResourceLocation particleId = ResourceLocation.tryParse(JsonValues.string(feedback, "particle", ""));
        int count = JsonValues.integer(feedback, "particle_count", 0);
        if (particleId != null && count > 0 && player.level() instanceof ServerLevel level
                && BuiltInRegistries.PARTICLE_TYPE.getOptional(particleId).orElse(null) instanceof ParticleOptions particle) {
            level.sendParticles(particle, player.getX(), player.getY() + 1.0D, player.getZ(), count, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    private static void run(ServerPlayer player, String command, ResourceLocation item, String dimension) {
        MinecraftServer server = player.server;
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
                fill(command, player, item, item.toString(), dimension, 0L));
    }

    private static void watchers(ServerPlayer player, ResourceLocation item, BanAction action, String dimension) {
        if (WATCHERS.isEmpty()) {
            return;
        }
        Component line = Component.translatable("message.ultimatemodadditions.ban_watch", player.getDisplayName(),
                item.toString(), action.id(), dimension).withStyle(ChatFormatting.DARK_GRAY);
        for (UUID id : List.copyOf(WATCHERS)) {
            ServerPlayer watcher = player.server.getPlayerList().getPlayer(id);
            if (watcher != null) {
                watcher.sendSystemMessage(line);
            }
        }
    }
}
