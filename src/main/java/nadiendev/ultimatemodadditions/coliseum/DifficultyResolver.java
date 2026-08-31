package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.data.DifficultyProfile;
import nadiendev.ultimatemodadditions.data.MobRule;
import nadiendev.ultimatemodadditions.data.UMAData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public final class DifficultyResolver {

    public record Resolved(@Nullable MobRule rule, DifficultyProfile profile, ResourceLocation profileId) {
    }

    private DifficultyResolver() {
    }

    @Nullable
    public static MobRule findRule(LivingEntity entity) {
        if (entity instanceof Player) {
            return null;
        }
        ResourceLocation dimension = entity.level().dimension().location();
        for (MobRule rule : UMAData.mobRules()) {
            if (rule.appliesToDimension(dimension) && rule.match().matches(entity)) {
                return rule;
            }
        }
        return null;
    }

    public static Resolved resolve(LivingEntity entity) {
        MobRule rule = findRule(entity);
        ResourceLocation profileId = activeDifficulty(entity);
        if (rule != null) {
            if (rule.overrides().isPresent()) {
                return new Resolved(rule, rule.overrides().get(), profileId);
            }
            if (rule.difficulty().isPresent()) {
                profileId = rule.difficulty().get();
            }
        }
        return new Resolved(rule, UMAData.difficulty(profileId), profileId);
    }

    public static ResourceLocation activeDifficulty(LivingEntity entity) {
        if (entity.level().getServer() != null) {
            return ColiseumState.get(entity.level().getServer()).difficulty();
        }
        return nadiendev.ultimatemodadditions.UMA.parse(
                nadiendev.ultimatemodadditions.config.UMAConfig.COMMON.defaultDifficulty.get());
    }
}
