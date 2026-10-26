package nadiendev.ultimatemodadditions.compat.ftb;

import dev.ftb.mods.ftbchunks.api.ChunkTeamData;
import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Wealth;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RentService {

    private static final String ROOT = "uma_rent";
    private static final String PAID_UNTIL = "paid_until";
    private static final String OVERDUE_SINCE = "overdue_since";
    private static final String NOTIFIED_AT = "notified_at";
    private static final String SEEN_AT = "seen_at";
    private static final String EVICTED = "evicted";

    private static final long REAL_DAY = 86_400_000L;
    private static final long GAME_DAY = 24_000L * 50L;

    private static int ticks;

    private RentService() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!UMAConfig.COMMON.rentEnabled.get() || !UMAConfig.COMMON.bankEnabled.get()) {
            return;
        }
        ticks++;
        if (ticks < UMAConfig.COMMON.rentCheckSeconds.get() * 20) {
            return;
        }
        ticks = 0;
        MinecraftServer server = event.getServer();
        if (!FTBChunksAPI.api().isManagerLoaded() || !FTBTeamsAPI.api().isManagerLoaded()) {
            return;
        }
        try {
            checkAll(server);
        } catch (RuntimeException exception) {
            UMA.LOGGER.error("Claim rent check failed", exception);
        }
    }

    public static long dayLength() {
        return UMAConfig.COMMON.rentUseGameDays.get() ? GAME_DAY : REAL_DAY;
    }

    public static long now(MinecraftServer server) {
        return UMAConfig.COMMON.rentUseGameDays.get() ? server.overworld().getGameTime() * 50L : System.currentTimeMillis();
    }

    public static long period() {
        return UMAConfig.COMMON.rentPeriodDays.get() * dayLength();
    }

    public static BigInteger price(int chunks) {
        BigInteger mangos = BigInteger.valueOf(UMAConfig.COMMON.rentBasePriceMangos.get())
                .add(BigInteger.valueOf(UMAConfig.COMMON.rentPricePerChunkMangos.get()).multiply(BigInteger.valueOf(chunks)));
        return Wealth.centsOfMangos(mangos);
    }

    public static CompoundTag state(Team team) {
        CompoundTag extra = team.getExtraData();
        if (extra.contains(ROOT)) {
            return extra.getCompound(ROOT);
        }
        CompoundTag created = new CompoundTag();
        extra.put(ROOT, created);
        return created;
    }

    public static int claimCount(Team team) {
        return FTBChunksAPI.api().getManager().getOrCreateData(team).getClaimedChunks().size();
    }

    public static long paidUntil(Team team) {
        return state(team).getLong(PAID_UNTIL);
    }

    public static long overdueSince(Team team) {
        return state(team).getLong(OVERDUE_SINCE);
    }

    public static void extend(MinecraftServer server, Team team, long milliseconds) {
        CompoundTag rent = state(team);
        long base = Math.max(rent.getLong(PAID_UNTIL), now(server));
        rent.putLong(PAID_UNTIL, base + milliseconds);
        rent.remove(OVERDUE_SINCE);
        team.markDirty();
    }

    public static void skip(MinecraftServer server, Team team, long milliseconds) {
        CompoundTag rent = state(team);
        for (String key : new String[]{PAID_UNTIL, OVERDUE_SINCE, NOTIFIED_AT}) {
            if (rent.contains(key)) {
                rent.putLong(key, rent.getLong(key) - milliseconds);
            }
        }
        team.markDirty();
        checkNow(server, team);
    }

    public static long untilDue(MinecraftServer server, Team team) {
        CompoundTag rent = state(team);
        if (!rent.contains(PAID_UNTIL) || rent.contains(OVERDUE_SINCE)) {
            return 0L;
        }
        return Math.max(0L, rent.getLong(PAID_UNTIL) - now(server));
    }

    public static long untilEviction(MinecraftServer server, Team team) {
        CompoundTag rent = state(team);
        long grace = (long) (UMAConfig.COMMON.rentGraceDays.get() * dayLength());
        if (!rent.contains(OVERDUE_SINCE)) {
            return untilDue(server, team) + grace;
        }
        return Math.max(0L, grace - (now(server) - rent.getLong(OVERDUE_SINCE)));
    }

    public static void checkNow(MinecraftServer server, Team team) {
        if (!FTBChunksAPI.api().isManagerLoaded()) {
            return;
        }
        int chunks = claimCount(team);
        if (chunks > 0) {
            processTeam(server, team, chunks);
        }
    }

    public static void reset(Team team) {
        team.getExtraData().remove(ROOT);
        team.markDirty();
    }

    public enum PayResult {
        PAID,
        NO_CHUNKS,
        AHEAD,
        NO_FUNDS
    }

    public static PayResult payNow(MinecraftServer server, Team team, ServerPlayer player) {
        int chunks = claimCount(team);
        if (chunks == 0) {
            return PayResult.NO_CHUNKS;
        }
        if (paidUntil(team) - now(server) > period()) {
            return PayResult.AHEAD;
        }
        return payFrom(server, team, player.getUUID(), price(chunks)) ? PayResult.PAID : PayResult.NO_FUNDS;
    }

    public static boolean payFrom(MinecraftServer server, Team team, UUID payer, BigInteger cents) {
        BigInteger taken = BankService.withdraw(server, payer, cents);
        if (taken.compareTo(cents) < 0) {
            BankService.deposit(server, payer, taken);
            return false;
        }
        extend(server, team, period());
        announce(server, team, Component.translatable("message.ultimatemodadditions.rent_short_paid",
                Money.format(cents)).withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    private static void checkAll(MinecraftServer server) {
        ClaimedChunkManager manager = FTBChunksAPI.api().getManager();
        Map<UUID, Collection<ClaimedChunk>> byTeam = manager.getClaimedChunksByTeam(chunk -> true);
        for (Map.Entry<UUID, Collection<ClaimedChunk>> entry : new ArrayList<>(byTeam.entrySet())) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(entry.getKey()).orElse(null);
            if (team == null || (UMAConfig.COMMON.rentSkipServerTeams.get() && team.isServerTeam())) {
                continue;
            }
            try {
                processTeam(server, team, entry.getValue().size());
            } catch (RuntimeException exception) {
                UMA.LOGGER.error("Claim rent check failed for team {}", team.getId(), exception);
            }
        }
    }

    private static void processTeam(MinecraftServer server, Team team, int chunks) {
        CompoundTag rent = state(team);
        long now = now(server);
        long interval = UMAConfig.COMMON.rentCheckSeconds.get() * 1000L;

        if (!rent.contains(PAID_UNTIL)) {
            boolean free = UMAConfig.COMMON.rentFirstPeriodFree.get() && !rent.getBoolean(EVICTED);
            rent.remove(EVICTED);
            rent.putLong(PAID_UNTIL, free ? now + period() : now);
            rent.putLong(SEEN_AT, now);
            team.markDirty();
            if (free) {
                return;
            }
        }

        long gap = now - rent.getLong(SEEN_AT);
        if (rent.contains(SEEN_AT) && gap > interval * 3L) {
            rent.putLong(PAID_UNTIL, rent.getLong(PAID_UNTIL) + gap);
            if (rent.contains(OVERDUE_SINCE)) {
                rent.putLong(OVERDUE_SINCE, rent.getLong(OVERDUE_SINCE) + gap);
            }
        }
        rent.putLong(SEEN_AT, now);
        team.markDirty();

        long paidUntil = rent.getLong(PAID_UNTIL);
        BigInteger cost = price(chunks);
        long warnWindow = (long) (UMAConfig.COMMON.rentWarnDays.get() * dayLength());
        long throttle = dayLength() / 4L;

        if (now < paidUntil) {
            if (warnWindow > 0L && paidUntil - now <= warnWindow && now - rent.getLong(NOTIFIED_AT) >= throttle) {
                rent.putLong(NOTIFIED_AT, now);
                tell(server, team, Component.translatable("message.ultimatemodadditions.rent_due_soon",
                        Money.format(cost), duration(paidUntil - now)).withStyle(ChatFormatting.YELLOW),
                        Component.translatable("message.ultimatemodadditions.rent_short_due_soon",
                                duration(paidUntil - now)).withStyle(ChatFormatting.YELLOW), false);
            }
            return;
        }

        if (charge(server, team, cost)) {
            rent.putLong(PAID_UNTIL, now + period());
            rent.remove(OVERDUE_SINCE);
            rent.putLong(NOTIFIED_AT, now);
            team.markDirty();
            tell(server, team, Component.translatable("message.ultimatemodadditions.rent_paid",
                    Money.format(cost), duration(period())).withStyle(ChatFormatting.GREEN),
                    Component.translatable("message.ultimatemodadditions.rent_short_paid",
                            Money.format(cost)).withStyle(ChatFormatting.GREEN), true);
            return;
        }

        long grace = (long) (UMAConfig.COMMON.rentGraceDays.get() * dayLength());
        if (!rent.contains(OVERDUE_SINCE)) {
            rent.putLong(OVERDUE_SINCE, now);
            rent.putLong(NOTIFIED_AT, now);
            team.markDirty();
            tell(server, team, Component.translatable("message.ultimatemodadditions.rent_failed",
                    Money.format(cost), duration(grace)).withStyle(ChatFormatting.RED),
                    Component.translatable("message.ultimatemodadditions.rent_short_failed",
                            duration(grace)).withStyle(ChatFormatting.RED), false);
            if (grace > 0L) {
                return;
            }
        }
        long overdue = rent.getLong(OVERDUE_SINCE);
        if (now - overdue >= grace) {
            int removed = unclaimAll(server, team);
            if (removed == 0) {
                UMA.LOGGER.warn("Could not remove the claims of team {}; trying again on the next check", team.getId());
                return;
            }
            reset(team);
            state(team).putBoolean(EVICTED, true);
            team.markDirty();
            tell(server, team, Component.translatable("message.ultimatemodadditions.rent_evicted", removed)
                    .withStyle(ChatFormatting.RED),
                    Component.translatable("message.ultimatemodadditions.rent_short_evicted", removed)
                            .withStyle(ChatFormatting.RED), false);
        } else if (now - rent.getLong(NOTIFIED_AT) >= throttle) {
            rent.putLong(NOTIFIED_AT, now);
            team.markDirty();
            tell(server, team, Component.translatable("message.ultimatemodadditions.rent_failed",
                    Money.format(cost), duration(grace - (now - overdue))).withStyle(ChatFormatting.RED),
                    Component.translatable("message.ultimatemodadditions.rent_short_failed",
                            duration(grace - (now - overdue))).withStyle(ChatFormatting.RED), false);
        }
    }

    private static boolean charge(MinecraftServer server, Team team, BigInteger cents) {
        if (cents.signum() == 0) {
            return true;
        }
        List<UUID> payers = new ArrayList<>();
        if (UMAConfig.COMMON.teamBankPaysRent.get() && TeamBankCommands.usable(team)) {
            payers.add(TeamBankCommands.account(team));
        }
        payers.add(team.getOwner());
        if (UMAConfig.COMMON.rentChargeMembers.get()) {
            for (UUID member : team.getMembers()) {
                if (!member.equals(team.getOwner())) {
                    payers.add(member);
                }
            }
        }
        BigInteger total = BigInteger.ZERO;
        for (UUID payer : payers) {
            total = total.add(BankService.balance(server, payer));
        }
        if (total.compareTo(cents) < 0) {
            return false;
        }
        BigInteger left = cents;
        for (UUID payer : payers) {
            left = left.subtract(BankService.withdraw(server, payer, left));
            if (left.signum() == 0) {
                break;
            }
        }
        return true;
    }

    public static int unclaimAll(MinecraftServer server, Team team) {
        ChunkTeamData data = FTBChunksAPI.api().getManager().getOrCreateData(team);
        CommandSourceStack source = server.createCommandSourceStack();
        int removed = 0;
        for (ClaimedChunk chunk : new ArrayList<>(data.getClaimedChunks())) {
            if (data.unclaim(source, chunk.getPos(), false, true).isSuccess()) {
                removed++;
            }
        }
        return removed;
    }

    public static Component duration(long milliseconds) {
        long day = dayLength();
        long days = milliseconds / day;
        long hours = (milliseconds % day) * 24L / day;
        if (days > 0L) {
            return Component.translatable("message.ultimatemodadditions.rent_days_hours", days, hours);
        }
        return Component.translatable("message.ultimatemodadditions.rent_hours", Math.max(0L, hours));
    }

    private static void tell(MinecraftServer server, Team team, Component message, Component subtitle,
                             boolean payment) {
        for (ServerPlayer player : team.getOnlineMembers()) {
            send(player, message, subtitle);
        }
        announce(server, team, subtitle, payment);
    }

    public static void announce(MinecraftServer server, Team team, Component subtitle, boolean payment) {
        boolean everyone = payment ? UMAConfig.COMMON.rentBroadcastPayments.get()
                : UMAConfig.COMMON.rentBroadcastNotices.get();
        if (!everyone) {
            return;
        }
        Component line = Component.translatable("message.ultimatemodadditions.rent_public", team.getName(), subtitle);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!team.getMembers().contains(player.getUUID())) {
                send(player, line, line);
            }
        }
    }

    private static void send(ServerPlayer player, Component message, Component subtitle) {
        player.sendSystemMessage(message);
        if (UMAConfig.COMMON.rentTitles.get()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("message.ultimatemodadditions.rent_title").withStyle(ChatFormatting.GOLD)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }
}
