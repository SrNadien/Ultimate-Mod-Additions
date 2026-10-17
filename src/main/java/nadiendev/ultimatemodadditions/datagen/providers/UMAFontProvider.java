package nadiendev.ultimatemodadditions.datagen.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class UMAFontProvider implements DataProvider {

    private static final String[] ROWS = {
            "ABCDEFGHIJKLMNOP",
            "QRSTUVWXYZ012345",
            "6789:.,/+-%()!?\u0000",
            "\u00C1\u00C9\u00CD\u00D3\u00DA\u00D1\u00DC\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
    };

    private final PackOutput.PathProvider paths;

    public UMAFontProvider(PackOutput output) {
        this.paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "font");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject space = new JsonObject();
        space.addProperty("type", "space");
        JsonObject advances = new JsonObject();
        advances.addProperty(" ", 4);
        space.add("advances", advances);

        JsonObject bitmap = new JsonObject();
        bitmap.addProperty("type", "bitmap");
        bitmap.addProperty("file", UMA.MODID + ":font/pixel.png");
        bitmap.addProperty("height", 10);
        bitmap.addProperty("ascent", 9);
        JsonArray chars = new JsonArray();
        for (String row : ROWS) {
            chars.add(row);
        }
        bitmap.add("chars", chars);

        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "reference");
        fallback.addProperty("id", "minecraft:default");

        JsonArray providers = new JsonArray();
        providers.add(space);
        providers.add(bitmap);
        providers.add(fallback);
        JsonObject root = new JsonObject();
        root.add("providers", providers);
        return DataProvider.saveStable(cache, root, paths.json(UMA.id("pixel")));
    }

    @Override
    public String getName() {
        return "Pixel font";
    }
}
