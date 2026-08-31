package nadiendev.ultimatemodadditions.compat;

import net.neoforged.fml.ModList;

public final class ModIds {

    public static final String JEI = "jei";
    public static final String KUBEJS = "kubejs";
    public static final String AVARITIA = "avaritia";
    public static final String IRON_FURNACES = "ironfurnaces";
    public static final String ALLTHEMODIUM = "allthemodium";

    private ModIds() {
    }

    public static boolean loaded(String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }

    public static boolean jei() {
        return loaded(JEI);
    }

    public static boolean kubeJs() {
        return loaded(KUBEJS);
    }


    public static boolean avaritia() {
        return loaded(AVARITIA);
    }

    public static boolean ironFurnaces() {
        return loaded(IRON_FURNACES);
    }

    public static boolean allTheModium() {
        return loaded(ALLTHEMODIUM);
    }
}
