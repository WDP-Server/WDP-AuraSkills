import dev.aurelium.auraskills.bukkit.skillcoins.shop.ShopItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TestEnchantmentFix {
    public static void main(String[] args) {
        // Test with multiple enchantments
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        enchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        enchantments.put(Enchantment.UNBREAKING, 3);
        
        // Create a shop item with multiple enchantments
        ShopItem shopItem = new ShopItem(Material.DIAMOND_SWORD, 1000.0, 500.0, enchantments);
        
        // Create the item stack
        ItemStack itemStack = shopItem.createItemStack(1);
        
        System.out.println("Testing enchantment display fix:");
        System.out.println("Item material: " + itemStack.getType());
        System.out.println("Item display name: " + (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : "None (good!)"));
        
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            System.out.println("Item lore:");
            for (String line : itemStack.getItemMeta().getLore()) {
                System.out.println("  " + line);
            }
        }
        
        // Test with enchanted book
        ShopItem enchantedBook = new ShopItem(Material.ENCHANTED_BOOK, 500.0, 250.0, enchantments);
        ItemStack bookStack = enchantedBook.createItemStack(1);
        
        System.out.println("\nTesting enchanted book:");
        System.out.println("Book material: " + bookStack.getType());
        System.out.println("Book display name: " + (bookStack.hasItemMeta() && bookStack.getItemMeta().hasDisplayName() ? bookStack.getItemMeta().getDisplayName() : "None (good!)"));
        
        if (bookStack.hasItemMeta() && bookStack.getItemMeta().hasLore()) {
            System.out.println("Book lore:");
            for (String line : bookStack.getItemMeta().getLore()) {
                System.out.println("  " + line);
            }
        }
    }
}