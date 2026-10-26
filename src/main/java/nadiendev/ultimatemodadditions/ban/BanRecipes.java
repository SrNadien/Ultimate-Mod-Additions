package nadiendev.ultimatemodadditions.ban;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = UMA.MODID)
public final class BanRecipes {

    private static List<RecipeHolder<?>> original;

    private BanRecipes() {
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        RecipeManager manager = event.getServerResources().getRecipeManager();
        RegistryAccess access = event.getRegistryAccess();
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resources, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void prepared, ResourceManager resources, ProfilerFiller profiler) {
                synchronized (BanRecipes.class) {
                    original = new ArrayList<>(manager.getRecipes());
                }
                MinecraftServer running = ServerLifecycleHooks.getCurrentServer();
                if (running != null) {
                    strip(running, manager, access);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        synchronized (BanRecipes.class) {
            if (original == null) {
                original = new ArrayList<>(server.getRecipeManager().getRecipes());
            }
        }
        strip(server, server.getRecipeManager(), server.registryAccess());
        BanService.markApplied(server);
    }

    public static void recompute(MinecraftServer server) {
        synchronized (BanRecipes.class) {
            if (original == null) {
                original = new ArrayList<>(server.getRecipeManager().getRecipes());
            }
        }
        strip(server, server.getRecipeManager(), server.registryAccess());
    }

    private static synchronized void strip(MinecraftServer server, RecipeManager manager, RegistryAccess access) {
        List<RecipeHolder<?>> source = original == null ? new ArrayList<>(manager.getRecipes()) : original;
        List<BanRule> rules = new ArrayList<>();
        if (BanService.enabled() && JsonValues.bool(BanService.config(), "remove_recipes", true)) {
            for (BanRule rule : BanService.rules(server)) {
                if (rule.everywhere() && rule.data().gamemodes().isEmpty()) {
                    rules.add(rule);
                }
            }
        }
        List<RecipeHolder<?>> kept = new ArrayList<>(source.size());
        int removed = 0;
        for (RecipeHolder<?> holder : source) {
            ItemStack result;
            try {
                result = holder.value().getResultItem(access);
            } catch (RuntimeException exception) {
                kept.add(holder);
                continue;
            }
            BanAction action = actionOf(holder.value().getType());
            boolean banned = false;
            if (!result.isEmpty()) {
                for (BanRule rule : rules) {
                    if (rule.actions().contains(action) && rule.target().matches(result)) {
                        banned = true;
                        break;
                    }
                }
            }
            if (banned) {
                removed++;
            } else {
                kept.add(holder);
            }
        }
        manager.replaceRecipes(kept);
        if (removed > 0) {
            UMA.LOGGER.info("Removed {} recipes of banned items", removed);
        }
    }

    private static BanAction actionOf(RecipeType<?> type) {
        if (type == RecipeType.SMELTING || type == RecipeType.BLASTING || type == RecipeType.SMOKING
                || type == RecipeType.CAMPFIRE_COOKING) {
            return BanAction.SMELT;
        }
        if (type == RecipeType.SMITHING) {
            return BanAction.SMITH;
        }
        return BanAction.CRAFT;
    }
}
