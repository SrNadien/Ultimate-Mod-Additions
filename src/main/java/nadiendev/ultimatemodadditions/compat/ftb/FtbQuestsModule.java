package nadiendev.ultimatemodadditions.compat.ftb;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.compat.CompatModule;
import net.neoforged.bus.api.IEventBus;

public class FtbQuestsModule implements CompatModule {

    public static RewardType money;

    @Override
    public void onConstruct(IEventBus modBus) {
        money = RewardTypes.register(UMA.id("money"), MoneyReward::new,
                () -> Icon.getIcon(UMA.MODID + ":item/20_lucas"));
    }
}
