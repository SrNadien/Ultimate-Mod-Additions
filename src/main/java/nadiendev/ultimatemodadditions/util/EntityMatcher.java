package nadiendev.ultimatemodadditions.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public record EntityMatcher(List<ResourceLocation> entities,
                            List<ResourceLocation> tags,
                            List<String> regex,
                            List<ResourceLocation> exclude,
                            List<String> categories,
                            Optional<Boolean> boss,
                            Optional<Boolean> baby) {

    public static final EntityMatcher ANY = new EntityMatcher(List.of(), List.of(), List.of(), List.of(), List.of(),
            Optional.empty(), Optional.empty());

    public static final Codec<EntityMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("entities", List.of()).forGetter(EntityMatcher::entities),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(EntityMatcher::tags),
            Codec.STRING.listOf().optionalFieldOf("regex", List.of()).forGetter(EntityMatcher::regex),
            ResourceLocation.CODEC.listOf().optionalFieldOf("exclude", List.of()).forGetter(EntityMatcher::exclude),
            Codec.STRING.listOf().optionalFieldOf("categories", List.of()).forGetter(EntityMatcher::categories),
            Codec.BOOL.optionalFieldOf("boss").forGetter(EntityMatcher::boss),
            Codec.BOOL.optionalFieldOf("baby").forGetter(EntityMatcher::baby)
    ).apply(instance, EntityMatcher::new));

    public boolean matches(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (exclude.contains(typeId)) {
            return false;
        }
        if (boss.isPresent() && boss.get() != isBoss(entity)) {
            return false;
        }
        if (baby.isPresent() && baby.get() != entity.isBaby()) {
            return false;
        }
        if (!categories.isEmpty()) {
            String category = type.getCategory().getName().toLowerCase(Locale.ROOT);
            boolean hit = categories.stream().anyMatch(c -> c.toLowerCase(Locale.ROOT).equals(category));
            if (!hit) {
                return false;
            }
        }

        if (isAnyOfWildcard()) {
            return true;
        }
        if (entities.contains(typeId)) {
            return true;
        }
        for (ResourceLocation tag : tags) {
            if (type.is(TagKey.create(Registries.ENTITY_TYPE, tag))) {
                return true;
            }
        }
        String asString = typeId.toString();
        for (String pattern : regex) {
            try {
                if (Pattern.compile(pattern).matcher(asString).find()) {
                    return true;
                }
            } catch (PatternSyntaxException ignored) {
            }
        }
        return false;
    }

    private boolean isAnyOfWildcard() {
        return entities.isEmpty() && tags.isEmpty() && regex.isEmpty();
    }

    private static boolean isBoss(Entity entity) {
        return entity.getType().is(Tags.EntityTypes.BOSSES);
    }
}
