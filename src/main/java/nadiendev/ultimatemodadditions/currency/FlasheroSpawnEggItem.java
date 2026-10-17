package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.registry.UMAVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class FlasheroSpawnEggItem extends Item {

    public FlasheroSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Villager villager = EntityType.VILLAGER.spawn(serverLevel, context.getItemInHand(), context.getPlayer(), pos,
                MobSpawnType.SPAWN_EGG, true, false);
        if (villager == null) {
            return InteractionResult.FAIL;
        }
        villager.setVillagerData(villager.getVillagerData().setProfession(UMAVillagers.FLASHERO.get()).setLevel(1));
        villager.setVillagerXp(1);
        villager.setPersistenceRequired();
        context.getItemInHand().consume(1, context.getPlayer());
        serverLevel.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
        return InteractionResult.CONSUME;
    }
}
