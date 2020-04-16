package com.oop.testingPlugin;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.BiomeBase.BiomeMeta;
/*
 * An Utility class for net.minecraft.server related operations, mostly mobs and items.
 * NO COPYRIGHT. Use it however you feel like. You can claim it as your own, you can modify it,
 * you can share it, you can use it, with no permission whatsoever, I don't mind :)
 * @author jetp250
 */
public final class NmsUtils {

    private static final Field META_LIST_MONSTER;
    private static final Field META_LIST_CREATURE;
    private static final Field META_LIST_AMBIENT;
    private static final Field META_LIST_WATER_CREATURE;
    /**
     * a CraftBukkit server field to reduce continuous casting from
     * {@link Bukkit#getServer()}.
     */
    public static final CraftServer SERVER;

    private static boolean accessible;

    private static boolean isAccessible() {
        return accessible;
    }

    private static void init() {
        accessible = !(META_LIST_MONSTER == null || META_LIST_CREATURE == null || META_LIST_AMBIENT == null
                || META_LIST_WATER_CREATURE == null);
    }

    /**
     * Registers an Item (Not an ItemStack!) to be available for use. an ItemStack can then be created using <code>new ItemStack(item)</code>.
     * @param name - The name of the item, can be anything
     * @param id - The ID of the item, will be rendered depending on this
     * @param item - The net.minecraft.server.version.Item itself
     */
    public static void registerItem(final String name, final int id, final Item item) {
        final MinecraftKey key = new MinecraftKey(name);
        Item.REGISTRY.a(id, key, item);
    }

    /**
     * Adds a random spawn for the mob with the specified arguments.
     * <p>
     * If you're using a custom entity class, remember to <b>register</b> it
     * before using this! Otherwise it'll not be rendered by the client.
     * <p>
     * If {@link #isAccessible()} returns false, the process will not be
     * executed.
     *
     * @param type
     *            - The mob type to spawn
     * @param data
     *            - The spawn data (chance, amount), cannot be null!
     * @param biomes
     *            - The array of biomes to let the mobs spawn in, use Biome.ALL
     *            for all.
     */
    public static void addRandomSpawn(final EntityType type, final SpawnData data, final Biome... biomes) {
        if (!isAccessible() || type.isSpecial()) {
            return;
        }
        final Field field;
        if ((field = type.getMeta().getField()) == null) {
            return;
        }
        try {
            field.setAccessible(true);
            for (Object base : BiomeBase.i) {
                for (Biome biome : biomes) {
                    if (biome != Biome.ALL && base.getClass() != biome.getNMSClass()) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    final List<BiomeMeta> list = (List<BiomeMeta>) field.get(base);
                    list.add(data);
                    field.set(base, list);
                }
            }
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers the custom class to be available for use.
     *
     * @param type
     *            - The type of your mob
     * @param customClass
     *            - Your custom class that'll be used
     * @param biomes
     *            - Should your mob be set as a default in each biome? Only one
     *            custom entity of this type entity can have this set as 'true'.
     */
    public static void registerEntity(final EntityType type, final Class<? extends Entity> customClass,
                                      boolean biomes) {
        registerEntity(type.getName(), type, customClass, biomes);
    }

    /**
     * Registers the custom class to be available for use.
     *
     * @param name
     *            - The 'savegame id' of the mob.
     * @param type
     *            - The type of your mob
     * @param customClass
     *            - Your custom class that'll be used
     * @param biomes
     *            - Should your mob be set as a default in each biome? Only one
     *            custom entity of this type entity can have this set as 'true'.
     * @see #registerEntity(int, String, MobType, Class, Biome[])
     * @see EntityType#getName() EntityType#getName() for the savegame id.
     */
    @SuppressWarnings("unchecked")
    public static void registerEntity(final String name, final EntityType type,
                                      final Class<? extends Entity> customClass, boolean biomes) {
        final MinecraftKey key = new MinecraftKey(name);
        EntityTypes.b.a(type.getId(), key, customClass);
        if (!EntityTypes.d.contains(key)) {
            EntityTypes.d.add(key);
        }
        if (!isAccessible() || !biomes || type.isSpecial()) {
            return;
        }
        final Field field;
        if ((field = type.getMeta().getField()) == null) {
            return;
        }
        try {
            field.setAccessible(true);
            for (Object base : BiomeBase.i) {
                final List<BiomeMeta> list = (List<BiomeMeta>) field.get(base);
                for (BiomeMeta meta : list) {
                    if (meta.b == type.getNMSClass()) {
                        meta.b = (Class<? extends EntityInsentient>) customClass;
                        break;
                    }
                }
            }
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers the custom class to be available for use.
     *
     * @param id
     *            - The mob id. BE CAREFUL with this. Your Minecraft client
     *            renders the entity based on this, and if used improperly, will
     *            cause unexpected behavior!
     * @param name
     *            - The 'savegame id' of the mob.
     * @param type
     *            - The type of your mob
     * @param customClass
     *            - Your custom class that'll be used
     * @param biomes
     *            - The array of biomes to make the mob spawn in.
     * @see #registerEntity(int, String, MobType, Class, Biome[])
     * @see EntityType#getName() EntityType#getName() for the savegame id.
     * @see EntityType#getId() EntityType#getId() for the correct mob id.
     */
    @SuppressWarnings("unchecked")
    public static void registerEntity(final int id, final String name, final MobType type,
                                      final Class<? extends Entity> customClass, final Biome... biomes) {
        final MinecraftKey key = new MinecraftKey(name);
        EntityTypes.b.a(id, key, customClass);
        if (!EntityTypes.d.contains(key)) {
            EntityTypes.d.add(key);
        }
        if (!isAccessible() || biomes.length == 0 || type.isSpecial()) {
            return;
        }
        final Field field;
        if ((field = type.getMeta().getField()) == null) {
            return;
        }
        try {
            field.setAccessible(true);
            for (Object base : BiomeBase.i) {
                final List<BiomeMeta> list = (List<BiomeMeta>) field.get(base);
                for (Biome biome : biomes) {
                    if (biome.getNMSClass() != base.getClass()) {
                        continue;
                    }
                    for (BiomeMeta meta : list) {
                        if (meta.b == type.getNMSClass()) {
                            meta.b = (Class<? extends EntityInsentient>) customClass;
                            break;
                        }
                    }
                }
            }
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum MobType implements EntityType {

        ELDER_GUARDIAN(4, "elder_guardian", EntityGuardianElder.class, MobMeta.MONSTER),
        WITHER_SKELETON(5, "wither_skeleton", EntitySkeletonWither.class, MobMeta.MONSTER),
        STRAY(6, "stray", EntitySkeletonStray.class, MobMeta.MONSTER),
        HUSK(23, "husk", EntityZombieHusk.class, MobMeta.MONSTER),
        ZOMBIE_VILLAGER(27, "zombie_villager", EntityZombieVillager.class, MobMeta.MONSTER),
        EVOKER(34, "evocation_illager", EntityEvoker.class, MobMeta.MONSTER),
        VEX(35, "vex", EntityVex.class, MobMeta.MONSTER),
        VINDICATOR(36, "vindication_illager", EntityVindicator.class, MobMeta.MONSTER),
        CREEPER(50, "creeper", EntityCreeper.class, MobMeta.MONSTER),
        SKELETON(51, "skeleton", EntitySkeleton.class, MobMeta.MONSTER),
        SPIDER(52, "spider", EntitySpider.class, MobMeta.MONSTER),
        GIANT(53, "giant", EntityGiantZombie.class, MobMeta.MONSTER),
        ZOMBIE(54, "zombie", EntityZombie.class, MobMeta.MONSTER),
        SLIME(55, "slime", EntitySlime.class, MobMeta.MONSTER),
        GHAST(56, "ghast", EntityGhast.class, MobMeta.MONSTER),
        ZOMBIE_PIGMAN(57, "zombie_pigman", EntityPigZombie.class, MobMeta.MONSTER),
        ENDERMAN(58, "enderman", EntityEnderman.class, MobMeta.MONSTER),
        CAVE_SPIDER(59, "cave_spider", EntityCaveSpider.class, MobMeta.MONSTER),
        SILVERFISH(60, "silverfish", EntitySilverfish.class, MobMeta.MONSTER),
        BLAZE(61, "blaze", EntityBlaze.class, MobMeta.MONSTER),
        MAGMACUBE(62, "magma_cube", EntityMagmaCube.class, MobMeta.MONSTER),
        ENDER_DRAGON(63, "ender_dragon", EntityEnderDragon.class, MobMeta.MONSTER),
        WITHER(64, "wither", EntityWither.class, MobMeta.MONSTER),
        WITCH(66, "witch", EntityWitch.class, MobMeta.MONSTER),
        ENDERMITE(67, "endermite", EntityEndermite.class, MobMeta.MONSTER),
        GUARDIAN(68, "guardian", EntityGuardian.class, MobMeta.MONSTER),
        SHULKER(69, "shulker", EntityShulker.class, MobMeta.MONSTER),
        SKELETON_HORSE(28, "skeleton_horse", EntityHorseSkeleton.class, MobMeta.CREATURE),
        ZOMBIE_HORSE(29, "zombie_horse", EntityHorseZombie.class, MobMeta.CREATURE),
        DONKEY(31, "donkey", EntityHorseDonkey.class, MobMeta.CREATURE),
        MULE(32, "mule", EntityHorseMule.class, MobMeta.CREATURE),
        BAT(65, "bat", EntityBat.class, MobMeta.AMBIENT),
        PIG(90, "pig", EntityPig.class, MobMeta.CREATURE),
        SHEEP(91, "sheep", EntitySheep.class, MobMeta.CREATURE),
        COW(92, "cow", EntityCow.class, MobMeta.CREATURE),
        CHICKEN(93, "chicken", EntityChicken.class, MobMeta.CREATURE),
        SQUID(94, "squid", EntitySquid.class, MobMeta.WATER_CREATURE),
        WOLF(95, "wolf", EntityWolf.class, MobMeta.CREATURE),
        MOOSHROOM(96, "mooshroom", EntityMushroomCow.class, MobMeta.CREATURE),
        SNOWMAN(97, "snowman", EntitySnowman.class, MobMeta.CREATURE),
        OCELOT(98, "ocelot", EntityOcelot.class, MobMeta.CREATURE),
        IRON_GOLEM(99, "villager_golem", EntityIronGolem.class, MobMeta.CREATURE),
        HORSE(100, "horse", EntityHorse.class, MobMeta.CREATURE),
        RABBIT(101, "rabbit", EntityRabbit.class, MobMeta.CREATURE),
        POLARBEAR(102, "polar_bear", EntityPolarBear.class, MobMeta.CREATURE),
        LLAMA(103, "llama", EntityLlama.class, MobMeta.CREATURE),
        VILLAGER(120, "villager", EntityVillager.class, MobMeta.CREATURE);

        private final int id;
        private final String name;
        private final Class<? extends EntityInsentient> clazz;
        private final MobMeta meta;

        private MobType(final int id, final String name, Class<? extends EntityInsentient> nmsClazz,
                        final MobMeta meta) {
            this.id = id;
            this.name = name;
            this.clazz = nmsClazz;
            this.meta = meta;
        }

        @Override
        public MobMeta getMeta() {
            return meta;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<? extends EntityInsentient> getNMSClass() {
            return clazz;
        }

        @Override
        public boolean isSpecial() {
            return false;
        }
    }

    public enum SpecialEntities implements EntityType {
        DROPPED_ITEM(1, "item", EntityItem.class),
        EXPERIENCE_ORB(2, "xp_orb", EntityExperienceOrb.class),
        AREA_EFFECT_CLOUD(3, "area_effect_cloud", EntityAreaEffectCloud.class),
        LEAD_KNOT(8, "leash_knot", EntityLeash.class),
        PAINTING(9, "painting", EntityPainting.class),
        ITEM_FRAME(18, "item_frame", EntityItemFrame.class),
        ARMOR_STAND(30, "armor_stand", EntityArmorStand.class),
        EVOCATION_FANGS(33, "evocation_fangs", EntityEvokerFangs.class),
        ENDER_CRYSTAL(200, "ender_crystal", EntityEnderCrystal.class),
        THROWN_EGG(7, "egg", EntityEgg.class),
        ARROW(10, "arrow", EntityArrow.class),
        SNOWBALL(11, "snowball", EntitySnowball.class),
        FIREBALL(12, "fireball", EntityFireball.class),
        SMALL_FIREBALL(13, "fireball", EntitySmallFireball.class),
        ENDER_PEARL(14, "ender_pearl", EntityEnderPearl.class),
        EYE_OF_ENDER(15, "eye_of_ender_signal", EntityEnderSignal.class),
        POTION(16, "potion", EntityPotion.class),
        EXP_BOTTLE(17, "xp_bottle", EntityThrownExpBottle.class),
        WITHER_SKULL(19, "wither_skull", EntityWitherSkull.class),
        FIREWORK_ROCKET(22, "fireworks_rocket", EntityFireworks.class),
        SPECTRAL_ARROW(24, "spectral_arrow", EntitySpectralArrow.class),
        SHULKER_BULLET(25, "shulker_bullet", EntityShulkerBullet.class),
        DRAGON_FIREBALL(26, "dragon_fireball", EntityDragonFireball.class),
        LLAMA_SPIT(104, "llama_spit", EntityLlamaSpit.class),
        PRIMED_TNT(20, "tnt", EntityTNTPrimed.class),
        FALLING_BLOCK(21, "falling_block", EntityFallingBlock.class),
        COMMAND_BLOCK_MINECART(40, "commandblock_minecart", EntityMinecartCommandBlock.class),
        BOAT(41, "boat", EntityBoat.class),
        MINECART(42, "minecart", EntityMinecartRideable.class),
        CHEST_MINECART(43, "chest_minecart", EntityMinecartChest.class),
        FURNACE_MINECART(44, "furnace_minecart", EntityMinecartFurnace.class),
        TNT_MINECART(45, "tnt_minecart", EntityMinecartTNT.class),
        HOPPER_MINECART(46, "hopper_minecart", EntityMinecartHopper.class),
        SPAWNER_MINECART(47, "spawner_minecart", EntityMinecartMobSpawner.class);

        private final int id;
        private final String name;
        private final Class<? extends Entity> clazz;

        private SpecialEntities(final int id, final String name, Class<? extends Entity> nmsClazz) {
            this.id = id;
            this.name = name;
            this.clazz = nmsClazz;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<? extends Entity> getNMSClass() {
            return clazz;
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public MobMeta getMeta() {
            return MobMeta.UNDEFINED;
        }
    }

    private interface EntityType {

        /**
         * Returns the mob's network hexadecimal id value. Used to tell the
         * client which mob should be rendered.
         *
         * @return The ID as an int.
         */
        int getId();

        /**
         * Rather than returning its name, this returns the mob's
         * <b>savegame-id</b>. A Custom savegame-id can be provided instead of
         * this, though.
         *
         * @return The savegame-id String.
         */
        String getName();

        /**
         * Returns the NMS class of this EntityType. Used when overriding the
         * mob to spawn as a default in biomes.
         *
         * @return The NMS class your mob should be extending.
         */
        Class<? extends Entity> getNMSClass();

        /**
         * If a mob is special (aka is part of {@link SpecialEntities} enum),
         * this mob type cannot be registered as a default to the biomes.
         *
         * @return True if the mob cannot override default mobs in biomes.
         */
        boolean isSpecial();

        /**
         * Returns the meta containing info about the list field in which the
         * mob will be added to.
         */
        MobMeta getMeta();
    };

    public enum Biome {

        BEACH(BiomeBeach.class),
        EXTREME_HILLS(BiomeBigHills.class),
        DESERT(BiomeDesert.class),
        FOREST(BiomeForest.class),
        FLOWER_FOREST(BiomeForestMutated.class),
        HELL(BiomeHell.class),
        ICE_PLAINS(BiomeIcePlains.class),
        JUNGLE(BiomeJungle.class),
        MESA(BiomeMesa.class),
        MUSHROOM_ISLANDS(BiomeMushrooms.class),
        OCEAN(BiomeOcean.class),
        PLAINS(BiomePlains.class),
        RIVER(BiomeRiver.class),
        SAVANNA(BiomeSavanna.class),
        MUTATED_SAVANNA(BiomeSavannaMutated.class),
        STONE_BEACH(BiomeStoneBeach.class),
        SWAMP(BiomeSwamp.class),
        TAIGA(BiomeTaiga.class),
        THE_END(BiomeTheEnd.class),
        VOID(BiomeVoid.class),
        ALL(null);

        private final Class<? extends BiomeBase> clazz;

        private Biome(final Class<? extends BiomeBase> clazz) {
            this.clazz = clazz;
        }

        /**
         * @return the NMS class behind this Biome, in which the vanilla mob
         *         will be overridden.
         */
        public Class<? extends BiomeBase> getNMSClass() {
            return clazz;
        }

    }

    public enum MobMeta {
        MONSTER(META_LIST_MONSTER),
        CREATURE(META_LIST_CREATURE),
        WATER_CREATURE(META_LIST_WATER_CREATURE),
        AMBIENT(META_LIST_AMBIENT),
        UNDEFINED(null);

        private final Field field;

        private MobMeta(final Field field) {
            this.field = field;
        }

        /**
         * @return the BiomeMeta list field of this entity.
         *         <p>
         *         <b>Undefined will not be accepted and returns null.</b>
         *         </p>
         */
        public Field getField() {
            return field;
        }
    }

    public static enum Attributes {
        MAX_HEALTH("generic.maxHealth", GenericAttributes.maxHealth),
        KNOCKBACK_RESISTANCE("generic.knockbackResistance", GenericAttributes.c),
        MOVEMENT_SPEED("generic.movementSpeed", GenericAttributes.MOVEMENT_SPEED),
        ATTACK_DAMAGE("generic.attackDamage", GenericAttributes.ATTACK_DAMAGE),
        ARMOR("generic.armor", GenericAttributes.g),
        ARMOR_TOUGHNESS("generic.armorToughness", GenericAttributes.h),
        LUCK("generic.luck", GenericAttributes.i),
        FOLLOW_RANGE("generic.followRange", GenericAttributes.FOLLOW_RANGE);

        private final String name;
        private final IAttribute attribute;

        private Attributes(String nmsName, IAttribute nmsAttribute) {
            this.name = nmsName;
            this.attribute = nmsAttribute;
        }

        /**
         * Returns the NMS name of the attribute. For example,
         * <code>MAX_HEALTH</code> returns <code>"generic.maxHealth"</code>, and
         * so on and so forth.
         *
         * @return The name as a String.
         */
        public String getName() {
            return name;
        }

        /**
         * @return the IAttribute value of this type, used in place of
         *         <code>GenericAttributes.g</code> (for Attributes.ARMOR as an
         *         example).
         */
        public IAttribute getValue() {
            return attribute;
        }
    }

    public class NBTTagType {
        public static final int COMPOUND = 10;
        public static final int LIST = 9;
        public static final int STRING = 8;
        public static final int INT_ARRAY = 11;
        public static final int BYTE_ARRAY = 7;
        public static final int DOUBLE = 6;
        public static final int FLOAT = 5;
        public static final int LONG = 4;
        public static final int INT = 3;
        public static final int SHORT = 2;
        public static final int BYTE = 1;
        public static final int BOOLEAN = 1;
        public static final int END = 0;
    }

    public static class SpawnData extends BiomeMeta {

        private final Class<? extends EntityInsentient> customClass;

        /**
         * Creates a new instance of SpawnData, and at the same time, a new
         * instanceof BiomeMeta, used to add random spawns and such.
         *
         * @param customClass
         *            - Your class to spawn
         * @param spawnWeight
         *            - The chance for the mob(s) to spawn.
         * @param minSpawns
         *            - The minimum amount of entities spawned at once.
         * @param maxSpawns
         *            - The maximum amount of entities spawned at once.
         */
        public SpawnData(Class<? extends EntityInsentient> customClass, final int spawnWeight, final int minSpawns,
                         final int maxSpawns) {
            super(customClass, spawnWeight, minSpawns, maxSpawns);
            this.customClass = customClass;
        }

        public Class<? extends EntityInsentient> getCustomClass() {
            return customClass;
        }
    }

    static {
        final Class<BiomeBase> clazz = BiomeBase.class;
        Field monster = null;
        Field creature = null;
        Field water = null;
        Field ambient = null;
        try {
            // These fields may vary depending on your version.
            // The new names can be found under
            // net.minecraft.server.<version>.BiomeBase.class
            monster = clazz.getDeclaredField("u");
            creature = clazz.getDeclaredField("v");
            water = clazz.getDeclaredField("w");
            ambient = clazz.getDeclaredField("x");
        } catch (Exception e) {
        }
        META_LIST_MONSTER = monster;
        META_LIST_CREATURE = creature;
        META_LIST_WATER_CREATURE = water;
        META_LIST_AMBIENT = ambient;
        SERVER = (CraftServer) Bukkit.getServer();
        init();
    }
}