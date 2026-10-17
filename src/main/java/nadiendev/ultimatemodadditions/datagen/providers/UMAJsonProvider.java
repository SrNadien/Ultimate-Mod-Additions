package nadiendev.ultimatemodadditions.datagen.providers;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

abstract class UMAJsonProvider implements DataProvider {

    interface Emitter {
        <T> void accept(String name, Codec<T> codec, T value);
    }

    private final PackOutput.PathProvider paths;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final String label;

    UMAJsonProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String directory,
                    String label) {
        this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, directory);
        this.registries = registries;
        this.label = label;
    }

    abstract void generate(Emitter emitter);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(provider -> {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
            List<CompletableFuture<?>> futures = new ArrayList<>();
            generate(new Emitter() {
                @Override
                public <T> void accept(String name, Codec<T> codec, T value) {
                    JsonElement json = codec.encodeStart(ops, value).getOrThrow(IllegalStateException::new);
                    futures.add(DataProvider.saveStable(cache, json, paths.json(UMA.id(name))));
                }
            });
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return label;
    }
}
