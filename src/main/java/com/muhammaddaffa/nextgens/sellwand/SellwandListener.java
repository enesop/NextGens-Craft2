package com.muhammaddaffa.nextgens.sellwand;

import com.griefcraft.lwc.LWC;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.nextgens.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.lynuxcraft.deadsilenceiv.advancedchests.AdvancedChestsAPI;
import us.lynuxcraft.deadsilenceiv.advancedchests.AdvancedChestsPlugin;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.gui.page.ChestPage;
import us.lynuxcraft.deadsilenceiv.advancedchests.utils.inventory.InteractiveInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record SellwandListener(
        SellwandManager sellwandManager
) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack stack = event.getItem();
        // if item is not a sellwand, skip
        if (block == null || !this.sellwandManager.isSellwand(stack)) {
            return;
        }
        // cancel the event no matter what
        event.setCancelled(true);
        /**
         * Advanced Chests API Hook
         *
         */
        AdvancedChest<?, ?> advancedChest = AdvancedChestsAPI.getChestManager().getAdvancedChest(block.getLocation());
        if (advancedChest != null) {
            // check if player is the owner of the chest
            if (!advancedChest.getWhoPlaced().equals(player.getUniqueId())) {
                // send a message and do nothing
                Common.configMessage("config.yml", player, "messages.sellwand-failed");
                // bass sound
                Utils.bassSound(player);
                return;
            }
            // get all pages
            List<Inventory> inventories = advancedChest.getPages().values()
                    .stream()
                    .map(InteractiveInventory::getBukkitInventory)
                    .toList();
            // try to sell the content of the chest
            this.sellwandManager.action(player, stack, inventories.toArray(Inventory[]::new));
            return;
        }
        /**
         * Normal Container
         */
        if (block.getState() instanceof Container container) {
            // LWC check
            if (Bukkit.getPluginManager().getPlugin("LWC") != null && !LWC.getInstance().canAccessProtection(player, block)) {
                // send a message and do nothing
                Common.configMessage("config.yml", player, "messages.sellwand-failed");
                // bass sound
                Utils.bassSound(player);
                return;
            }
            // try to sell the content of the chest
            this.sellwandManager.action(player, stack, container.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onLoseDurability(PlayerItemDamageEvent event) {
        if (!this.sellwandManager.isSellwand(event.getItem())) return;
        event.setCancelled(true);
    }

}
