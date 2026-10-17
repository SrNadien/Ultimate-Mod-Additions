package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.data.JobAction;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class UMAJobsProvider extends UMAJsonProvider {

    private static final int[] TITLE_LEVELS = {1, 10, 25, 50, 75, 100};
    private static final double LEVEL_PAY_START = 15.0D;
    private static final double LEVEL_PAY_GROWTH = 1.04D;

    private static final String[] ARMORER = {
            "craft|minecraft:diamond_chestplate|22.0|16.0|",
            "craft|minecraft:diamond_leggings|19.0|14.0|",
            "craft|minecraft:diamond_helmet|14.0|10.0|",
            "craft|minecraft:diamond_boots|11.0|8.0|",
            "craft|minecraft:iron_chestplate|5.0|4.0|",
            "craft|minecraft:iron_leggings|4.5|3.5|",
            "craft|minecraft:iron_helmet|3.0|2.5|",
            "craft|minecraft:iron_boots|2.5|2.0|",
            "craft|minecraft:shield|2.0|2.0|"
    };

    private static final String[] BREEDER = {
            "tame||4.0|4.0|",
            "breed||1.2|1.8|"
    };

    private static final String[] BREWER = {
            "brew||2.5|3.0|"
    };

    private static final String[] BUILDER = {
            "place_block|#minecraft:planks|0.06|0.2|",
            "place_block|#minecraft:stone_bricks|0.08|0.25|",
            "place_block|minecraft:bricks|0.1|0.3|",
            "place_block|minecraft:glass|0.08|0.25|",
            "place_block||0.04|0.15|"
    };

    private static final String[] COOK = {
            "craft|minecraft:cake|4.0|4.0|",
            "craft|minecraft:pumpkin_pie|1.5|1.5|",
            "craft|minecraft:bread|0.5|0.6|",
            "craft|minecraft:cookie|0.2|0.3|",
            "smelt|minecraft:cooked_beef|0.4|0.5|",
            "smelt|minecraft:cooked_porkchop|0.4|0.5|",
            "smelt|minecraft:cooked_chicken|0.3|0.4|",
            "smelt|minecraft:baked_potato|0.2|0.3|"
    };

    private static final String[] CRAFTER = {
            "craft|minecraft:iron_pickaxe|3.0|3.0|",
            "craft|minecraft:iron_sword|3.0|3.0|",
            "craft|minecraft:diamond_pickaxe|12.0|10.0|",
            "craft|minecraft:diamond_sword|12.0|10.0|",
            "craft|minecraft:bread|0.4|0.5|",
            "craft|minecraft:torch|0.1|0.2|",
            "craft||0.15|0.3|"
    };

    private static final String[] DIGGER = {
            "break_block|minecraft:clay|0.5|0.7|",
            "break_block|#minecraft:sand|0.2|0.3|",
            "break_block|minecraft:gravel|0.2|0.3|",
            "break_block|minecraft:dirt|0.1|0.2|",
            "break_block|minecraft:grass_block|0.1|0.2|",
            "break_block|minecraft:soul_sand|0.4|0.5|"
    };

    private static final String[] ENCHANTER = {
            "enchant||9.0|7.0|"
    };

    private static final String[] EXPLORER = {
            "explore||0.8|1.5|"
    };

    private static final String[] FARMER = {
            "break_block|minecraft:melon|1.0|1.5|",
            "break_block|minecraft:pumpkin|1.0|1.5|",
            "break_block|minecraft:nether_wart|1.0|1.5|grown",
            "break_block|#minecraft:crops|0.75|1.0|grown"
    };

    private static final String[] FISHER = {
            "fish|#minecraft:fishes|1.0|1.5|",
            "fish||5.0|4.0|"
    };

    private static final String[] HUNTER = {
            "kill_entity|minecraft:ender_dragon|5000.0|1500.0|",
            "kill_entity|minecraft:wither|5000.0|1500.0|",
            "kill_entity|minecraft:warden|3000.0|1000.0|",
            "kill_entity|minecraft:elder_guardian|500.0|200.0|",
            "kill_entity|minecraft:wither_skeleton|8.0|6.0|",
            "kill_entity|minecraft:blaze|5.0|4.0|",
            "kill_entity|minecraft:witch|5.0|4.0|",
            "kill_entity|minecraft:enderman|5.0|4.0|",
            "kill_entity|minecraft:creeper|3.0|3.0|",
            "kill_entity|minecraft:zombie|2.0|2.0|",
            "kill_entity|minecraft:skeleton|2.0|2.0|",
            "kill_entity|minecraft:spider|2.0|2.0|"
    };

    private static final String[] MINER = {
            "break_block|#c:ores/netherite_scrap|150.0|60.0|",
            "break_block|#c:ores/diamond|50.0|25.0|",
            "break_block|#c:ores/emerald|50.0|25.0|",
            "break_block|#c:ores/gold|10.0|8.0|",
            "break_block|#c:ores/iron|5.0|5.0|",
            "break_block|#c:ores/lapis|5.0|5.0|",
            "break_block|#c:ores/redstone|3.0|4.0|",
            "break_block|#c:ores/copper|2.0|3.0|",
            "break_block|#c:ores/coal|2.0|3.0|",
            "break_block|#minecraft:base_stone_overworld|0.1|0.5|"
    };

    private static final String[] NETHERWALKER = {
            "kill_entity|minecraft:wither_skeleton|3.0|3.0|",
            "kill_entity|minecraft:blaze|2.5|2.5|",
            "kill_entity|minecraft:ghast|4.0|4.0|",
            "kill_entity|minecraft:piglin_brute|4.0|4.0|",
            "kill_entity|minecraft:magma_cube|1.0|1.0|",
            "kill_entity|minecraft:hoglin|2.0|2.0|",
            "kill_entity|minecraft:zombified_piglin|1.0|1.0|"
    };

    private static final String[] SMELTER = {
            "smelt|minecraft:netherite_scrap|6.0|6.0|",
            "smelt|minecraft:gold_ingot|0.9|1.2|",
            "smelt|minecraft:iron_ingot|0.6|0.9|",
            "smelt|minecraft:copper_ingot|0.4|0.6|",
            "smelt|minecraft:glass|0.2|0.3|",
            "smelt||0.1|0.2|"
    };

    private static final String[] WEAPONSMITH = {
            "craft|minecraft:diamond_sword|14.0|10.0|",
            "craft|minecraft:diamond_axe|14.0|10.0|",
            "craft|minecraft:iron_sword|3.5|3.0|",
            "craft|minecraft:iron_axe|3.5|3.0|",
            "craft|minecraft:bow|1.5|1.5|",
            "craft|minecraft:crossbow|3.0|2.5|",
            "craft|minecraft:arrow|0.15|0.2|",
            "craft|minecraft:stone_sword|0.4|0.5|"
    };

    private static final String[] WOODCUTTER = {
            "break_block|#minecraft:logs|0.5|1.0|"
    };

    public UMAJobsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, "uma/jobs", "Jobs");
    }

    @Override
    void generate(Emitter emitter) {
        job(emitter, "armorer", "Armadurero", "Cobra por fabricar armaduras.", "minecraft:iron_chestplate", 20000.0, 1.06,
                new String[]{"Aprendiz", "Armadurero", "Oficial armadurero", "Maestro armadurero", "Gran armadurero", "Coloso de acero"}, ARMORER);
        job(emitter, "breeder", "Criador", "Cobra por criar y domar animales.", "minecraft:lead", 20000.0, 1.06,
                new String[]{"Pastor", "Criador", "Ganadero", "Maestro criador", "Gran ganadero", "Señor de las bestias"}, BREEDER);
        job(emitter, "brewer", "Alquimista", "Cobra por preparar pociones.", "minecraft:brewing_stand", 20000.0, 1.06,
                new String[]{"Aprendiz", "Boticario", "Alquimista", "Maestro alquimista", "Gran sabio", "Sabio de las pociones"}, BREWER);
        job(emitter, "builder", "Constructor", "Cobra por colocar bloques.", "minecraft:bricks", 20000.0, 1.06,
                new String[]{"Ayudante", "Albañil", "Oficial albañil", "Constructor", "Maestro de obra", "Arquitecto"}, BUILDER);
        job(emitter, "cook", "Cocinero", "Cobra por cocinar y hornear comida.", "minecraft:cooked_beef", 20000.0, 1.06,
                new String[]{"Pinche", "Cocinero", "Chef de partida", "Maestro cocinero", "Chef ejecutivo", "Chef estrella"}, COOK);
        job(emitter, "crafter", "Artesano", "Cobra por fabricar objetos.", "minecraft:crafting_table", 20000.0, 1.06,
                new String[]{"Aprendiz de taller", "Artesano", "Oficial", "Maestro artesano", "Gran maestro", "Artífice"}, CRAFTER);
        job(emitter, "digger", "Excavador", "Cobra por excavar tierra, arena y grava.", "minecraft:iron_shovel", 20000.0, 1.06,
                new String[]{"Palero", "Excavador", "Cavador", "Maestro excavador", "Gran excavador", "Señor de la tierra"}, DIGGER);
        job(emitter, "enchanter", "Encantador", "Cobra por encantar objetos.", "minecraft:enchanting_table", 20000.0, 1.06,
                new String[]{"Acólito", "Aprendiz de magia", "Hechicero", "Mago", "Archimago", "Gran arcano"}, ENCHANTER);
        job(emitter, "explorer", "Explorador", "Cobra por descubrir chunks nuevos.", "minecraft:compass", 20000.0, 1.06,
                new String[]{"Curioso", "Caminante", "Viajero", "Explorador", "Aventurero", "Cartógrafo legendario"}, EXPLORER);
        job(emitter, "farmer", "Granjero", "Cobra por cosechar cultivos maduros.", "minecraft:wheat", 20000.0, 1.06,
                new String[]{"Peón", "Sembrador", "Chacarero", "Granjero", "Hacendado", "Rey del campo"}, FARMER);
        job(emitter, "fisher", "Pescador", "Cobra por cada cosa que pescas.", "minecraft:fishing_rod", 20000.0, 1.06,
                new String[]{"Pescador de orilla", "Aficionado", "Lanchero", "Pescador", "Capitán", "Lobo de mar"}, FISHER);
        job(emitter, "hunter", "Cazador", "Cobra por abatir monstruos.", "minecraft:iron_sword", 20000.0, 1.06,
                new String[]{"Cazador novato", "Rastreador", "Cazador", "Experto", "Maestro cazador", "Depredador"}, HUNTER);
        job(emitter, "miner", "Minero", "Cobra por picar piedra y minerales.", "minecraft:iron_pickaxe", 20000.0, 1.06,
                new String[]{"Picapedrero", "Minero", "Barretero", "Capataz", "Maestro minero", "Señor de la mina"}, MINER);
        job(emitter, "netherwalker", "Caminante del Nether", "Cobra por matar criaturas del Nether.", "minecraft:blaze_rod", 20000.0, 1.06,
                new String[]{"Visitante", "Caminante", "Superviviente", "Cazador infernal", "Azote del Nether", "Señor del fuego"}, NETHERWALKER);
        job(emitter, "smelter", "Fundidor", "Cobra por fundir minerales y cocinar.", "minecraft:furnace", 20000.0, 1.06,
                new String[]{"Fogonero", "Fundidor", "Herrero", "Maestro fundidor", "Gran herrero", "Señor de la forja"}, SMELTER);
        job(emitter, "weaponsmith", "Armero", "Cobra por fabricar armas.", "minecraft:diamond_sword", 20000.0, 1.06,
                new String[]{"Aprendiz de armero", "Armero", "Oficial armero", "Maestro armero", "Gran armero", "Forjador legendario"}, WEAPONSMITH);
        job(emitter, "woodcutter", "Leñador", "Cobra por talar troncos.", "minecraft:iron_axe", 20000.0, 1.06,
                new String[]{"Hachero", "Leñador", "Talador", "Capataz", "Maestro leñador", "Guardabosques"}, WOODCUTTER);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    private static List<JobDefinition.Reward> rewards() {
        List<JobDefinition.Reward> rewards = new ArrayList<>();
        for (int level = 2; level <= 100; level++) {
            double money = Math.round(LEVEL_PAY_START * Math.pow(LEVEL_PAY_GROWTH, level - 2) * 100.0D) / 100.0D;
            List<JobDefinition.RewardItem> items = switch (level) {
                case 5 -> List.of(new JobDefinition.RewardItem(Items.BREAD, 16));
                case 10 -> List.of(new JobDefinition.RewardItem(Items.GOLDEN_APPLE, 1));
                case 25 -> List.of(new JobDefinition.RewardItem(Items.DIAMOND, 4));
                case 50 -> List.of(new JobDefinition.RewardItem(Items.EMERALD, 16));
                case 75 -> List.of(new JobDefinition.RewardItem(Items.NETHERITE_INGOT, 1));
                case 100 -> List.of(new JobDefinition.RewardItem(Items.ENCHANTED_GOLDEN_APPLE, 2));
                default -> List.of();
            };
            rewards.add(new JobDefinition.Reward(level, 0, money, items, List.of()));
        }
        return rewards;
    }

    private static void job(Emitter emitter, String file, String name, String description, String icon, double xpBase,
                            double xpGrowth, String[] titles, String[] rows) {
        List<JobDefinition.Title> titleList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            titleList.add(new JobDefinition.Title(TITLE_LEVELS[i], titles[i]));
        }
        List<JobAction> actions = new ArrayList<>();
        for (String row : rows) {
            String[] part = row.split("\\|", -1);
            actions.add(new JobAction(JobAction.Type.valueOf(part[0].toUpperCase(Locale.ROOT)), part[1],
                    part[4].equals("grown"), Double.parseDouble(part[2]), Double.parseDouble(part[3])));
        }
        emitter.accept(file, JobDefinition.CODEC, new JobDefinition(name, description, item(icon), 100, xpBase,
                xpGrowth, 0.0D, titleList, rewards(), actions));
    }
}
