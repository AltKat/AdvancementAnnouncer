package io.github.altkat.advancementannouncer.Handlers;

import io.github.altkat.advancementannouncer.AdvancementAnnouncer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Resolves CustomModelData from various sources, including direct integers
 * or external plugins like ItemsAdder and Nexo, using reflection for soft dependency.
 */
public class CustomModelDataResolver {

    private final AdvancementAnnouncer plugin;
    private boolean itemsAdderAvailable = false;
    private boolean nexoAvailable = false;
    private final int serverVersion;

    public CustomModelDataResolver(AdvancementAnnouncer plugin) {
        this.plugin = plugin;
        this.serverVersion = plugin.getVersion();
    }

    /**
     * Checks for external plugins.
     */
    public void detectExternalPlugins() {
        itemsAdderAvailable = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
        nexoAvailable = Bukkit.getPluginManager().getPlugin("Nexo") != null;

        if (itemsAdderAvailable) {
            plugin.getLogger().info("ItemsAdder detected - CustomModelData integration enabled.");
        }
        if (nexoAvailable) {
            plugin.getLogger().info("Nexo detected - CustomModelData integration enabled.");
        }
    }

    /**
     * Resolves the input string into a CustomModelData value.
     * @param input The string from the config (e.g., "12345", "itemsadder:my_item")
     * @param itemId A name for logging purposes (e.g., the material name)
     * @return ResolvedIconData object, or null if invalid.
     */
    public ResolvedIconData resolve(String input, String itemId) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String trimmed = input.trim();

        // 1. Try to parse as a direct integer
        try {
            int value = Integer.parseInt(trimmed);
            if (value < 0) {
                plugin.getLogger().warning("[CMD] Invalid custom-model-data for item '" + itemId + "': negative value " + value);
                return null;
            }
            return new ResolvedIconData(value, trimmed, ResolvedIconData.Source.DIRECT);
        } catch (NumberFormatException e) {
            // Not an integer, proceed to check for plugin prefixes
        }

        // 2. Check for plugin prefixes (e.g., "itemsadder:my_item")
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":", 2);
            String pluginName = parts[0].toLowerCase();
            String externalItemId = parts[1];

            switch (pluginName) {
                case "itemsadder":
                    return resolveItemsAdder(externalItemId, trimmed, itemId);
                case "nexo":
                    return resolveNexo(externalItemId, trimmed, itemId);
                default:
                    plugin.getLogger().warning("[CMD] Unknown plugin prefix for item '" + itemId + "': " + pluginName);
                    return null;
            }
        }

        plugin.getLogger().warning("[CMD] Invalid custom-model-data format for item '" + itemId + "': " + trimmed);
        return null;
    }

    private ResolvedIconData resolveItemsAdder(String externalItemId, String rawValue, String buffedItemId) {
        if (!itemsAdderAvailable) {
            plugin.getLogger().warning("[CMD] ItemsAdder format used but plugin not found for item: " + buffedItemId);
            return null;
        }

        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = customStackClass.getMethod("getInstance", String.class)
                    .invoke(null, externalItemId);

            if (customStack == null) {
                plugin.getLogger().warning("[CMD] ItemsAdder item not found: " + externalItemId + " (for item: " + buffedItemId + ")");
                return null;
            }

            Object itemStackObj = customStackClass.getMethod("getItemStack").invoke(customStack);
            ItemStack itemStack = (ItemStack) itemStackObj;
            Integer cmd = extractCustomModelData(itemStack);

            if (cmd != null) {
                return new ResolvedIconData(cmd, rawValue, ResolvedIconData.Source.ITEMSADDER);
            }

            plugin.getLogger().warning("[CMD] ItemsAdder item '" + externalItemId + "' has no custom model data");
            return null;

        } catch (Exception e) {
            plugin.getLogger().warning("[CMD] Failed to resolve ItemsAdder item for '" + buffedItemId + "': " + e.getMessage());
            return null;
        }
    }

    private ResolvedIconData resolveNexo(String externalItemId, String rawValue, String buffedItemId) {
        if (!nexoAvailable) {
            plugin.getLogger().warning("[CMD] Nexo format used but plugin not found for item: " + buffedItemId);
            return null;
        }

        try {
            Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
            Object itemBuilder = nexoItemsClass.getMethod("itemFromId", String.class)
                    .invoke(null, externalItemId);

            if (itemBuilder == null) {
                plugin.getLogger().warning("[CMD] Nexo item not found: " + externalItemId + " (for item: " + buffedItemId + ")");
                return null;
            }

            Object itemStackObj = itemBuilder.getClass().getMethod("build").invoke(itemBuilder);
            ItemStack itemStack = (ItemStack) itemStackObj;
            Integer cmd = extractCustomModelData(itemStack);

            if (cmd != null) {
                return new ResolvedIconData(cmd, rawValue, ResolvedIconData.Source.NEXO);
            }

            plugin.getLogger().warning("[CMD] Nexo item '" + externalItemId + "' has no custom model data");
            return null;

        } catch (Exception e) {
            plugin.getLogger().warning("[CMD] Failed to resolve Nexo item for '" + buffedItemId + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts CustomModelData from an ItemStack, compatible with 1.16.5+
     */
    private Integer extractCustomModelData(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();

        // 1.16.5 API has hasCustomModelData() and getCustomModelData()
        if (meta.hasCustomModelData()) {
            return meta.getCustomModelData();
        }

        // This is sufficient for 1.16.5-1.20.4.
        // The 1.20.5+ (components) reflection is only needed if this code runs
        // on a 1.20.5+ server, but since we are compiling against 1.16.5,
        // we only use the 1.16.5 API methods.

        return null;
    }

    public boolean isItemsAdderAvailable() {
        return itemsAdderAvailable;
    }
    public boolean isNexoAvailable() {
        return nexoAvailable;
    }
}