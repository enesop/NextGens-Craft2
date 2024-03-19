package com.muhammaddaffa.nextgens.sellwand;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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
import us.lynuxcraft.deadsilenceiv.advancedchests.chest.AdvancedChest;
import us.lynuxcraft.deadsilenceiv.advancedchests.utils.inventory.InteractiveInventory;
import world.bentobox.bentobox.BentoBox;

import java.util.List;
import java.util.Optional;

public record SellwandListener(
        SellwandManager sellwandManager
) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
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
        if (Bukkit.getPluginManager().getPlugin("AdvancedChests") != null) {
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
        }
        /**
         * Normal Container
         */
        if (block.getState() instanceof Container container) {
            // Access check
            if (!this.hasAccess(player, container)) {
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

    private boolean hasAccess(Player player, Container container) {
        // LWC Check
        if (Bukkit.getPluginManager().getPlugin("LWC") != null &&
                !LWC.getInstance().canAccessProtection(player, container.getBlock())) {
            return false;
        }
        // SuperiorSkyblock Check
        if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
            Island playerIsland = superiorPlayer.getIsland();
            Island islandAt = SuperiorSkyblockAPI.getIslandAt(container.getLocation());
            // If island is not found, skip it
            if (playerIsland == null || islandAt == null) {
                return false;
            }
            return playerIsland.getUniqueId().equals(islandAt.getUniqueId());
        }
        // BentoBox Check
        if (Bukkit.getPluginManager().getPlugin("BentoBox") != null) {
            Optional<world.bentobox.bentobox.database.objects.Island> islandAt = BentoBox.getInstance().getIslandsManager().getIslandAt(container.getLocation());
            // If island is not present, skip it
            if (islandAt.isEmpty()) {
                return false;
            }
            world.bentobox.bentobox.database.objects.Island island = islandAt.get();
            return island.getMemberSet().contains(player.getUniqueId());
        }
        return true;
    }

}
