package nadiendev.ultimatemodadditions.event;

import nadiendev.ultimatemodadditions.UMA;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = UMA.MODID)
public final class Scheduler {

    private record Task(long due, Runnable action) {
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static long ticks;

    private Scheduler() {
    }

    public static synchronized void after(int delayTicks, Runnable action) {
        TASKS.add(new Task(ticks + Math.max(1, delayTicks), action));
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        List<Runnable> due = new ArrayList<>();
        synchronized (Scheduler.class) {
            ticks++;
            Iterator<Task> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (task.due() <= ticks) {
                    due.add(task.action());
                    iterator.remove();
                }
            }
        }
        for (Runnable action : due) {
            try {
                action.run();
            } catch (RuntimeException exception) {
                UMA.LOGGER.error("A scheduled task failed", exception);
            }
        }
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent event) {
        synchronized (Scheduler.class) {
            TASKS.clear();
        }
    }
}
