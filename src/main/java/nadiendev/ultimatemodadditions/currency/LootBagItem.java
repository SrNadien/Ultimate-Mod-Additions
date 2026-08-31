package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.network.SyncedData;
import nadiendev.ultimatemodadditions.data.LootBagDefinition;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.registry.UMADataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LootBagItem extends Item {

    private static final Map<BagTier, LootBagItem> BY_TIER = new EnumMap<>(BagTier.class);

    private final BagTier tier;

    public LootBagItem(Properties properties, BagTier tier) {
        super(properties);
        this.tier = tier;
        BY_TIER.put(tier, this);
    }

    public BagTier bagTier() {
        return tier;
    }

    public String tier() {
        return tier.key();
    }

    public static BagTier tierOf(ResourceLocation bagId) {
        LootBagDefinition definition = SyncedData.bag(bagId);
        if (definition == null) {
            definition = UMAData.lootBags().get(bagId);
        }
        return definition != null ? BagTier.byKey(definition.tier()) : BagTier.COMMON;
    }

    public static ItemStack create(ResourceLocation bagId, int count) {
        LootBagItem item = BY_TIER.get(tierOf(bagId));
        if (item == null) {
            item = BY_TIER.get(BagTier.COMMON);
        }
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        stack.set(UMADataComponents.BAG_ID.get(), bagId);
        return stack;
    }

    public ResourceLocation bagId(ItemStack stack) {
        ResourceLocation id = stack.get(UMADataComponents.BAG_ID.get());
        return id != null ? id : UMA.id(tier.key());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        int toOpen = player.isShiftKeyDown() ? stack.getCount() : 1;
        int opened = 0;
        for (int i = 0; i < toOpen; i++) {
            if (!LootBagOpener.open(serverPlayer, bagId(stack))) {
                break;
            }
            opened++;
        }
        if (opened == 0) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.ultimatemodadditions.unknown_bag", bagId(stack).toString())
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        stack.shrink(opened);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        LootBagDefinition definition = SyncedData.bag(bagId(stack));
        if (definition != null && definition.name().isPresent()) {
            return Component.literal(definition.name().get());
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.bag_tier",
                Component.translatable("bag_tier.ultimatemodadditions." + tier.key())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.bag_open").withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced()) {
            tooltip.add(Component.literal(bagId(stack).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
