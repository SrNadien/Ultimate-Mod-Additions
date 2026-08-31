package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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

    private UMAAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
