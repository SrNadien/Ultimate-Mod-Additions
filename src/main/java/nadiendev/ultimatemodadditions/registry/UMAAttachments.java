package nadiendev.ultimatemodadditions.registry;

import com.mojang.serialization.Codec;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.daily.DailyState;
import nadiendev.ultimatemodadditions.jobs.JobProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UMAAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UMA.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> SCALED =
            ATTACHMENTS.register("scaled", () -> AttachmentType.<Boolean>builder(() -> Boolean.FALSE)
                    .serialize(com.mojang.serialization.Codec.BOOL)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> APPLIED_DIFFICULTY =
            ATTACHMENTS.register("applied_difficulty", () -> AttachmentType.<String>builder(() -> "")
                    .serialize(com.mojang.serialization.Codec.STRING)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<net.minecraft.core.GlobalPos>> RETURN_POS =
            ATTACHMENTS.register("return_pos", () -> AttachmentType.builder(
                            () -> net.minecraft.core.GlobalPos.of(net.minecraft.world.level.Level.OVERWORLD,
                                    net.minecraft.core.BlockPos.ZERO))
                    .serialize(net.minecraft.core.GlobalPos.CODEC)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<ResourceLocation, JobProgress>>> JOBS =
            ATTACHMENTS.register("jobs", () -> AttachmentType.<Map<ResourceLocation, JobProgress>>builder(() -> new HashMap<>())
                    .serialize(Codec.unboundedMap(ResourceLocation.CODEC, JobProgress.CODEC)
                            .xmap(map -> (Map<ResourceLocation, JobProgress>) new HashMap<>(map), map -> map))
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<ItemStack>>> DELIVERIES =
            ATTACHMENTS.register("deliveries", () -> AttachmentType.<List<ItemStack>>builder(() -> new ArrayList<>())
                    .serialize(ItemStack.CODEC.listOf().xmap(list -> (List<ItemStack>) new ArrayList<>(list), list -> list))
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Set<String>>> EXPLORED =
            ATTACHMENTS.register("explored", () -> AttachmentType.<Set<String>>builder(() -> new HashSet<>())
                    .serialize(Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), set -> new ArrayList<>(set)))
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DailyState>> DAILY =
            ATTACHMENTS.register("daily", () -> AttachmentType.builder(DailyState::fresh)
                    .serialize(DailyState.CODEC)
                    .copyOnDeath()
                    .build());

    private UMAAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
