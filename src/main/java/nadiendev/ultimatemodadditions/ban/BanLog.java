package nadiendev.ultimatemodadditions.ban;

import java.util.ArrayList;
import java.util.List;

public final class BanLog {

    public record Entry(long time, String player, String item, String action, String dimension) {
    }

    private static final int LIMIT = 200;
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private BanLog() {
    }

    public static synchronized void add(String player, String item, String action, String dimension) {
        ENTRIES.add(new Entry(System.currentTimeMillis(), player, item, action, dimension));
        if (ENTRIES.size() > LIMIT) {
            ENTRIES.remove(0);
        }
    }

    public static synchronized List<Entry> recent(String player, int amount) {
        List<Entry> found = new ArrayList<>();
        for (int i = ENTRIES.size() - 1; i >= 0 && found.size() < amount; i--) {
            Entry entry = ENTRIES.get(i);
            if (player == null || entry.player().equalsIgnoreCase(player)) {
                found.add(entry);
            }
        }
        return found;
    }

    public static synchronized void clear() {
        ENTRIES.clear();
    }
}
