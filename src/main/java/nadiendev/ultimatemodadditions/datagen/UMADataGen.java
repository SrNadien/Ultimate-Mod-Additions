package nadiendev.ultimatemodadditions.datagen;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.datagen.providers.UMALangProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

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
    }
}
