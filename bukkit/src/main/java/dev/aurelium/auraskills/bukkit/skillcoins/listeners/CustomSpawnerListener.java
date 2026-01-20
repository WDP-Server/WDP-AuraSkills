package dev.aurelium.auraskills.bukkit.skillcoins.listeners;

import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.skillcoins.shop.ShopItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

public class CustomSpawnerListener implements Listener {

    private final AuraSkills plugin;
    private static final NamespacedKey SPAWNER_TIER = new NamespacedKey("auraskills", "spawner_tier");
    private static final NamespacedKey SPAWNER_TYPE = new NamespacedKey("auraskills", "spawner_type");

    public CustomSpawnerListener(AuraSkills plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();

        if (block.getType() != Material.SPAWNER) return;
        if (item == null || item.getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            plugin.getLogger().warning("No meta on spawner item placed by " + player.getName());
            return;
        }

        String tierName = meta.getPersistentDataContainer().get(SPAWNER_TIER, PersistentDataType.STRING);
        String typeName = meta.getPersistentDataContainer().get(SPAWNER_TYPE, PersistentDataType.STRING);

        plugin.getLogger().info("Spawner placed by " + player.getName() + " - tier: " + tierName + ", type: " + typeName);

        if (tierName == null || typeName == null) {
            plugin.getLogger().warning("Spawner placed without tier/type data: " + player.getName());
            player.sendMessage(ChatColor.RED + "Warning: Spawner missing tier data!");
            return;
        }

        try {
            ShopItem.SpawnerTier tier = ShopItem.SpawnerTier.valueOf(tierName);
            EntityType entityType = EntityType.valueOf(typeName);

            plugin.getLogger().info("Applying spawner: " + entityType + " with tier " + tier + " (multiplier: " + tier.getSpawnRateMultiplier() + ")");

            applySpawnerData(block, entityType, tier);

            player.sendMessage(ChatColor.of("#55FF55") + "✔ Spawner placed with " +
                ChatColor.of(getTierColor(tier)) + tierName + ChatColor.of("#55FF55") + " tier!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to apply spawner data", e);
            player.sendMessage(ChatColor.RED + "Error applying spawner data!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() != Material.SPAWNER) return;

        plugin.getLogger().info("Spawner break attempt by " + player.getName());

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        EntityType spawnerType = spawner.getSpawnedType();

        plugin.getLogger().info("Spawner type: " + spawnerType);

        boolean hasSilkTouch = player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH) ||
                              player.getInventory().getItemInOffHand().containsEnchantment(Enchantment.SILK_TOUCH);

        if (!hasSilkTouch) {
            player.sendMessage(ChatColor.of("#FF5555") + "✖ Spawners require Silk Touch to harvest!");
            player.sendMessage(ChatColor.of("#808080") + "Use a tool with Silk Touch to break this spawner.");
            event.setCancelled(true);
            return;
        }

        try {
            ShopItem.SpawnerTier tier = getTierFromSpawner(spawner);
            plugin.getLogger().info("Detected tier from spawner: " + tier);

            ItemStack dropItem = createSpawnerItem(spawnerType, tier);

            block.setType(Material.AIR);
            player.getWorld().dropItemNaturally(block.getLocation(), dropItem);

            player.sendMessage(ChatColor.of("#55FF55") + "✔ Spawner harvested!");
            player.sendMessage(ChatColor.of("#808080") + "Received: " +
                ChatColor.of(getTierColor(tier)) + tier.name() + " " +
                ChatColor.of("#00FFFF") + formatEntityName(spawnerType) + ChatColor.of("#808080") + " Spawner");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to harvest spawner", e);
        }
    }

    private void applySpawnerData(Block block, EntityType entityType, ShopItem.SpawnerTier tier) {
        try {
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            spawner.setSpawnedType(entityType);

            double multiplier = tier.getSpawnRateMultiplier();

            spawner.setSpawnCount((int) Math.round(4 * multiplier));
            spawner.setSpawnRange((int) Math.round(4 * Math.sqrt(multiplier)));
            spawner.setMaxNearbyEntities((int) Math.round(24 * multiplier));
            spawner.setRequiredPlayerRange(16);

            int minDelay = (int) Math.round(200 / multiplier);
            int maxDelay = (int) Math.round(800 / multiplier);
            spawner.setMinSpawnDelay(minDelay);
            spawner.setMaxSpawnDelay(maxDelay);

            spawner.update();

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to apply spawner data via API", e);
            applySpawnerDataNMS(block, entityType, tier);
        }
    }

    private void applySpawnerDataNMS(Block block, EntityType entityType, ShopItem.SpawnerTier tier) {
        try {
            String version = getServerVersion();
            Class<?> craftWorldClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftWorld");
            Class<?> tileEntitySpawnerClass = Class.forName("net.minecraft.world.level.block.entity.TileEntitySpawner");
            Class<?> nbttagcompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");

            Object craftWorld = craftWorldClass.cast(block.getWorld());
            Object handle = craftWorldClass.getMethod("getHandle").invoke(craftWorld);

            Object chunk = handle.getClass().getMethod("getChunkAt", int.class, int.class)
                .invoke(handle, block.getX() >> 4, block.getZ() >> 4);

            Object blockPos = Class.forName("net.minecraft.core.BlockPosition")
                .getConstructor(int.class, int.class, int.class)
                .newInstance(block.getX(), block.getY(), block.getZ());

            Object tileEntity = chunk.getClass().getMethod("a", blockPos.getClass(), boolean.class)
                .invoke(chunk, blockPos, false);

            if (tileEntity == null || !tileEntityClass.getName().contains("Spawner")) {
                return;
            }

            double multiplier = tier.getSpawnRateMultiplier();
            String entityId = getEntityTypeId(entityType);

            Object spawnData = nbttagcompoundClass.getConstructor().newInstance();
            spawnData.getClass().getMethod("setString", String.class, String.class)
                .invoke(spawnData, "id", "minecraft:" + entityId);

            Object entityTag = nbttagcompoundClass.getConstructor().newInstance();
            entityTag.getClass().getMethod("setString", String.class, String.class)
                .invoke(entityTag, "id", "minecraft:" + entityId);
            spawnData.getClass().getMethod("set", String.class, nbttagcompoundClass)
                .invoke(spawnData, "entity", entityTag);

            Object blockEntityTag = nbttagcompoundClass.getConstructor().newInstance();
            blockEntityTag.getClass().getMethod("set", String.class, nbttagcompoundClass)
                .invoke(blockEntityTag, "SpawnData", spawnData);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class)
                .invoke(blockEntityTag, "SpawnCount", (int) Math.round(4 * multiplier));
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class)
                .invoke(blockEntityTag, "SpawnRange", (int) Math.round(4 * Math.sqrt(multiplier)));
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class)
                .invoke(blockEntityTag, "MaxNearbyEntities", (int) Math.round(24 * multiplier));
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class)
                .invoke(blockEntityTag, "RequiredPlayerRange", 16);
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class)
                .invoke(blockEntityTag, "MinSpawnDelay", (int) Math.round(200 / multiplier));
            blockEntityTag.getClass().getMethod("setInt", String.class, int.class)
                .invoke(blockEntityTag, "MaxSpawnDelay", (int) Math.round(800 / multiplier));

            tileEntity.getClass().getMethod("a", nbttagcompoundClass).invoke(tileEntity, blockEntityTag);

            block.getState().update();

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed NMS spawner data application", e);
        }
    }

    private ShopItem.SpawnerTier getTierFromSpawner(CreatureSpawner spawner) {
        int spawnCount = spawner.getSpawnCount();
        if (spawnCount >= 12) return ShopItem.SpawnerTier.OMEGA;
        if (spawnCount >= 8) return ShopItem.SpawnerTier.HYPER;
        if (spawnCount >= 6) return ShopItem.SpawnerTier.ENHANCED;
        return ShopItem.SpawnerTier.BASIC;
    }

    private ItemStack createSpawnerItem(EntityType entityType, ShopItem.SpawnerTier tier) {
        ItemStack item = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String tierPrefix = tier.getPrefix();
            String entityName = entityType.name().replace("_", " ");
            String tierColor = getTierColor(tier);

            meta.setDisplayName(ChatColor.of(tierColor) + tierPrefix + entityName + " Spawner");

            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("");
            lore.add(ChatColor.of("#808080") + "Entity: " + ChatColor.of("#00FFFF") + entityName);
            lore.add(ChatColor.of("#808080") + "Tier: " + ChatColor.of(tierColor) + tier.name());
            lore.add(ChatColor.of("#808080") + "Spawn Rate: " + ChatColor.of(tierColor) +
                String.format("%.1fx", tier.getSpawnRateMultiplier()));
            lore.add("");
            lore.add(ChatColor.of("#FFFF00") + "Place to activate spawner");
            lore.add(ChatColor.of("#808080") + "Requires Silk Touch to harvest");

            meta.setLore(lore);

            meta.getPersistentDataContainer().set(SPAWNER_TIER, PersistentDataType.STRING, tier.name());
            meta.getPersistentDataContainer().set(SPAWNER_TYPE, PersistentDataType.STRING, entityType.name());

            item.setItemMeta(meta);
        }

        return item;
    }

    private String getTierColor(ShopItem.SpawnerTier tier) {
        switch (tier) {
            case BASIC: return "#AAAAAA";
            case ENHANCED: return "#55FF55";
            case HYPER: return "#FFAA00";
            case OMEGA: return "#FF5555";
            default: return "#FFFFFF";
        }
    }

    private String formatEntityName(EntityType type) {
        String name = type.name().replace("_", " ");
        String[] words = name.split(" ");
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

    private String getEntityTypeId(EntityType type) {
        switch (type) {
            case CAVE_SPIDER: return "cave_spider";
            case ZOMBIFIED_PIGLIN: return "zombie_pigman";
            case WITHER_SKELETON: return "wither_skeleton";
            case ZOMBIE_VILLAGER: return "zombie_villager";
            case SKELETON_HORSE: return "skeleton_horse";
            case ZOMBIE_HORSE: return "zombie_horse";
            case TRADER_LLAMA: return "trader_llama";
            default: return type.name().toLowerCase();
        }
    }

    private String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    private static Class<?> getTileEntityClass() {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName()
                .substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1);
            return Class.forName("net.minecraft.world.level.block.entity.TileEntitySpawner");
        } catch (Exception e) {
            return null;
        }
    }

    private static final Class<?> tileEntityClass = getTileEntityClass();
}
