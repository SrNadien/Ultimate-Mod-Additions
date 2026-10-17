package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMAVillagers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = UMA.MODID)
public final class FlasheroEvents {

    private static final int NAME_CHECK_INTERVAL = 40;

    private static final String TRADES_TAG = "uma_flashero_trades";

    private FlasheroEvents() {
    }

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent event) {
        if (event.getType() != UMAVillagers.FLASHERO.get()) {
            return;
        }
        event.getTrades().computeIfAbsent(1, level -> new ArrayList<>()).addAll(FlasheroTrades.listings());
    }

    @SubscribeEvent
    public static void onVillagerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager) || villager.level().isClientSide
                || villager.tickCount % NAME_CHECK_INTERVAL != 0) {
            return;
        }
        if (!UMAConfig.COMMON.flasheroEnabled.get()
                || villager.getVillagerData().getProfession() != UMAVillagers.FLASHERO.get()) {
            return;
        }
        AttributeInstance size = villager.getAttribute(Attributes.SCALE);
        double scale = UMAConfig.COMMON.flasheroScale.get();
        if (size != null && Math.abs(size.getBaseValue() - scale) > 1.0E-4D) {
            size.setBaseValue(scale);
        }
        syncOffers(villager);
        Component name = Component.literal(UMAConfig.COMMON.flasheroName.get());
        if (!name.equals(villager.getCustomName())) {
            villager.setCustomName(name);
            villager.setCustomNameVisible(true);
        }
    }

    private static void syncOffers(Villager villager) {
        String signature = UMAConfig.COMMON.flasheroTrades.get().hashCode() + ":" + UMAConfig.COMMON.flasheroMaxUses.get()
                + ":" + UMAConfig.COMMON.flasheroXp.get();
        CompoundTag data = villager.getPersistentData();
        if (signature.equals(data.getString(TRADES_TAG))) {
            return;
        }
        MerchantOffers offers = new MerchantOffers();
        for (VillagerTrades.ItemListing listing : FlasheroTrades.listings()) {
            MerchantOffer offer = listing.getOffer(villager, villager.getRandom());
            if (offer != null) {
                offers.add(offer);
            }
        }
        villager.setOffers(offers);
        data.putString(TRADES_TAG, signature);
    }
}
