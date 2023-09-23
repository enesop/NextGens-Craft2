package com.muhammaddaffa.nextgens.worth;

import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.ItemBuilder;
import com.muhammaddaffa.mdlib.utils.Logger;
import com.muhammaddaffa.nextgens.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WorthManager {

    private final Map<Material, Double> materialWorth = new HashMap<>();
    private final Map<ItemStack, Double> itemWorth = new HashMap<>();

    @Nullable
    public Double getMaterialWorth(ItemStack stack) {
        Double price = this.materialWorth.get(stack.getType());
        if (price == null) return null;
        return price * stack.getAmount();
    }

    @Nullable
    public Double getItemWorth(ItemStack stack) {
        Double price = null;
        for (ItemStack item : this.itemWorth.keySet()) {
            if (!Utils.isSimilar(stack, item)) continue;
            price = this.itemWorth.get(item);
            break;
        }
        if (price == null) return null;
        return price * stack.getAmount();
    }

    public void load() {
        // clear the items
        this.materialWorth.clear();
        this.itemWorth.clear();
        // get all variables we want
        FileConfiguration config = Config.getFileConfiguration("worth.yml");
        ConfigurationSection materialSection = config.getConfigurationSection("material-worth");
        ConfigurationSection itemSection = config.getConfigurationSection("custom-item-worth");
        // load the material worth first
        Logger.info("Starting to load material and custom items worth...");
        if (materialSection != null) {
            for (String key : materialSection.getKeys(false)) {
                Material material = Material.matchMaterial(key.toUpperCase());
                // if material is null, continue
                if (material == null) {
                    Logger.warning("There is no material named '" + key + "'");
                    continue;
                }
                double worth = materialSection.getDouble(key);
                // register the material worth
                this.materialWorth.put(material, worth);
            }
        }
        // load the custom items worth
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                ItemBuilder builder = ItemBuilder.fromConfig(config, "custom-item-worth." + key);
                // check if the builder is valid
                if (builder == null) {
                    Logger.warning("Failed to load custom item worth with id '" + key + "'");
                    continue;
                }
                // register the custom item worth
                this.itemWorth.put(builder.build(), itemSection.getDouble(key + ".price"));
            }
        }
        Logger.info("Successfully loaded all material and custom items worth");
    }

}
