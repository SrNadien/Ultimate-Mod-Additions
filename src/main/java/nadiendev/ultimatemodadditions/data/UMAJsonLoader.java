package nadiendev.ultimatemodadditions.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UMAJsonLoader<T> extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    private final Codec<T> codec;
    private final Map<ResourceLocation, T> values = new LinkedHashMap<>();
    private final Supplier<HolderLookup.Provider> registries;
    private final Consumer<Map<ResourceLocation, T>> afterLoad;

    public UMAJsonLoader(String directory, Codec<T> codec, Supplier<HolderLookup.Provider> registries,
                         Consumer<Map<ResourceLocation, T>> afterLoad) {
        super(GSON, directory);
        this.codec = codec;
        this.registries = registries;
        this.afterLoad = afterLoad;
    }

    public Map<ResourceLocation, T> values() {
        return values;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        values.clear();
        HolderLookup.Provider provider = registries.get();
        DynamicOps<JsonElement> ops = provider == null ? JsonOps.INSTANCE : RegistryOps.create(JsonOps.INSTANCE, provider);
        objects.forEach((id, json) -> codec.parse(ops, json)
                .resultOrPartial(error -> UMA.LOGGER.error("Skipping {} : {}", id, error))
                .ifPresent(value -> values.put(id, value)));
        afterLoad.accept(values);
    }
}
