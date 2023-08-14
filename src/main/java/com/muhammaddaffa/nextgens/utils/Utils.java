package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.nextgens.NextGens;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static double getPriceValue(Player player, ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null) {
            return 0;
        }
        ItemMeta meta = stack.getItemMeta();
        // check if the item is from generator
        if (meta.getPersistentDataContainer().has(NextGens.drop_value, PersistentDataType.DOUBLE)) {
            // get the drop value
            Double value = meta.getPersistentDataContainer().get(NextGens.drop_value, PersistentDataType.DOUBLE);
            // if value is null, skip it
            if (value == null) {
                return 0;
            }
            return value * stack.getAmount();
        }
        // check if the shopgui+ hook is enabled
        if (Config.CONFIG.getBoolean("sell-options.hook_shopguiplus") && isShopGUIPlus()) {
            // get the price from shopgui+
            return ShopGuiPlusApi.getItemStackPriceSell(player, stack);
        }
        return 0;
    }

    private static boolean isShopGUIPlus() {
        return Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null;
    }

}
