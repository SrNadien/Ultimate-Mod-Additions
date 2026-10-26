package nadiendev.ultimatemodadditions.ban;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class ComponentMatch {

    private ComponentMatch() {
    }

    static boolean matches(DataComponentPatch patch, ItemStack stack) {
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            Object current = stack.get(entry.getKey());
            Optional<?> wanted = entry.getValue();
            if (wanted.isPresent() ? !Objects.equals(current, wanted.get()) : current != null) {
                return false;
            }
        }
        return true;
    }
}
