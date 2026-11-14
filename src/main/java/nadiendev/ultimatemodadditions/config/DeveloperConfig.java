package nadiendev.ultimatemodadditions.config;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public final class DeveloperConfig {

    private static final String FILE = "developer.properties";
    private static final String DEFAULT = """
            # ------- ULTIMATE MOD ADDITIONS DEVELOPER CONFIG -------
            # Lets authorised players edit the server config from the game with /uma config.
            # If properties are missing, delete this file and it is created again.

            # [Developer Mode]
            # Enables /uma config set and /uma config reset. Only turn it on while you are
            # setting up your server, never on a public server or with players you do not trust.
            # Even with it on, a player must be an operator and be listed in developers below.
            # The server console is always allowed while developer mode is on.
            # Possible values: true, false
            developerMode=false

            # [Broadcast Logs]
            # When a developer changes a value, tell the other developers who are online.
            # The change is always written to the log of the server.
            # Possible values: true, false
            broadcastLogs=true

            # [Developers]
            # Comma separated UUIDs of the players who may edit the config. You can find the
            # UUID of a player in the log when they join the server.
            developers=
            """;

    private static boolean developerMode;
    private static boolean broadcastLogs = true;
    private static Set<UUID> developers = Set.of();
    private static FileTime modified;
    private static long checkedAt;

    private DeveloperConfig() {
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(UMA.MODID).resolve(FILE);
    }

    private static synchronized void refresh() {
        long now = System.currentTimeMillis();
        if (now - checkedAt < 1000L) {
            return;
        }
        checkedAt = now;
        Path path = path();
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, DEFAULT);
            }
            FileTime time = Files.getLastModifiedTime(path);
            if (time.equals(modified)) {
                return;
            }
            modified = time;
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            }
            developerMode = Boolean.parseBoolean(properties.getProperty("developerMode", "false").trim());
            broadcastLogs = Boolean.parseBoolean(properties.getProperty("broadcastLogs", "true").trim());
            developers = parseDevelopers(properties.getProperty("developers", ""));
        } catch (IOException | RuntimeException exception) {
            UMA.LOGGER.error("Could not read {}", path, exception);
        }
    }

    private static Set<UUID> parseDevelopers(String value) {
        Set<UUID> parsed = new LinkedHashSet<>();
        for (String raw : value.split(",")) {
            String text = raw.trim();
            if (text.isEmpty()) {
                continue;
            }
            try {
                parsed.add(UUID.fromString(text));
            } catch (IllegalArgumentException exception) {
                UMA.LOGGER.error("Invalid UUID in developers of {}: {}", FILE, text);
            }
        }
        return parsed;
    }

    public static void load() {
        refresh();
    }

    public static boolean developerMode() {
        refresh();
        return developerMode;
    }

    public static boolean broadcastLogs() {
        refresh();
        return broadcastLogs;
    }

    public static Set<UUID> developers() {
        refresh();
        return developers;
    }

    public static boolean listed(UUID id) {
        return developers().contains(id);
    }

    public static String denial(CommandSourceStack source) {
        if (!developerMode()) {
            return "command.ultimatemodadditions.config_dev_off";
        }
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!source.hasPermission(2)) {
                return "command.ultimatemodadditions.config_not_op";
            }
            if (!listed(player.getUUID())) {
                return "command.ultimatemodadditions.config_not_listed";
            }
        }
        return null;
    }
}
