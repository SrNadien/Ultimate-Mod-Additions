package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.network.payload.JobActionPayload;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload.ActionView;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload.JobView;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload.RewardView;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload.TitleView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JobsScreen extends Screen {

    private static final int WIDTH = 330;
    private static final int HEIGHT = 240;
    private static final int GRID_X = 125;
    private static final int GRID_Y = 28;
    private static final int SLOT = 20;
    private static final int COLUMNS = 9;
    private static final int ROWS = 2;
    private static final int PER_PAGE = COLUMNS * ROWS;
    private static final int LIST_X = 125;
    private static final int LIST_Y = 96;
    private static final int LIST_WIDTH = 195;
    private static final int ROW = 13;
    private static final int LIST_ROWS = 10;

    private static final int PANEL = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADE = 0xFF555555;
    private static final int OUTLINE = 0xFF000000;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int ROW_A = 0xFFB4B4B4;
    private static final int ROW_B = 0xFFBFBFBF;
    private static final int TEXT = 0xFF202020;
    private static final int GREEN = 0xFF1E7B34;
    private static final int GRAY = 0xFF6B6B6B;
    private static final int AQUA = 0xFF1A6E8E;
    private static final int GOLD = 0xFF9A6A00;
    private static final int ORANGE = 0xFFD9741B;


    private static final Map<String, Integer> COLORS = Map.ofEntries(
            Map.entry("farmer", 0x9C7A32), Map.entry("fisher", 0x2E6FA8), Map.entry("hunter", 0x3E6B2E),
            Map.entry("miner", 0x6B6B6B), Map.entry("woodcutter", 0x7A4A22), Map.entry("builder", 0xC8B08A),
            Map.entry("crafter", 0x9A5A2A), Map.entry("smelter", 0xB03A1E), Map.entry("explorer", 0x2E8A8A),
            Map.entry("breeder", 0x8A6AB0), Map.entry("enchanter", 0x6A2EB0), Map.entry("brewer", 0x2EB08A),
            Map.entry("digger", 0x8A7A55), Map.entry("weaponsmith", 0x555555), Map.entry("armorer", 0x777788),
            Map.entry("cook", 0xE8E0D0), Map.entry("netherwalker", 0x8A1E1E));

    private int left;
    private int top;
    private int page;
    private int tab;
    private int scroll;
    private ResourceLocation selection;

    public JobsScreen() {
        super(Component.translatable("screen.ultimatemodadditions.jobs"));
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        ensureSelection();
        rebuild();
    }

    public void refresh() {
        ensureSelection();
        rebuild();
    }

    private List<JobView> jobs() {
        return ClientHooks.jobs().jobs();
    }

    private void ensureSelection() {
        if (selected() != null) {
            return;
        }
        selection = null;
        for (JobView job : jobs()) {
            if (job.joined()) {
                selection = job.id();
                return;
            }
        }
        if (!jobs().isEmpty()) {
            selection = jobs().get(0).id();
        }
    }

    private JobView selected() {
        for (JobView job : jobs()) {
            if (job.id().equals(selection)) {
                return job;
            }
        }
        return null;
    }

    private int joinedCount() {
        int count = 0;
        for (JobView job : jobs()) {
            if (job.joined()) {
                count++;
            }
        }
        return count;
    }

    private boolean full() {
        return joinedCount() >= ClientHooks.jobs().maxJobs();
    }

    private int pageCount() {
        return Math.max(1, (jobs().size() + PER_PAGE - 1) / PER_PAGE);
    }

    private void rebuild() {
        clearWidgets();
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.job_tab_actions"), button -> {
            tab = 0;
            scroll = 0;
        }).bounds(left + LIST_X, top + 76, 96, 14).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.job_tab_rewards"), button -> {
            tab = 1;
            scroll = 0;
        }).bounds(left + LIST_X + 99, top + 76, 96, 14).build());

        addRenderableWidget(Button.builder(Component.literal("←"), button -> step(-1))
                .bounds(left + 14, top + 190, 40, 16).build());
        addRenderableWidget(Button.builder(Component.literal("→"), button -> step(1))
                .bounds(left + 58, top + 190, 40, 16).build());

        JobView job = selected();
        if (job != null) {
            boolean blocked = !job.joined() && full();
            Component label = Component.translatable(blocked ? "screen.ultimatemodadditions.job_full"
                    : (job.joined() ? "screen.ultimatemodadditions.job_leave" : "screen.ultimatemodadditions.job_join"));
            Button join = Button.builder(label, button -> PacketDistributor.sendToServer(
                    new JobActionPayload(job.id(), !job.joined()))).bounds(left + 14, top + 166, 84, 16).build();
            join.active = !blocked;
            addRenderableWidget(join);
        }
        if (pageCount() > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), button -> page = Math.max(0, page - 1))
                    .bounds(left + GRID_X + SLOT * COLUMNS + 4, top + GRID_Y, 14, 18).build());
            addRenderableWidget(Button.builder(Component.literal(">"),
                            button -> page = Math.min(pageCount() - 1, page + 1))
                    .bounds(left + GRID_X + SLOT * COLUMNS + 4, top + GRID_Y + 22, 14, 18).build());
        }
    }

    private void step(int direction) {
        List<JobView> jobs = jobs();
        if (jobs.isEmpty()) {
            return;
        }
        int current = 0;
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).id().equals(selection)) {
                current = i;
            }
        }
        int next = (current + direction + jobs.size()) % jobs.size();
        selection = jobs.get(next).id();
        page = next / PER_PAGE;
        scroll = 0;
        rebuild();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int jobAt(double mouseX, double mouseY) {
        double x = mouseX - left - GRID_X;
        double y = mouseY - top - GRID_Y;
        if (x < 0 || y < 0 || x >= SLOT * COLUMNS || y >= SLOT * ROWS) {
            return -1;
        }
        int index = page * PER_PAGE + (int) (y / SLOT) * COLUMNS + (int) (x / SLOT);
        return index < jobs().size() ? index : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int index = jobAt(mouseX, mouseY);
        if (index >= 0 && button == 0) {
            selection = jobs().get(index).id();
            scroll = 0;
            rebuild();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        JobView job = selected();
        if (job != null) {
            int total = tab == 0 ? job.actions().size() : rewardLines(job).size();
            scroll = Math.max(0, Math.min(scroll - (int) Math.signum(scrollY), Math.max(0, total - LIST_ROWS)));
        }
        return true;
    }

    private void raised(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, OUTLINE);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, LIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, LIGHT);
        graphics.fill(x + 2, y + h - 2, x + w - 1, y + h - 1, SHADE);
        graphics.fill(x + w - 2, y + 2, x + w - 1, y + h - 1, SHADE);
    }

    private void inset(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, SLOT_FILL);
        graphics.fill(x, y, x + w, y + 1, SLOT_DARK);
        graphics.fill(x, y, x + 1, y + h, SLOT_DARK);
        graphics.fill(x + w - 1, y + 1, x + w, y + h, LIGHT);
        graphics.fill(x + 1, y + h - 1, x + w, y + h, LIGHT);
    }

    private void plainText(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, false);
    }

    private void fittedText(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
        float fit = Math.min(1.0F, (float) maxWidth / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (1.0F - fit) * 4.0F, 0.0F);
        graphics.pose().scale(fit, fit, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        raised(graphics, left, top, WIDTH, HEIGHT);
        raised(graphics, left + WIDTH / 2 - 55, top + 5, 110, 17);
        Component header = title.copy();
        plainText(graphics, header, left + WIDTH / 2 - font.width(header) / 2, top + 10, TEXT);
        Component count = Component.translatable("screen.ultimatemodadditions.jobs_count",
                joinedCount(), ClientHooks.jobs().maxJobs());
        plainText(graphics, count, left + WIDTH - 12 - font.width(count), top + 10, GREEN);

        JobView job = selected();
        renderPortrait(graphics, job, mouseX, mouseY);
        renderGrid(graphics);
        if (job != null) {
            if (tab == 0) {
                renderActions(graphics, job);
            } else {
                renderRewards(graphics, job);
            }
        } else {
            plainText(graphics, Component.translatable("screen.ultimatemodadditions.job_empty"),
                    left + LIST_X, top + LIST_Y + 4, GRAY);
        }
    }

    private void renderPortrait(GuiGraphics graphics, JobView job, int mouseX, int mouseY) {
        int x = left + 14;
        int y = top + 28;
        graphics.fill(x, y, x + 84, y + 84, 0xFF3B1F0B);
        graphics.fill(x + 2, y + 2, x + 82, y + 82, ORANGE);
        graphics.fill(x + 5, y + 5, x + 79, y + 79, 0xFF2A1608);
        if (job == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            renderPlayer(graphics, job, x, y, mouseX, mouseY, minecraft.player);
        } else {
            graphics.pose().pushPose();
            graphics.pose().translate(x + 10, y + 10, 0.0F);
            graphics.pose().scale(4.0F, 4.0F, 1.0F);
            graphics.renderItem(new ItemStack(job.icon()), 0, 0);
            graphics.pose().popPose();
        }

        fittedText(graphics, Component.literal(job.name()), left + 14, top + 116, 90, TEXT);
        if (job.joined()) {
            fittedText(graphics, Component.translatable("screen.ultimatemodadditions.job_level", job.level())
                    .append(job.title().isEmpty() ? "" : " [" + job.title() + "]"), left + 14, top + 128, 90, AQUA);
            boolean max = job.level() >= job.maxLevel();
            double ratio = max ? 1.0D : Math.min(1.0D, job.xp() / Math.max(1.0D, job.xpNeeded()));
            inset(graphics, left + 14, top + 141, 84, 8);
            graphics.fill(left + 15, top + 142, left + 15 + (int) (82 * ratio), top + 148, max ? 0xFFE0A800 : 0xFF3FA845);
            Component xp = max ? Component.translatable("screen.ultimatemodadditions.job_max")
                    : Component.literal(Math.round(job.xp()) + "/" + Math.round(job.xpNeeded()));
            fittedText(graphics, xp, left + 14, top + 152, 90, GRAY);
        } else {
            List<FormattedCharSequence> lines = font.split(Component.literal(job.description()), 90);
            for (int i = 0; i < Math.min(4, lines.size()); i++) {
                graphics.drawString(font, lines.get(i), left + 14, top + 128 + i * 9, GRAY, false);
            }
        }
    }

    private static ItemStack dyed(Item item, int color) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        return stack;
    }

    private void renderPlayer(GuiGraphics graphics, JobView job, int x, int y, int mouseX, int mouseY, Player player) {
        int color = COLORS.getOrDefault(job.id().getPath(), 0xE0E0E0);
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
                EquipmentSlot.MAINHAND};
        ItemStack[] saved = new ItemStack[slots.length];
        for (int i = 0; i < slots.length; i++) {
            saved[i] = player.getItemBySlot(slots[i]).copy();
        }
        try {
            player.setItemSlot(EquipmentSlot.HEAD, dyed(Items.LEATHER_HELMET, color));
            player.setItemSlot(EquipmentSlot.CHEST, dyed(Items.LEATHER_CHESTPLATE, color));
            player.setItemSlot(EquipmentSlot.LEGS, dyed(Items.LEATHER_LEGGINGS, color));
            player.setItemSlot(EquipmentSlot.FEET, dyed(Items.LEATHER_BOOTS, color));
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(job.icon()));
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x + 5, y + 5, x + 79, y + 79, 33, 0.0625F,
                    mouseX, mouseY, player);
        } finally {
            for (int i = 0; i < slots.length; i++) {
                player.setItemSlot(slots[i], saved[i]);
            }
        }
    }

    private void renderGrid(GuiGraphics graphics) {
        List<JobView> jobs = jobs();
        for (int i = 0; i < PER_PAGE; i++) {
            int x = left + GRID_X + (i % COLUMNS) * SLOT;
            int y = top + GRID_Y + (i / COLUMNS) * SLOT;
            inset(graphics, x, y, SLOT, SLOT);
            int index = page * PER_PAGE + i;
            if (index >= jobs.size()) {
                continue;
            }
            JobView job = jobs.get(index);
            graphics.renderItem(new ItemStack(job.icon()), x + 2, y + 2);
            if (job.joined()) {
                graphics.fill(x + SLOT - 6, y + 2, x + SLOT - 2, y + 6, 0xFF3FD060);
                graphics.renderOutline(x + SLOT - 6, y + 2, 4, 4, OUTLINE);
            }
            if (job.id().equals(selection)) {
                graphics.renderOutline(x - 1, y - 1, SLOT + 2, SLOT + 2, ORANGE);
                graphics.renderOutline(x, y, SLOT, SLOT, ORANGE);
            }
        }
    }

    private void row(GuiGraphics graphics, int y, int index) {
        graphics.fill(left + LIST_X, y, left + LIST_X + LIST_WIDTH, y + ROW, index % 2 == 0 ? ROW_A : ROW_B);
    }

    private void renderActions(GuiGraphics graphics, JobView job) {
        inset(graphics, left + LIST_X - 1, top + LIST_Y - 1, LIST_WIDTH + 2, ROW * LIST_ROWS + 2);
        List<ActionView> actions = job.actions();
        for (int i = 0; i < LIST_ROWS && scroll + i < actions.size(); i++) {
            ActionView action = actions.get(scroll + i);
            int y = top + LIST_Y + i * ROW;
            row(graphics, y, i);
            MutableComponent name = Component.translatable("screen.ultimatemodadditions.job_action_"
                    + action.type().getSerializedName()).append(" ").append(action.label());
            fittedText(graphics, name, left + LIST_X + 4, y + 3, 138, TEXT);
            Component pay = Money.format(Math.round(action.pay() * Money.toCents(1L)));
            plainText(graphics, pay, left + LIST_X + LIST_WIDTH - 4 - font.width(pay), y + 3, GREEN);
        }
        scrollbar(graphics, actions.size());
    }

    private record Line(int level, Component text, int color) {
    }

    private List<Line> rewardLines(JobView job) {
        List<Line> lines = new ArrayList<>();
        for (TitleView title : job.titles()) {
            lines.add(new Line(title.level(), Component.translatable("screen.ultimatemodadditions.job_reward_title",
                    title.level(), title.name()), AQUA));
        }
        for (RewardView reward : job.rewards()) {
            List<String> parts = new ArrayList<>();
            if (reward.money() > 0.0D) {
                parts.add(Money.format(Math.round(reward.money() * Money.toCents(1L))).getString());
            }
            for (ItemStack stack : reward.items()) {
                parts.add(stack.getCount() + "x " + stack.getHoverName().getString());
            }
            String text = String.join(" + ", parts);
            if (reward.level() > 0) {
                lines.add(new Line(reward.level(), Component.translatable("screen.ultimatemodadditions.job_reward_level",
                        reward.level(), text), job.joined() && job.level() >= reward.level() ? GREEN : TEXT));
            }
            if (reward.every() > 0) {
                lines.add(new Line(10_000_000 + reward.every(), Component.translatable(
                        "screen.ultimatemodadditions.job_reward_every", reward.every(), text), GOLD));
            }
        }
        lines.sort(Comparator.comparingInt(Line::level));
        return lines;
    }

    private void renderRewards(GuiGraphics graphics, JobView job) {
        inset(graphics, left + LIST_X - 1, top + LIST_Y - 1, LIST_WIDTH + 2, ROW * LIST_ROWS + 2);
        List<Line> lines = rewardLines(job);
        for (int i = 0; i < LIST_ROWS && scroll + i < lines.size(); i++) {
            Line line = lines.get(scroll + i);
            int y = top + LIST_Y + i * ROW;
            row(graphics, y, i);
            fittedText(graphics, line.text(), left + LIST_X + 4, y + 3, LIST_WIDTH - 12, line.color());
        }
        scrollbar(graphics, lines.size());
    }

    private void scrollbar(GuiGraphics graphics, int total) {
        int trackLeft = left + LIST_X + LIST_WIDTH + 3;
        int trackHeight = LIST_ROWS * ROW;
        inset(graphics, trackLeft, top + LIST_Y, 5, trackHeight);
        int overflow = Math.max(0, total - LIST_ROWS);
        int handle = overflow == 0 ? trackHeight - 2 : Math.max(10, (trackHeight - 2) * LIST_ROWS / total);
        int travel = trackHeight - 2 - handle;
        int handleTop = top + LIST_Y + 1 + (overflow == 0 ? 0 : travel * scroll / overflow);
        graphics.fill(trackLeft + 1, handleTop, trackLeft + 4, handleTop + handle, PANEL);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int index = jobAt(mouseX, mouseY);
        if (index < 0) {
            return;
        }
        JobView job = jobs().get(index);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(job.name()).withStyle(ChatFormatting.YELLOW));
        if (job.joined()) {
            lines.add(Component.translatable("screen.ultimatemodadditions.job_level", job.level())
                    .withStyle(ChatFormatting.AQUA));
        }
        lines.add(Component.literal(job.description()).withStyle(ChatFormatting.GRAY));
        graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }
}
