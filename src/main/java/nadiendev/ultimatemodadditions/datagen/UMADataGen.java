package nadiendev.ultimatemodadditions.datagen;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.datagen.providers.UMABlockLootProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMABlockStateProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMABlockTagsProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMADailyProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMAFontProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMAItemModelProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMAItemTagsProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMAJobsProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMALangProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMALootModifierProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMARecipeProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMAShopProvider;
import nadiendev.ultimatemodadditions.datagen.providers.UMAWorldgen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = UMA.MODID)
public final class UMADataGen {

    /**
     * es_es carries the Spanish text; es_mx and es_ar are emitted from the same source so the
     * Latin American locales are covered too, because Minecraft does not fall back between them.
     */
    public static final List<String> LOCALES = List.of("en_us", "es_es", "es_mx", "es_ar");

    private UMADataGen() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        for (String locale : LOCALES) {
            generator.addProvider(event.includeClient(), new UMALangProvider(output, locale));
        }
        generator.addProvider(event.includeClient(), new UMAFontProvider(output));
        generator.addProvider(event.includeClient(), new UMAItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new UMABlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new UMARecipeProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(UMABlockLootProvider::new, LootContextParamSets.BLOCK)),
                event.getLookupProvider()));
        UMABlockTagsProvider blockTags = new UMABlockTagsProvider(output, event.getLookupProvider(),
                event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new UMAItemTagsProvider(output, event.getLookupProvider(),
                blockTags.contentsGetter(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new UMALootModifierProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new UMAShopProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new UMAJobsProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new UMADailyProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output,
                event.getLookupProvider(), UMAWorldgen.BUILDER, Set.of(UMA.MODID)));
    }
}
