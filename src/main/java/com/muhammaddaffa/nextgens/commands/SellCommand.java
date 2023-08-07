package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.hooks.vault.VaultEconomy;
import com.muhammaddaffa.nextgens.utils.Common;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.Placeholder;
import com.muhammaddaffa.nextgens.utils.VisualAction;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public record SellCommand(
        GeneratorManager generatorManager
) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            if (!sender.hasPermission("nextgens.sell")) {
                Common.config(sender, "messages.no-permission");
                return true;
            }
            if (!(sender instanceof Player player)) {
                Common.sendMessage(sender, "&cUsage: /sell <player>");
                return true;
            }
            // sell the items
            this.sell(player);
        }

        if (args.length == 1 && sender.hasPermission("nextgens.admin")) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Common.config(sender, "messages.target-not-found");
                return true;
            }
            // sell the items
            this.sell(player);
        }
        return true;
    }

    private void sell(Player player) {
        double totalValue = 0.0;
        int totalItems = 0;
        // loop through inventory contents
        for (ItemStack stack : player.getInventory()) {
            // get value
            double value = this.getPriceValue(player, stack);
            // if the item has value, register it
            if (value > 0) {
                totalItems += stack.getAmount();
                totalValue += value;
                // remove the item
                stack.setAmount(0);
            }
        }
        // check if player has anything to sell
        if (totalItems == 0) {
            // send message
            Common.config(player, "messages.no-sell");
            // play bass sound
            Common.playBassSound(player);
            return;
        }
        // deposit the money
        VaultEconomy.deposit(player, totalValue);
        // send the visual action
        VisualAction.send(player, Config.CONFIG.getConfig(), "sell-options", new Placeholder()
                .add("{amount}", totalItems)
                .add("{value}", Common.digits(totalValue)));
    }

    private double getPriceValue(Player player, ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null || this.generatorManager.isGeneratorItem(stack)) {
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
        if (Config.CONFIG.getBoolean("sell-options.hook_shopguiplus") && this.isShopGUIPlus()) {
            // get the price from shopgui+
            return ShopGuiPlusApi.getItemStackPriceSell(player, stack);
        }
        return 0;
    }

    private boolean isShopGUIPlus() {
        return Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null;
    }

}
