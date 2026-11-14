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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = UMA.MODID)
public final class BanRecipes {

    private static final int POLL_TICKS = 10;

    private static List<RecipeHolder<?>> original;
    private static List<ItemStack> results;
    private static boolean stripped;
    private static volatile boolean pending;

    private BanRecipes() {
    }

    private static void capture(RecipeManager manager, RegistryAccess access) {
        List<RecipeHolder<?>> holders = new ArrayList<>(manager.getRecipes());
        List<ItemStack> outputs = new ArrayList<>(holders.size());
        for (RecipeHolder<?> holder : holders) {
            try {
                outputs.add(holder.value().getResultItem(access));
            } catch (RuntimeException exception) {
                outputs.add(ItemStack.EMPTY);
            }
        }
        original = holders;
        results = outputs;
        stripped = false;
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
                    capture(manager, access);
                }
                MinecraftServer running = ServerLifecycleHooks.getCurrentServer();
                if (running != null) {
                    strip(running, manager);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        synchronized (BanRecipes.class) {
            if (original == null) {
                capture(server.getRecipeManager(), server.registryAccess());
            }
        }
        strip(server, server.getRecipeManager());
        BanService.markApplied(server);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (pending && server.getTickCount() % POLL_TICKS == 0) {
            pending = false;
            recompute(server);
        }
    }

    public static void request() {
        pending = true;
    }

    public static void recompute(MinecraftServer server) {
        synchronized (BanRecipes.class) {
            if (original == null) {
                capture(server.getRecipeManager(), server.registryAccess());
            }
        }
        strip(server, server.getRecipeManager());
    }

    private static synchronized void strip(MinecraftServer server, RecipeManager manager) {
        List<BanRule> rules = new ArrayList<>();
        if (BanService.enabled() && JsonValues.bool(BanService.config(), "remove_recipes", true)) {
            for (BanRule rule : BanService.rules(server)) {
                if (rule.everywhere() && rule.data().gamemodes().isEmpty()) {
                    rules.add(rule);
                }
            }
        }
        if (rules.isEmpty() && !stripped) {
            return;
        }
        List<RecipeHolder<?>> kept = new ArrayList<>(original.size());
        int removed = 0;
        for (int i = 0; i < original.size(); i++) {
            RecipeHolder<?> holder = original.get(i);
            ItemStack result = results.get(i);
            boolean banned = false;
            if (!result.isEmpty() && !rules.isEmpty()) {
                BanAction action = actionOf(holder.value().getType());
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
        stripped = removed > 0;
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
