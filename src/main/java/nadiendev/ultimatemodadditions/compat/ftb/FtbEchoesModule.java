package nadiendev.ultimatemodadditions.compat.ftb;

import dev.ftb.mods.ftblibrary.integration.currency.CurrencyHelper;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.compat.CompatModule;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.Optional;

public class FtbEchoesModule implements CompatModule {

    private static final String PACK_ID = UMA.MODID + "_echoes";

    @Override
    public void onConstruct(IEventBus modBus) {
        modBus.addListener(FMLLoadCompleteEvent.class, event -> event.enqueueWork(FtbEchoesModule::register));
        modBus.addListener(AddPackFindersEvent.class, FtbEchoesModule::addPack);
    }

    private static void register() {
        if (UMAConfig.COMMON.shopEchoesCurrency.get()) {
            CurrencyHelper.getInstance().setActiveImpl(new MangoCurrencyProvider());
        }
        if (UMAConfig.COMMON.echoesPack.get()) {
            StageHelper stages = StageHelper.getInstance();
            stages.setProviderImpl(new OpenStageProvider(stages.getProvider()));
        }
    }

    private static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA || !UMAConfig.COMMON.echoesPack.get()) {
            return;
        }
        PackLocationInfo location = new PackLocationInfo(PACK_ID, Component.literal("Ultimate Mod Additions echoes"),
                PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo info) {
                return new EchoPack(info);
            }

            @Override
            public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new EchoPack(info);
            }
        };
        Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.SERVER_DATA,
                new PackSelectionConfig(true, Pack.Position.TOP, false));
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }
}
