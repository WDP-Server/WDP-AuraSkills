package dev.aurelium.auraskills.bukkit.skillcoins.shop;

import dev.aurelium.auraskills.common.skillcoins.CurrencyType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ShopItem {

    public enum ItemType {
        REGULAR,
        SKILL_LEVEL,
        TOKEN_EXCHANGE,
        SPAWNER
    }

    public enum SpawnerTier {
        BASIC(1.0, ""),
        ENHANCED(1.5, "✦ "),
        HYPER(2.0, "✦✦ "),
        OMEGA(3.0, "✦✦✦ ");

        private final double spawnRateMultiplier;
        private final String prefix;

        SpawnerTier(double spawnRateMultiplier, String prefix) {
            this.spawnRateMultiplier = spawnRateMultiplier;
            this.prefix = prefix;
        }

        public double getSpawnRateMultiplier() {
            return spawnRateMultiplier;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getPriceMultiplier() {
            switch (this) {
                case BASIC: return 1;
                case ENHANCED: return 3;
                case HYPER: return 6;
                case OMEGA: return 12;
                default: return 1;
            }
        }
    }

    private final Material material;
    private final double buyPrice;
    private final double sellPrice;
    private final Map<Enchantment, Integer> enchantments;
    private final ItemType type;
    private final String skillName;
    private final int tokenAmount;
    private final CurrencyType currency;
    private final EntityType spawnerType;
    private final SpawnerTier spawnerTier;
    private final int packSize;

    public ShopItem(Material material, double buyPrice, double sellPrice) {
        this(material, buyPrice, sellPrice, new HashMap<>(), ItemType.REGULAR, null, 0, CurrencyType.COINS,
             null, null, 1);
    }

    public ShopItem(Material material, double buyPrice, double sellPrice, Map<Enchantment, Integer> enchantments) {
        this(material, buyPrice, sellPrice, enchantments, ItemType.REGULAR, null, 0, CurrencyType.COINS,
             null, null, 1);
    }

    public ShopItem(Material material, double buyPrice, double sellPrice, Map<Enchantment, Integer> enchantments,
                    ItemType type, String skillName, int tokenAmount, CurrencyType currency) {
        this(material, buyPrice, sellPrice, enchantments, type, skillName, tokenAmount, currency,
             null, null, 1);
    }

    public ShopItem(Material material, double buyPrice, double sellPrice, Map<Enchantment, Integer> enchantments,
                    ItemType type, String skillName, int tokenAmount, CurrencyType currency,
                    EntityType spawnerType, SpawnerTier spawnerTier, int packSize) {
        this.material = material;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.enchantments = new HashMap<>(enchantments);
        this.type = type;
        this.skillName = skillName;
        this.tokenAmount = tokenAmount;
        this.currency = currency;
        this.spawnerType = spawnerType;
        this.spawnerTier = spawnerTier;
        this.packSize = packSize;
    }

    public Material getMaterial() {
        return material;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return new HashMap<>(enchantments);
    }

    public boolean hasEnchantments() {
        return !enchantments.isEmpty();
    }

    public ItemType getType() {
        return type;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getTokenAmount() {
        return tokenAmount;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public boolean canBuy() {
        return buyPrice >= 0;
    }

    public boolean canSell() {
        return sellPrice >= 0;
    }

    public boolean isSpawner() {
        return type == ItemType.SPAWNER;
    }

    public EntityType getSpawnerType() {
        return spawnerType;
    }

    public SpawnerTier getSpawnerTier() {
        return spawnerTier;
    }

    public int getPackSize() {
        return packSize;
    }

    public ItemStack createItemStack(int amount) {
        if (isSpawner()) {
            return createSpawnerItemStack(amount);
        }

        ItemStack item = new ItemStack(material, amount);

        if (!enchantments.isEmpty()) {
            if (material == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) item.getItemMeta();
                if (storageMeta != null) {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        storageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                    }
                    item.setItemMeta(storageMeta);
                }
            } else {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                    item.setItemMeta(meta);
                }
            }
        }

        return item;
    }

    private String getEnchantmentDisplayName(Enchantment enchantment) {
        String key = enchantment.getKeyOrThrow().getKey();
        return formatEnchantmentName(key);
    }

    private String formatEnchantmentName(String key) {
        return getEnchantmentDisplayNameMap().getOrDefault(key.toUpperCase(), formatSimpleName(key));
    }

    private String formatSimpleName(String key) {
        String[] words = key.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    private java.util.Map<String, String> getEnchantmentDisplayNameMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        // Modern Bukkit keys (lowercase keys uppercased here) and legacy enum-like names
        map.put("PROTECTION_ENVIRONMENTAL", "Protection");
        map.put("PROTECTION", "Protection");
        map.put("PROTECTION_FIRE", "Fire Protection");
        map.put("FIRE_PROTECTION", "Fire Protection");
        map.put("PROTECTION_FALL", "Feather Falling");
        map.put("FEATHER_FALLING", "Feather Falling");
        map.put("PROTECTION_EXPLOSIONS", "Blast Protection");
        map.put("BLAST_PROTECTION", "Blast Protection");
        map.put("PROTECTION_PROJECTILE", "Projectile Protection");
        map.put("PROJECTILE_PROTECTION", "Projectile Protection");

        map.put("OXYGEN", "Respiration");
        map.put("RESPIRATION", "Respiration");
        map.put("WATER_WORKER", "Aqua Affinity");
        map.put("AQUA_AFFINITY", "Aqua Affinity");

        map.put("THORNS", "Thorns");
        map.put("DEPTH_STRIDER", "Depth Strider");
        map.put("DEPTH_STRIDER", "Depth Strider");
        map.put("FROST_WALKER", "Frost Walker");

        map.put("BINDING_CURSE", "Curse of Binding");
        map.put("CURSE_OF_BINDING", "Curse of Binding");
        map.put("VANISHING_CURSE", "Curse of Vanishing");

        map.put("SOUL_SPEED", "Soul Speed");
        map.put("SWIFT_SNEAK", "Swift Sneak");

        // Tools / general
        map.put("DIG_SPEED", "Efficiency");
        map.put("EFFICIENCY", "Efficiency");
        map.put("SILK_TOUCH", "Silk Touch");
        map.put("SILK_TOUCH", "Silk Touch");
        map.put("DURABILITY", "Unbreaking");
        map.put("UNBREAKING", "Unbreaking");

        map.put("LOOT_BONUS_BLOCKS", "Fortune");
        map.put("FORTUNE", "Fortune");
        map.put("LOOT_BONUS_MOBS", "Looting");
        map.put("LOOTING", "Looting");

        // Weapons / bow
        map.put("ARROW_KNOCKBACK", "Punch");
        map.put("PUNCH", "Punch");
        map.put("ARROW_FIRE", "Flame");
        map.put("FLAME", "Flame");
        map.put("ARROW_INFINITE", "Infinity");
        map.put("INFINITY", "Infinity");
        map.put("POWER", "Power");

        // Damage related (legacy enum names -> proper names)
        map.put("DAMAGE_ALL", "Sharpness");
        map.put("SHARPNESS", "Sharpness");
        map.put("DAMAGE_UNDEAD", "Smite");
        map.put("SMITE", "Smite");
        map.put("DAMAGE_ARTHROPODS", "Bane of Arthropods");
        map.put("BANE_OF_ARTHROPODS", "Bane of Arthropods");

        map.put("KNOCKBACK", "Knockback");
        map.put("FIRE_ASPECT", "Fire Aspect");

        map.put("LOOTING", "Looting");
        map.put("SWEEPING_EDGE", "Sweeping Edge");

        // Fishing / trident
        map.put("LUCK_OF_THE_SEA", "Luck of the Sea");
        map.put("LURE", "Lure");
        map.put("LOYALTY", "Loyalty");
        map.put("IMPALING", "Impaling");
        map.put("RIPTIDE", "Riptide");
        map.put("CHANNELING", "Channeling");

        // Crossbow / bow
        map.put("MULTISHOT", "Multishot");
        map.put("PIERCING", "Piercing");
        map.put("QUICK_CHARGE", "Quick Charge");

        // Misc / custom
        map.put("MENDING", "Mending");
        map.put("LUCK", "Luck");
        map.put("ARROW_DAMAGE", "Power");

        // Custom / other
        map.put("DENSITY", "Density");
        map.put("BREACH", "Breach");
        map.put("WIND_BURST", "Wind Burst");
        map.put("VEINMINE", "Veinmine");

        map.put("PROTECTION_BAD_OOMEN", "Bad Omen");
        map.put("RAID_OMEN", "Raid Omen");
        return map;
    }

    private ItemStack createSpawnerItemStack(int amount) {
        ItemStack item = new ItemStack(Material.SPAWNER, amount);

        String entityTypeId = spawnerType != null ? getEntityTypeId(spawnerType) : "pig";

        item = setSpawnerNBT(item, entityTypeId);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName;

            if (spawnerType != null) {
                String entityName = spawnerType.name().replace("_", " ");
                String tierPrefix = spawnerTier != null ? spawnerTier.getPrefix() : "";
                displayName = "§a" + tierPrefix + entityName + " Spawner";
            } else {
                displayName = "§aMonster Spawner";
            }

            meta.setDisplayName(displayName);

            java.util.List<String> lore = new java.util.ArrayList<>();

            if (spawnerType != null && spawnerTier != null) {
                String entityName = spawnerType.name().replace("_", " ");
                lore.add("§7Entity: §f" + entityName);
                lore.add("§7Tier: §f" + spawnerTier.name());

                if (spawnerTier != SpawnerTier.BASIC) {
                    lore.add("§7Spawn Rate: §f" + String.format("%.1fx", spawnerTier.getSpawnRateMultiplier()));
                }
            }

            lore.add("");
            lore.add("§7Place to activate spawner");

            meta.setLore(lore);

            if (spawnerType != null && spawnerTier != null) {
                String tierName = spawnerTier.name();
                String typeName = spawnerType.name();
                meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey("auraskills", "spawner_tier"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    tierName
                );
                meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey("auraskills", "spawner_type"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    typeName
                );
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private String getEntityTypeId(EntityType type) {
        switch (type) {
            case SKELETON: return "skeleton";
            case ZOMBIE: return "zombie";
            case CREEPER: return "creeper";
            case SPIDER: return "spider";
            case CAVE_SPIDER: return "cave_spider";
            case ENDERMAN: return "enderman";
            case SILVERFISH: return "silverfish";
            case BLAZE: return "blaze";
            case MAGMA_CUBE: return "magma_cube";
            case ZOMBIFIED_PIGLIN: return "zombie_pigman";
            case GHAST: return "ghast";
            case SLIME: return "slime";
            case WITCH: return "witch";
            case SHULKER: return "shulker";
            case PHANTOM: return "phantom";
            case DROWNED: return "drowned";
            case HUSK: return "husk";
            case STRAY: return "stray";
            case VEX: return "vex";
            case VINDICATOR: return "vindicator";
            case EVOKER: return "evoker";
            case WITHER_SKELETON: return "wither_skeleton";
            case ZOMBIE_VILLAGER: return "zombie_villager";
            case SKELETON_HORSE: return "skeleton_horse";
            case ZOMBIE_HORSE: return "zombie_horse";
            case DONKEY: return "donkey";
            case MULE: return "mule";
            case LLAMA: return "llama";
            case TRADER_LLAMA: return "trader_llama";
            case PARROT: return "parrot";
            case POLAR_BEAR: return "polar_bear";
            case COW: return "cow";
            case PIG: return "pig";
            case SHEEP: return "sheep";
            case CHICKEN: return "chicken";
            case RABBIT: return "rabbit";
            case BAT: return "bat";
            case CAT: return "cat";
            case WOLF: return "wolf";
            case FOX: return "fox";
            case BEE: return "bee";
            default: return "pig";
        }
    }

    private ItemStack setSpawnerNBT(ItemStack item, String entityTypeId) {
        try {
            String version = getServerVersion();
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            Class<?> nbttagcompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
            Class<?> nbtTagListClass = Class.forName("net.minecraft.nbt.NBTTagList");

            Object nmsItem = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);

            Object blockEntityTag = nbttagcompoundClass.getConstructor().newInstance();

            Object spawnData = nbttagcompoundClass.getConstructor().newInstance();
            spawnData.getClass().getMethod("setString", String.class, String.class).invoke(spawnData, "id", "minecraft:" + entityTypeId);

            Object entityTag = nbttagcompoundClass.getConstructor().newInstance();
            entityTag.getClass().getMethod("setString", String.class, String.class).invoke(entityTag, "id", "minecraft:" + entityTypeId);
            spawnData.getClass().getMethod("set", String.class, Class.forName("net.minecraft.nbt.NBTTag")).invoke(spawnData, "entity", entityTag);

            blockEntityTag.getClass().getMethod("set", String.class, Class.forName("net.minecraft.nbt.NBTTag")).invoke(blockEntityTag, "SpawnData", spawnData);

            double spawnRateMultiplier = spawnerTier != null ? spawnerTier.getSpawnRateMultiplier() : 1.0;
            int baseSpawnCount = 4;
            int adjustedSpawnCount = (int) Math.round(baseSpawnCount * spawnRateMultiplier);
            int baseSpawnRange = 4;
            int adjustedSpawnRange = (int) Math.round(baseSpawnRange * Math.sqrt(spawnRateMultiplier));
            int minDelay = 200;
            int maxDelay = 800;
            int adjustedMinDelay = (int) Math.round(minDelay / spawnRateMultiplier);
            int adjustedMaxDelay = (int) Math.round(maxDelay / spawnRateMultiplier);

            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "SpawnCount", adjustedSpawnCount);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "SpawnRange", adjustedSpawnRange);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "MaxNearbyEntities", adjustedSpawnCount * 6);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "RequiredPlayerRange", 16);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "MinSpawnDelay", adjustedMinDelay);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "MaxSpawnDelay", adjustedMaxDelay);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class).invoke(blockEntityTag, "Delay", adjustedMinDelay);

            Object spawnPotentials = nbtTagListClass.getConstructor().newInstance();
            Object spawnPotential = nbttagcompoundClass.getConstructor().newInstance();
            spawnPotential.getClass().getMethod("setInt", String.class, int.class).invoke(spawnPotential, "Weight", 1);
            spawnPotential.getClass().getMethod("set", String.class, Class.forName("net.minecraft.nbt.NBTTag")).invoke(spawnPotential, "Data", spawnData);
            spawnPotentials.getClass().getMethod("add", Class.forName("net.minecraft.nbt.NBTTag")).invoke(spawnPotentials, spawnPotential);
            blockEntityTag.getClass().getMethod("set", String.class, Class.forName("net.minecraft.nbt.NBTTag")).invoke(blockEntityTag, "SpawnPotentials", spawnPotentials);

            Object itemTag = nmsItem.getClass().getMethod("getTag").invoke(nmsItem);
            if (itemTag == null) {
                itemTag = nbttagcompoundClass.getConstructor().newInstance();
                nmsItem.getClass().getMethod("setTag", nbttagcompoundClass).invoke(nmsItem, itemTag);
            }

            itemTag.getClass().getMethod("set", String.class, Class.forName("net.minecraft.nbt.NBTTag")).invoke(itemTag, "BlockEntityTag", blockEntityTag);

            item = (ItemStack) craftItemStackClass.getMethod("asBukkitCopy", nmsItem.getClass()).invoke(null, nmsItem);

        } catch (Exception e) {
        }
        return item;
    }

    public boolean matches(ItemStack item) {
        if (item == null || item.getType() != material) {
            return false;
        }

        if (isSpawner()) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                return false;
            }

            String displayName = meta.getDisplayName();
            String entityName = spawnerType != null ? spawnerType.name().replace("_", " ") : "";
            String tierPrefix = spawnerTier != null ? spawnerTier.getPrefix() : "";

            String expectedName = "§a" + tierPrefix + entityName + " Spawner";
            if (!displayName.equals(expectedName)) {
                return false;
            }
        }

        if (hasEnchantments()) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return false;
            }

            if (material == Material.ENCHANTED_BOOK) {
                if (!(meta instanceof EnchantmentStorageMeta)) {
                    return false;
                }
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
                Map<Enchantment, Integer> storedEnchants = storageMeta.getStoredEnchants();
                if (storedEnchants.size() != enchantments.size()) {
                    return false;
                }
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Integer itemLevel = storedEnchants.get(entry.getKey());
                    if (itemLevel == null || !itemLevel.equals(entry.getValue())) {
                        return false;
                    }
                }
            } else {
                if (!meta.hasEnchants()) {
                    return false;
                }
                Map<Enchantment, Integer> itemEnchants = meta.getEnchants();
                if (itemEnchants.size() != enchantments.size()) {
                    return false;
                }
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Integer itemLevel = itemEnchants.get(entry.getKey());
                    if (itemLevel == null || !itemLevel.equals(entry.getValue())) {
                        return false;
                    }
                }
            }
        }

            return true;
        }

        private static String toRoman(int num) {
            switch (num) {
                case 1: return "I";
                case 2: return "II";
                case 3: return "III";
                case 4: return "IV";
                case 5: return "V";
                case 6: return "VI";
                case 7: return "VII";
                case 8: return "VIII";
                case 9: return "IX";
                case 10: return "X";
                default: return String.valueOf(num);
            }
        }

        private String getServerVersion() {
            String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            return packageName.substring(packageName.lastIndexOf('.') + 1);
        }
    }
