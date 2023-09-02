package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.nextgens.NextGens;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Utils {

    private static final FormatBalance formatBalance = new FormatBalance();

    public static String formatBalance(long value) {
        return formatBalance.format(value);
    }

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
        if (Config.getFileConfiguration("config.yml").getBoolean("sell-options.hook_shopguiplus") && isShopGUIPlus()) {
            // get the price from shopgui+
            return ShopGuiPlusApi.getItemStackPriceSell(player, stack);
        }
        return 0;
    }

    public static void bassSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 2.0f);
    }

    private static boolean isShopGUIPlus() {
        return Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null;
    }

}
