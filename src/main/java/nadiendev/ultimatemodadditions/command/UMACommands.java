package nadiendev.ultimatemodadditions.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import nadiendev.ultimatemodadditions.ban.BanCommands;
import nadiendev.ultimatemodadditions.coliseum.ColiseumState;
import nadiendev.ultimatemodadditions.coliseum.ColiseumTravel;
import nadiendev.ultimatemodadditions.compat.ftb.RentCommands;
import nadiendev.ultimatemodadditions.compat.ftb.TeamBankCommands;
import nadiendev.ultimatemodadditions.compat.ModIds;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.LootBagItem;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.daily.DailyConfigService;
import nadiendev.ultimatemodadditions.event.EventCommands;
import nadiendev.ultimatemodadditions.daily.DailyEdits;
import nadiendev.ultimatemodadditions.daily.DailyPermissions;
import nadiendev.ultimatemodadditions.daily.DailyService;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.data.JobAction;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.jobs.JobProgress;
import nadiendev.ultimatemodadditions.jobs.JobsBoard;
import nadiendev.ultimatemodadditions.jobs.JobsBoosts;
import nadiendev.ultimatemodadditions.jobs.JobsService;
import nadiendev.ultimatemodadditions.registry.UMABlocks;
import nadiendev.ultimatemodadditions.shop.ModShops;
import nadiendev.ultimatemodadditions.shop.ShopEdits;
import nadiendev.ultimatemodadditions.shop.ShopPermissions;
import nadiendev.ultimatemodadditions.shop.ShopService;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = UMA.MODID)
public final class UMACommands {

    private UMACommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = root();
        root.then(UMAHelp.command());
        root.then(BanCommands.command());
        root.then(EventCommands.command());
        for (LiteralArgumentBuilder<CommandSourceStack> builder : List.of(moneyCommand(), shopCommand(), jobsCommand(), dailyCommand())) {
            root.then(builder);
        }
        if (ModIds.loaded(ModIds.FTB_CHUNKS)) {
            root.then(RentCommands.root());
            root.then(TeamBankCommands.root());
        }
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);
        dispatcher.register(Commands.literal("ums").redirect(node));
        Map<String, Supplier<LiteralArgumentBuilder<CommandSourceStack>>> shortcuts = Map.of(
                "shop", UMACommands::shopCommand,
                "jobs", UMACommands::jobsCommand,
                "money", UMACommands::moneyCommand,
                "daily", UMACommands::dailyCommand);
        for (String name : UMAConfig.COMMON.rootCommandShortcuts.get()) {
            String key = name.trim().toLowerCase(Locale.ROOT);
            Supplier<LiteralArgumentBuilder<CommandSourceStack>> factory = shortcuts.get(key);
            if (factory != null && dispatcher.getRoot().getChild(key) == null) {
                dispatcher.register(factory.get());
            }
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("uma")
                .then(Commands.literal("coliseum")
                        .then(Commands.literal("enter").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return ColiseumTravel.enter(player, context.getSource().getServer()) ? 1 : 0;
                        }))
                        .then(Commands.literal("leave").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return ColiseumTravel.leave(player, context.getSource().getServer()) ? 1 : 0;
                        })))
                .then(Commands.literal("difficulty")
                        .then(Commands.literal("get").executes(context -> {
                            ResourceLocation current = ColiseumState.get(context.getSource().getServer()).difficulty();
                            context.getSource().sendSuccess(() -> Component.translatable(
                                    "command.ultimatemodadditions.difficulty_current", current.toString()), false);
                            return 1;
                        }))
                        .then(Commands.literal("list").executes(context -> {
                            String joined = String.join(", ", UMAData.difficulties().keySet().stream()
                                    .map(ResourceLocation::toString).toList());
                            context.getSource().sendSuccess(() -> Component.literal(joined), false);
                            return 1;
                        }))
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                UMAData.difficulties().keySet(), builder))
                                        .executes(UMACommands::setDifficulty))))
                .then(Commands.literal("bag")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("bag", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                UMAData.lootBags().keySet(), builder))
                                        .executes(context -> giveBag(context, 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveBag(context,
                                                        IntegerArgumentType.getInteger(context, "count")))))))
                .then(tpsCommand())
                .then(pingCommand())
                .then(spawnersCommand())
                .then(rebuildCommand());
    }

    private static int setDifficulty(CommandContext<CommandSourceStack> context) {
        ResourceLocation id = ResourceLocationArgument.getId(context, "id");
        if (!UMAData.difficulties().containsKey(id)) {
            context.getSource().sendFailure(Component.translatable(
                    "command.ultimatemodadditions.unknown_difficulty", id.toString()));
            return 0;
        }
        ColiseumState.get(context.getSource().getServer()).setDifficulty(id);
        context.getSource().sendSuccess(() -> Component.translatable(
                "command.ultimatemodadditions.difficulty_set", id.toString()), true);
        return 1;
    }

    private static int giveBag(CommandContext<CommandSourceStack> context, int count)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation bag = ResourceLocationArgument.getId(context, "bag");
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            ItemStack stack = LootBagItem.create(bag, count);
            if (stack.isEmpty()) {
                context.getSource().sendFailure(Component.translatable(
                        "message.ultimatemodadditions.unknown_bag", bag.toString()));
                return 0;
            }
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tpsCommand() {
        return Commands.literal("tps").executes(context -> {
            net.minecraft.server.MinecraftServer server = context.getSource().getServer();
            double mspt = server.getAverageTickTimeNanos() / 1_000_000.0D;
            float target = server.tickRateManager().tickrate();
            double tps = Math.min(target, 1000.0D / Math.max(mspt, 1.0E-4D));
            double budget = 1000.0D / Math.max(target, 1.0F);

            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.tps_header").withStyle(net.minecraft.ChatFormatting.GOLD), false);
            context.getSource().sendSuccess(() -> Component.translatable(
                            "command.ultimatemodadditions.tps_line",
                            Component.literal(String.format(java.util.Locale.ROOT, "%.2f", tps)).withStyle(tpsColor(tps, target)),
                            Component.literal(String.format(java.util.Locale.ROOT, "%.2f", mspt)).withStyle(msptColor(mspt, budget)),
                            String.format(java.util.Locale.ROOT, "%.0f", budget)),
                    false);

            long[] samples = server.getTickTimesNanos();
            double peak = 0.0D;
            for (long sample : samples) {
                peak = Math.max(peak, sample / 1_000_000.0D);
            }
            final double peakMs = peak;
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.mspt_peak",
                    String.format(java.util.Locale.ROOT, "%.2f", peakMs)).withStyle(net.minecraft.ChatFormatting.GRAY), false);

            for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
                long[] worldTimes = server.getTickTime(level.dimension());
                if (worldTimes == null) {
                    continue;
                }
                double sum = 0.0D;
                for (long sample : worldTimes) {
                    sum += sample;
                }
                double worldMspt = sum / worldTimes.length / 1_000_000.0D;
                String name = level.dimension().location().toString();
                context.getSource().sendSuccess(() -> Component.translatable(
                                "command.ultimatemodadditions.tps_dimension", name,
                                String.format(java.util.Locale.ROOT, "%.2f", worldMspt))
                        .withStyle(net.minecraft.ChatFormatting.DARK_AQUA), false);
            }
            return (int) Math.round(tps);
        });
    }

    private static net.minecraft.ChatFormatting tpsColor(double tps, float target) {
        if (tps >= target - 0.5D) {
            return net.minecraft.ChatFormatting.GREEN;
        }
        return tps >= target * 0.75D ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
    }

    private static net.minecraft.ChatFormatting msptColor(double mspt, double budget) {
        if (mspt <= budget * 0.5D) {
            return net.minecraft.ChatFormatting.GREEN;
        }
        return mspt <= budget ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pingCommand() {
        return Commands.literal("ping")
                .executes(context -> reportPing(context.getSource(),
                        java.util.List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> reportPing(context.getSource(),
                                java.util.List.copyOf(EntityArgument.getPlayers(context, "targets")))));
    }

    private static int reportPing(CommandSourceStack source, java.util.List<ServerPlayer> players) {
        int best = 0;
        for (ServerPlayer player : players) {
            int latency = player.connection.latency();
            best = Math.max(best, latency);
            source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ping",
                    player.getGameProfile().getName(), latency).withStyle(pingColor(latency)), false);
        }
        return best;
    }

    private static net.minecraft.ChatFormatting pingColor(int latency) {
        if (latency < 100) {
            return net.minecraft.ChatFormatting.GREEN;
        }
        return latency < 250 ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> spawnersCommand() {
        return Commands.literal("spawners")
                .then(Commands.literal("list").executes(context -> spawners(context, Action.LIST)))
                .then(Commands.literal("place")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawners(context, Action.PLACE)))
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawners(context, Action.CLEAR)));
    }

    private enum Action {
        LIST,
        PLACE,
        CLEAR
    }

    private static int spawners(CommandContext<CommandSourceStack> context, Action action) {
        net.minecraft.server.level.ServerLevel level = context.getSource().getServer()
                .getLevel(nadiendev.ultimatemodadditions.coliseum.Coliseum.DIMENSION);
        if (level == null) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.no_coliseum"));
            return 0;
        }

        switch (action) {
            case LIST -> {
                int found = nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.count(level);
                context.getSource().sendSuccess(() -> Component.translatable(
                        "command.ultimatemodadditions.spawners_count", found), false);
                return found;
            }
            case CLEAR -> {
                int removed = nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.clear(level);
                context.getSource().sendSuccess(() -> Component.translatable(
                        "command.ultimatemodadditions.spawners_cleared", removed), true);
                return removed;
            }
            default -> {
                if (!nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.enabled()) {
                    context.getSource().sendFailure(
                            Component.translatable("command.ultimatemodadditions.spawners_disabled"));
                    return 0;
                }
                int placed = nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.place(level);
                if (placed == 0) {
                    context.getSource().sendFailure(
                            Component.translatable("command.ultimatemodadditions.spawners_none"));
                    return 0;
                }
                context.getSource().sendSuccess(() -> Component.translatable(
                        "command.ultimatemodadditions.spawners_placed", placed), true);
                return placed;
            }
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> rebuildCommand() {
        return Commands.literal("rebuild_arena")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    boolean built = nadiendev.ultimatemodadditions.coliseum.ColiseumStartup
                            .ensureArena(context.getSource().getServer(), true);
                    context.getSource().sendSuccess(() -> Component.translatable(built
                            ? "command.ultimatemodadditions.arena_built"
                            : "command.ultimatemodadditions.arena_failed"), true);
                    return built ? 1 : 0;
                });
    }


    private static final Pattern AMOUNT = Pattern.compile("(\\d+(?:[.,]\\d+)?)([lL]?)");
    private static final BigDecimal LUCA = BigDecimal.valueOf(1000L);
    private static final BigDecimal CENTS = BigDecimal.valueOf(100L);

    private static final DynamicCommandExceptionType INVALID_AMOUNT = new DynamicCommandExceptionType(
            value -> Component.translatable("command.ultimatemodadditions.money_invalid_amount", value));
    private static final SimpleCommandExceptionType NO_FUNDS = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.money_no_funds"));
    private static final SimpleCommandExceptionType BANK_DISABLED = new SimpleCommandExceptionType(
            Component.translatable("message.ultimatemodadditions.bank_disabled"));

    private static LiteralArgumentBuilder<CommandSourceStack> moneyCommand() {
        return Commands.literal("money")
                .then(Commands.literal("balance")
                        .executes(context -> moneyBalance(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> moneyBalance(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(amount().executes(UMACommands::moneyPay))))
                .then(moneyAdmin("give", Operation.GIVE))
                .then(moneyAdmin("take", Operation.TAKE))
                .then(moneyAdmin("set", Operation.SET));
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> amount() {
        return Commands.argument("amount", StringArgumentType.word());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> moneyAdmin(String name, Operation operation) {
        return Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players())
                        .then(amount().executes(context -> moneyAdjust(context, operation))));
    }

    private enum Operation {
        GIVE,
        TAKE,
        SET
    }

    public static BigInteger parseAmount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String raw = StringArgumentType.getString(context, "amount");
        Matcher matcher = AMOUNT.matcher(raw);
        if (!matcher.matches()) {
            throw INVALID_AMOUNT.create(raw);
        }
        BigDecimal mangos = new BigDecimal(matcher.group(1).replace(',', '.'));
        if (!matcher.group(2).isEmpty()) {
            mangos = mangos.multiply(LUCA);
        }
        return mangos.multiply(CENTS).setScale(0, RoundingMode.DOWN).toBigInteger();
    }

    public static void requireBank() throws CommandSyntaxException {
        if (!UMAConfig.COMMON.bankEnabled.get()) {
            throw BANK_DISABLED.create();
        }
    }

    private static int moneyBalance(CommandContext<CommandSourceStack> context, ServerPlayer target)
            throws CommandSyntaxException {
        requireBank();
        BigInteger value = BankService.balance(context.getSource().getServer(), target.getUUID());
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.money_balance",
                target.getDisplayName(), Money.format(value)), false);
        return 1;
    }

    private static int moneyPay(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        requireBank();
        ServerPlayer payer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        BigInteger cents = parseAmount(context);
        if (cents.signum() <= 0) {
            throw INVALID_AMOUNT.create(StringArgumentType.getString(context, "amount"));
        }
        var server = context.getSource().getServer();
        BigInteger taken = BankService.withdraw(server, payer.getUUID(), cents);
        if (taken.compareTo(cents) < 0) {
            BankService.deposit(server, payer.getUUID(), taken);
            throw NO_FUNDS.create();
        }
        BigInteger received = BankService.deposit(server, target.getUUID(), cents);
        if (received.compareTo(cents) < 0) {
            BankService.deposit(server, payer.getUUID(), cents.subtract(received));
        }
        BigInteger sent = received;
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.money_paid",
                Money.format(sent), target.getDisplayName()), false);
        target.sendSystemMessage(Component.translatable("command.ultimatemodadditions.money_received",
                payer.getDisplayName(), Money.format(sent)));
        return 1;
    }

    private static int moneyAdjust(CommandContext<CommandSourceStack> context, Operation operation)
            throws CommandSyntaxException {
        requireBank();
        BigInteger cents = parseAmount(context);
        var server = context.getSource().getServer();
        int count = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
            switch (operation) {
                case GIVE -> {
                    BigInteger added = BankService.deposit(server, player.getUUID(), cents);
                    moneyFeedback(context, "command.ultimatemodadditions.money_given", added, player);
                }
                case TAKE -> {
                    BigInteger taken = BankService.withdraw(server, player.getUUID(), cents);
                    moneyFeedback(context, "command.ultimatemodadditions.money_taken", taken, player);
                }
                case SET -> {
                    BankService.set(server, player.getUUID(), cents);
                    BigInteger now = BankService.balance(server, player.getUUID());
                    moneyFeedback(context, "command.ultimatemodadditions.money_set", now, player);
                }
            }
            count++;
        }
        return count;
    }

    private static void moneyFeedback(CommandContext<CommandSourceStack> context, String key, BigInteger value,
                                 ServerPlayer player) {
        context.getSource().sendSuccess(() -> Component.translatable(key, Money.format(value),
                player.getDisplayName()), true);
    }


    private static final SimpleCommandExceptionType DISABLED = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.shop_disabled"));
    private static final SimpleCommandExceptionType TOO_FAR = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.shop_too_far"));

    private static LiteralArgumentBuilder<CommandSourceStack> dailyCommand() {
        return Commands.literal("daily")
                .requires(source -> UMAConfig.COMMON.dailyEnabled.get())
                .executes(UMACommands::dailyOpen)
                .then(Commands.literal("claim")
                        .executes(context -> dailyClaim(context, null))
                        .then(Commands.literal("daily").executes(context -> dailyClaim(context, DailyReward.Period.DAILY)))
                        .then(Commands.literal("weekly").executes(context -> dailyClaim(context, DailyReward.Period.WEEKLY)))
                        .then(Commands.literal("monthly").executes(context -> dailyClaim(context, DailyReward.Period.MONTHLY))))
                .then(Commands.literal("config")
                        .requires(DailyPermissions::canConfigure)
                        .executes(UMACommands::dailyConfig))
                .then(Commands.literal("reset")
                        .requires(DailyPermissions::canConfigure)
                        .executes(UMACommands::dailyReset))
                .then(Commands.literal("resetplayer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(UMACommands::dailyResetPlayer)));
    }

    private static int dailyConfig(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        DailyConfigService.open(context.getSource().getPlayerOrException());
        return 1;
    }

    private static int dailyOpen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        DailyService.sync(context.getSource().getPlayerOrException(), true);
        return 1;
    }

    private static int dailyClaim(CommandContext<CommandSourceStack> context, DailyReward.Period period)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (period != null) {
            DailyService.claim(player, period);
        } else {
            for (DailyReward.Period each : DailyReward.Period.values()) {
                DailyService.claim(player, each);
            }
        }
        return 1;
    }

    private static int dailyReset(CommandContext<CommandSourceStack> context) {
        DailyEdits.get(context.getSource().getServer()).clear();
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.daily_reset"), true);
        return 1;
    }

    private static int dailyResetPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        DailyService.resetState(target);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.daily_reset_player",
                target.getDisplayName()), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> shopCommand() {
        return Commands.literal("shop")
                .requires(source -> source.hasPermission(UMAConfig.COMMON.shopPermissionLevel.get()))
                .executes(UMACommands::shopOpen)
                .then(Commands.literal("admin")
                        .requires(ShopPermissions::canEdit)
                        .executes(UMACommands::shopAdmin))
                .then(Commands.literal("reset")
                        .requires(ShopPermissions::canReset)
                        .executes(UMACommands::shopReset));
    }

    private static int shopReset(CommandContext<CommandSourceStack> context) {
        ShopEdits.get(context.getSource().getServer()).clear();
        ModShops.invalidate();
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.shop_reset"), true);
        return 1;
    }

    private static int shopOpen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!UMAConfig.COMMON.shopEnabled.get()) {
            throw DISABLED.create();
        }
        if (UMAConfig.COMMON.shopRequireStallNearby.get() && !stallNearby(player)) {
            throw TOO_FAR.create();
        }
        ShopService.openTiendita(player);
        return 1;
    }

    private static int shopAdmin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ShopService.open(context.getSource().getPlayerOrException());
        return 1;
    }

    private static boolean stallNearby(ServerPlayer player) {
        Level level = player.level();
        int radius = UMAConfig.COMMON.shopStallRadius.get();
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (level.getBlockState(pos).is(UMABlocks.EXCHANGE_STAND.get())) {
                return true;
            }
        }
        return false;
    }


    private static final SimpleCommandExceptionType UNKNOWN_JOB = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.job_unknown"));
    private static final SimpleCommandExceptionType CANNOT_JOIN = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.job_cannot_join"));
    private static final SimpleCommandExceptionType NOT_IN_JOB = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.job_not_in"));

    private static LiteralArgumentBuilder<CommandSourceStack> jobsCommand() {
        return Commands.literal("jobs")
                .requires(source -> UMAConfig.COMMON.jobsEnabled.get())
                .executes(UMACommands::jobsOpen)
                .then(Commands.literal("list").executes(UMACommands::jobsList))
                .then(Commands.literal("join")
                        .then(Commands.argument("job", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        UMAData.jobs().keySet().stream().map(ResourceLocation::getPath), builder))
                                .executes(UMACommands::jobsJoin)))
                .then(Commands.literal("leave")
                        .then(Commands.argument("job", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        UMAData.jobs().keySet().stream().map(ResourceLocation::getPath), builder))
                                .executes(UMACommands::jobsLeave)))
                .then(Commands.literal("browse").executes(UMACommands::jobsOpen))
                .then(Commands.literal("leaveall").executes(UMACommands::jobsLeaveAll))
                .then(Commands.literal("stats").executes(UMACommands::jobsStats))
                .then(Commands.literal("info")
                        .then(jobArgument().executes(UMACommands::jobsInfo)))
                .then(Commands.literal("top")
                        .then(jobArgument().executes(UMACommands::jobsTop)))
                .then(Commands.literal("boost")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(boostTarget()
                                        .executes(context -> jobsBoostAdd(context, 2.0D, 0))
                                        .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.1D, 100.0D))
                                                .executes(context -> jobsBoostAdd(context,
                                                        DoubleArgumentType.getDouble(context, "multiplier"), 0))
                                                .then(Commands.argument("minutes", IntegerArgumentType.integer(1, 525600))
                                                        .executes(context -> jobsBoostAdd(context,
                                                                DoubleArgumentType.getDouble(context, "multiplier"),
                                                                IntegerArgumentType.getInteger(context, "minutes")))))))
                        .then(Commands.literal("clear")
                                .then(boostTarget().executes(UMACommands::jobsBoostClear)))
                        .then(Commands.literal("list").executes(UMACommands::jobsBoostList)))
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("employ")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(jobArgument().executes(UMACommands::jobsEmploy))))
                        .then(Commands.literal("fire")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(jobArgument().executes(UMACommands::jobsFire))))
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(jobArgument()
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 10000))
                                                        .executes(UMACommands::jobsSetLevel)))))
                        .then(Commands.literal("addxp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(jobArgument()
                                                .then(Commands.argument("xp", DoubleArgumentType.doubleArg(0.0D))
                                                        .executes(UMACommands::jobsAddXp))))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> boostTarget() {
        return Commands.argument("target", StringArgumentType.word())
                .suggests((context, builder) -> {
                    List<String> names = new java.util.ArrayList<>();
                    names.add(JobsBoosts.ALL);
                    UMAData.jobs().keySet().forEach(id -> names.add(id.getPath()));
                    return SharedSuggestionProvider.suggest(names, builder);
                });
    }

    private static String boostKey(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String target = StringArgumentType.getString(context, "target");
        if (target.equalsIgnoreCase(JobsBoosts.ALL)) {
            return JobsBoosts.ALL;
        }
        ResourceLocation id = UMA.parse(target);
        if (!UMAData.jobs().containsKey(id)) {
            throw UNKNOWN_JOB.create();
        }
        return id.toString();
    }

    private static Component boostName(String key) {
        if (key.equals(JobsBoosts.ALL)) {
            return Component.translatable("command.ultimatemodadditions.job_boost_all");
        }
        ResourceLocation id = ResourceLocation.parse(key);
        JobDefinition job = UMAData.jobs().get(id);
        return job == null ? Component.literal(key) : JobsService.displayName(id, job);
    }

    private static int jobsBoostAdd(CommandContext<CommandSourceStack> context, double multiplier, int minutes)
            throws CommandSyntaxException {
        String key = boostKey(context);
        long until = minutes > 0 ? System.currentTimeMillis() + minutes * 60_000L : 0L;
        JobsBoosts.get(context.getSource().getServer()).set(key, multiplier, until);
        Component target = boostName(key);
        Component message = minutes > 0
                ? Component.translatable("command.ultimatemodadditions.job_boost_added_timed", multiplier, target, minutes)
                : Component.translatable("command.ultimatemodadditions.job_boost_added", multiplier, target);
        context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                message.copy().withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    private static int jobsBoostClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String key = boostKey(context);
        if (!JobsBoosts.get(context.getSource().getServer()).clear(key)) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.job_boost_none"));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_boost_cleared",
                boostName(key)), true);
        return 1;
    }

    private static int jobsBoostList(CommandContext<CommandSourceStack> context) {
        Map<String, JobsBoosts.Boost> active = JobsBoosts.get(context.getSource().getServer()).active();
        if (active.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_boost_none"), false);
            return 0;
        }
        long now = System.currentTimeMillis();
        active.forEach((key, boost) -> context.getSource().sendSuccess(() -> Component.translatable(
                boost.until() == 0L ? "command.ultimatemodadditions.job_boost_line"
                        : "command.ultimatemodadditions.job_boost_line_timed", boostName(key), boost.multiplier(),
                Math.max(1L, (boost.until() - now) / 60_000L)), false));
        return active.size();
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> jobArgument() {
        return Commands.argument("job", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        UMAData.jobs().keySet().stream().map(ResourceLocation::getPath), builder));
    }

    private static ResourceLocation jobId(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = UMA.parse(StringArgumentType.getString(context, "job"));
        if (!UMAData.jobs().containsKey(id)) {
            throw UNKNOWN_JOB.create();
        }
        return id;
    }

    private static int jobsJoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = jobId(context);
        if (!JobsService.join(player, id)) {
            throw CANNOT_JOIN.create();
        }
        JobDefinition job = UMAData.jobs().get(id);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_joined",
                JobsService.displayName(id, job)), false);
        return 1;
    }

    private static int jobsLeave(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResourceLocation id = jobId(context);
        if (!JobsService.leave(player, id)) {
            throw NOT_IN_JOB.create();
        }
        JobDefinition job = UMAData.jobs().get(id);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_left",
                JobsService.displayName(id, job)), false);
        return 1;
    }

    private static int jobsOpen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        JobsService.sync(context.getSource().getPlayerOrException(), true);
        return 1;
    }

    private static int jobsList(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Map<ResourceLocation, JobProgress> mine = JobsService.progress(player);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_header",
                mine.size(), UMAConfig.COMMON.jobsMaxPerPlayer.get()).withStyle(ChatFormatting.GOLD), false);
        for (Map.Entry<ResourceLocation, JobDefinition> entry : UMAData.jobs().entrySet()) {
            ResourceLocation id = entry.getKey();
            JobDefinition job = entry.getValue();
            JobProgress progress = mine.get(id);
            MutableComponent line = Component.empty()
                    .append(JobsService.displayName(id, job).copy().withStyle(ChatFormatting.YELLOW));
            if (progress != null) {
                line.append(Component.translatable("command.ultimatemodadditions.job_level", progress.level(),
                        Math.round(progress.xp()), Math.round(job.xpNeeded(progress.level())))
                        .withStyle(ChatFormatting.GRAY));
                line.append(button("command.ultimatemodadditions.job_leave_button", "/uma jobs leave " + id.getPath(),
                        ChatFormatting.RED));
            } else {
                line.append(button("command.ultimatemodadditions.job_join_button", "/uma jobs join " + id.getPath(),
                        ChatFormatting.GREEN));
            }
            if (!job.description().isEmpty()) {
                line.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal(job.description()))));
            }
            context.getSource().sendSuccess(() -> line, false);
        }
        return 1;
    }

    private static int jobsInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = jobId(context);
        JobDefinition job = UMAData.jobs().get(id);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_info_header",
                JobsService.displayName(id, job), job.maxLevel()).withStyle(ChatFormatting.GOLD), false);
        if (!job.description().isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal(job.description()).withStyle(ChatFormatting.GRAY),
                    false);
        }
        for (JobAction action : job.actions()) {
            Component label = JobsService.label(action);
            context.getSource().sendSuccess(() -> Component.literal("- ")
                    .append(Component.translatable("screen.ultimatemodadditions.job_action_"
                            + action.type().getSerializedName()))
                    .append(" ").append(label)
                    .append(Component.translatable("command.ultimatemodadditions.job_info_pay",
                            Money.format(Math.round(action.pay() * Money.toCents(1L))), action.xp()))
                    .withStyle(ChatFormatting.WHITE), false);
        }
        return 1;
    }

    private static int jobsStats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Map<ResourceLocation, JobProgress> mine = JobsService.progress(player);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_stats_header")
                .withStyle(ChatFormatting.GOLD), false);
        if (mine.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_stats_none")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        long day = System.currentTimeMillis() / 86_400_000L;
        for (Map.Entry<ResourceLocation, JobProgress> entry : mine.entrySet()) {
            JobDefinition job = UMAData.jobs().get(entry.getKey());
            if (job == null) {
                continue;
            }
            JobProgress progress = entry.getValue();
            String title = job.titleFor(progress.level());
            MutableComponent line = Component.empty()
                    .append(JobsService.displayName(entry.getKey(), job).copy().withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(title.isEmpty() ? "" : " [" + title + "]")
                            .withStyle(ChatFormatting.AQUA))
                    .append(Component.translatable("command.ultimatemodadditions.job_stats_line", progress.level(),
                            Math.round(progress.xp()), Math.round(job.xpNeeded(progress.level())),
                            Money.format(Math.round(progress.earnedOn(day) * Money.toCents(1L))),
                            Money.format(Math.round(progress.earned() * Money.toCents(1L))))
                            .withStyle(ChatFormatting.GRAY));
            context.getSource().sendSuccess(() -> line, false);
        }
        return 1;
    }

    private static int jobsTop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = jobId(context);
        JobDefinition job = UMAData.jobs().get(id);
        List<JobsBoard.Entry> top = JobsBoard.get(context.getSource().getServer()).top(id,
                UMAConfig.COMMON.jobsTopSize.get());
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_top_header",
                JobsService.displayName(id, job)).withStyle(ChatFormatting.GOLD), false);
        if (top.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_top_none")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        int place = 1;
        for (JobsBoard.Entry entry : top) {
            int rank = place++;
            context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_top_line",
                    rank, entry.name(), entry.level(), Math.round(entry.xp())).withStyle(
                    rank == 1 ? ChatFormatting.YELLOW : ChatFormatting.WHITE), false);
        }
        return 1;
    }

    private static int jobsLeaveAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int count = 0;
        for (ResourceLocation id : List.copyOf(JobsService.progress(player).keySet())) {
            if (JobsService.leave(player, id)) {
                count++;
            }
        }
        int left = count;
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_left_all", left),
                false);
        return count;
    }

    private static int jobsEmploy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ResourceLocation id = jobId(context);
        if (!JobsService.employ(target, id)) {
            throw CANNOT_JOIN.create();
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_admin_employ",
                target.getDisplayName(), JobsService.displayName(id, UMAData.jobs().get(id))), true);
        return 1;
    }

    private static int jobsFire(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ResourceLocation id = jobId(context);
        if (!JobsService.leave(target, id)) {
            throw NOT_IN_JOB.create();
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_admin_fire",
                target.getDisplayName(), JobsService.displayName(id, UMAData.jobs().get(id))), true);
        return 1;
    }

    private static int jobsSetLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ResourceLocation id = jobId(context);
        int level = IntegerArgumentType.getInteger(context, "level");
        if (!JobsService.setLevel(target, id, level)) {
            throw NOT_IN_JOB.create();
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_admin_level",
                target.getDisplayName(), JobsService.displayName(id, UMAData.jobs().get(id)), level), true);
        return 1;
    }

    private static int jobsAddXp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ResourceLocation id = jobId(context);
        double xp = DoubleArgumentType.getDouble(context, "xp");
        if (!JobsService.addExperience(target, id, xp)) {
            throw NOT_IN_JOB.create();
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.job_admin_xp",
                target.getDisplayName(), JobsService.displayName(id, UMAData.jobs().get(id)), Math.round(xp)), true);
        return 1;
    }

    private static MutableComponent button(String key, String command, ChatFormatting color) {
        return Component.literal(" ").append(Component.translatable(key).withStyle(style -> style
                .withColor(color)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))));
    }
}
