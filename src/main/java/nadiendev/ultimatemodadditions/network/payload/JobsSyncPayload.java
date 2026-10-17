package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.JobAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record JobsSyncPayload(boolean open, int maxJobs, List<JobView> jobs) implements CustomPacketPayload {

    public record ActionView(JobAction.Type type, Component label, double pay) {
    }

    public record TitleView(int level, String name) {
    }

    public record RewardView(int level, int every, double money, List<ItemStack> items) {
    }

    public record JobView(ResourceLocation id, String name, String description, Item icon, int maxLevel,
                          boolean joined, int level, double xp, double xpNeeded, String title, List<TitleView> titles,
                          List<RewardView> rewards, List<ActionView> actions) {
    }

    public static final Type<JobsSyncPayload> TYPE = new Type<>(UMA.id("jobs_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JobsSyncPayload> STREAM_CODEC =
            StreamCodec.of(JobsSyncPayload::write, JobsSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, JobsSyncPayload payload) {
        buffer.writeBoolean(payload.open);
        buffer.writeVarInt(payload.maxJobs);
        buffer.writeVarInt(payload.jobs.size());
        for (JobView job : payload.jobs) {
            buffer.writeResourceLocation(job.id);
            buffer.writeUtf(job.name);
            buffer.writeUtf(job.description, 1024);
            buffer.writeVarInt(BuiltInRegistries.ITEM.getId(job.icon));
            buffer.writeVarInt(job.maxLevel);
            buffer.writeBoolean(job.joined);
            buffer.writeVarInt(job.level);
            buffer.writeDouble(job.xp);
            buffer.writeDouble(job.xpNeeded);
            buffer.writeUtf(job.title);
            buffer.writeVarInt(job.titles.size());
            for (TitleView title : job.titles) {
                buffer.writeVarInt(title.level);
                buffer.writeUtf(title.name);
            }
            buffer.writeVarInt(job.rewards.size());
            for (RewardView reward : job.rewards) {
                buffer.writeVarInt(reward.level);
                buffer.writeVarInt(reward.every);
                buffer.writeDouble(reward.money);
                buffer.writeVarInt(reward.items.size());
                for (ItemStack stack : reward.items) {
                    ItemStack.STREAM_CODEC.encode(buffer, stack);
                }
            }
            buffer.writeVarInt(job.actions.size());
            for (ActionView action : job.actions) {
                buffer.writeEnum(action.type);
                ComponentSerialization.STREAM_CODEC.encode(buffer, action.label);
                buffer.writeDouble(action.pay);
            }
        }
    }

    private static JobsSyncPayload read(RegistryFriendlyByteBuf buffer) {
        boolean open = buffer.readBoolean();
        int maxJobs = buffer.readVarInt();
        int count = buffer.readVarInt();
        List<JobView> jobs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buffer.readResourceLocation();
            String name = buffer.readUtf();
            String description = buffer.readUtf(1024);
            Item icon = BuiltInRegistries.ITEM.byId(buffer.readVarInt());
            int maxLevel = buffer.readVarInt();
            boolean joined = buffer.readBoolean();
            int level = buffer.readVarInt();
            double xp = buffer.readDouble();
            double xpNeeded = buffer.readDouble();
            String title = buffer.readUtf();
            int titleCount = buffer.readVarInt();
            List<TitleView> titles = new ArrayList<>(titleCount);
            for (int j = 0; j < titleCount; j++) {
                titles.add(new TitleView(buffer.readVarInt(), buffer.readUtf()));
            }
            int rewardCount = buffer.readVarInt();
            List<RewardView> rewards = new ArrayList<>(rewardCount);
            for (int j = 0; j < rewardCount; j++) {
                int rewardLevel = buffer.readVarInt();
                int every = buffer.readVarInt();
                double money = buffer.readDouble();
                int itemCount = buffer.readVarInt();
                List<ItemStack> items = new ArrayList<>(itemCount);
                for (int k = 0; k < itemCount; k++) {
                    items.add(ItemStack.STREAM_CODEC.decode(buffer));
                }
                rewards.add(new RewardView(rewardLevel, every, money, items));
            }
            int actionCount = buffer.readVarInt();
            List<ActionView> actions = new ArrayList<>(actionCount);
            for (int j = 0; j < actionCount; j++) {
                JobAction.Type type = buffer.readEnum(JobAction.Type.class);
                Component label = ComponentSerialization.STREAM_CODEC.decode(buffer);
                actions.add(new ActionView(type, label, buffer.readDouble()));
            }
            jobs.add(new JobView(id, name, description, icon, maxLevel, joined, level, xp, xpNeeded, title, titles, rewards,
                    actions));
        }
        return new JobsSyncPayload(open, maxJobs, jobs);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
