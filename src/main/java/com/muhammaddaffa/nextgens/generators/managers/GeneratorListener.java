package com.muhammaddaffa.nextgens.generators.managers;

import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.*;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.api.events.generators.GeneratorBreakEvent;
import com.muhammaddaffa.nextgens.api.events.generators.GeneratorLoadEvent;
import com.muhammaddaffa.nextgens.api.events.generators.GeneratorPlaceEvent;
import com.muhammaddaffa.nextgens.api.events.generators.GeneratorUpgradeEvent;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.action.InteractAction;
import com.muhammaddaffa.nextgens.gui.FixInventory;
import com.muhammaddaffa.nextgens.gui.UpgradeInventory;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record GeneratorListener(
        GeneratorManager generatorManager,
        UserManager userManager
) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private void generatorUpgrade(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        if (event.getHand() != EquipmentSlot.HAND ||
                block == null ||
                NextGens.STOPPING) {
            return;
        }
        // get variables
        ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
        InteractAction action = InteractAction.find(event, InteractAction.SHIFT_RIGHT);
        // skip if not active generator
        if (active == null || action == null) {
            return;
        }
        // get the generator
        Generator generator = active.getGenerator();
        // corruption check
        if (active.isCorrupted()) {
            // get the correct interaction type
            InteractAction required = InteractAction.find(config.getString("interaction.gens-fix"), InteractAction.SHIFT_RIGHT);
            if (action == required) {
                if (config.getBoolean("repair-owner-only") && !player.getUniqueId().equals(active.getOwner())) {
                    Common.configMessage("config.yml", player, "messages.not-owner");
                    // play bass sound
                    Utils.bassSound(player);
                    return;
                }
                if (config.getBoolean("corruption.gui-fix")) {
                    // create gui
                    FixInventory gui = new FixInventory(player, active, generator, this.userManager);
                    // open the gui
                    gui.open(player);
                } else {
                    // money check
                    if (VaultEconomy.getBalance(player) < generator.fixCost()) {
                        Common.configMessage("config.yml", player, "messages.not-enough-money", new Placeholder()
                                .add("{money}", Common.digits(VaultEconomy.getBalance(player)))
                                .add("{upgradecost}", Common.digits(generator.fixCost()))
                                .add("{remaining}", Common.digits(VaultEconomy.getBalance(player) - generator.fixCost())));
                        // play bass sound
                        Utils.bassSound(player);
                        return;
                    }
                    // take the money from player
                    VaultEconomy.withdraw(player, generator.fixCost());
                    // fix the generator
                    active.setCorrupted(false);
                    // visual actions
                    VisualAction.send(player, Config.getFileConfiguration("config.yml"), "corrupt-fix-options", new Placeholder()
                            .add("{gen}", generator.displayName())
                            .add("{cost}", Common.digits(generator.fixCost())));
                    // play particle
                    Executor.async(() -> {
                        if (Config.getFileConfiguration("config.yml").getBoolean("corrupt-fix-options.particles")) {
                            // block crack particle
                            block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, generator.item().getType().createBlockData());
                            // happy villager particle
                            block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
                        }
                    });
                    // give cashback to the player
                    Utils.performCashback(player, this.userManager, generator.fixCost());
                }
                return;
            }
            return;
        }
        // check if player is the owner
        if (!player.getUniqueId().equals(active.getOwner())) {
            Common.configMessage("config.yml", player, "messages.not-owner");
            // play bass sound
            Utils.bassSound(player);
            return;
        }
        // get correct interaction type
        InteractAction required = InteractAction.find(config.getString("interaction.gens-upgrade"), InteractAction.SHIFT_RIGHT);
        if (action != required) {
            return;
        }
        // check for next tier
        Generator nextGenerator = this.generatorManager.getGenerator(generator.nextTier());
        // upgrade gui option
        if (config.getBoolean("upgrade-gui")) {
            GeneratorUpgradeEvent upgradeEvent = new GeneratorUpgradeEvent(generator, player, nextGenerator);
            Bukkit.getPluginManager().callEvent(upgradeEvent);
            if (upgradeEvent.isCancelled()) {
                return;
            }
            // create the gui object
            UpgradeInventory gui = new UpgradeInventory(player, active, generator, nextGenerator, this.generatorManager, this.userManager);
            // open the gui for player
            gui.open(player);
        } else {
            if (nextGenerator == null) {
                Common.configMessage("config.yml", player, "messages.no-upgrade");
                // play bass sound
                Utils.bassSound(player);
                return;
            }
            // money check
            if (VaultEconomy.getBalance(player) < generator.cost()) {
                Common.configMessage("config.yml", player, "messages.not-enough-money", new Placeholder()
                        .add("{money}", Common.digits(VaultEconomy.getBalance(player)))
                        .add("{upgradecost}", Common.digits(generator.cost()))
                        .add("{remaining}", Common.digits(VaultEconomy.getBalance(player) - generator.cost())));
                // play bass sound
                Utils.bassSound(player);
                return;
            }
            // call the custom events
            GeneratorUpgradeEvent upgradeEvent = new GeneratorUpgradeEvent(generator, player, nextGenerator);
            Bukkit.getPluginManager().callEvent(upgradeEvent);
            if (upgradeEvent.isCancelled()) {
                return;
            }
            // take the money from player
            VaultEconomy.withdraw(player, generator.cost());
            // register the generator again
            this.generatorManager.registerGenerator(player, nextGenerator, block);
            // visual actions
            VisualAction.send(player, config, "generator-upgrade-options", new Placeholder()
                    .add("{previous}", generator.displayName())
                    .add("{current}", nextGenerator.displayName())
                    .add("{cost}", Common.digits(generator.cost())));
            // play particle
            Executor.asyncLater(3L, () -> {
                if (config.getBoolean("generator-upgrade-options.particles")) {
                    // block crack particle
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, nextGenerator.item().getType().createBlockData());
                    // happy villager particle
                    block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
                }
            });
            // give cashback to the player
            Utils.performCashback(player, this.userManager, generator.cost());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void generatorBreak(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        if (event.getHand() != EquipmentSlot.HAND ||
                block == null ||
                NextGens.STOPPING) {
            return;
        }
        InteractAction action = InteractAction.find(event, InteractAction.LEFT);
        InteractAction required = InteractAction.find(config.getString("interaction.gens-pickup"), InteractAction.LEFT);
        ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
        // skip if not active generator
        if (active == null || action != required) {
            return;
        }
        Generator generator = active.getGenerator();
        // cancel event
        event.setCancelled(true);
        // pickup broken check
        if (active.isCorrupted()) {
            Common.configMessage("config.yml", player, "messages.pickup-broken");
            // play bass sound
            Utils.bassSound(player);
            return;
        }
        // disable breaking others gens
        if (!player.hasPermission("nextgens.break.others") && !player.getUniqueId().equals(active.getOwner())) {
            Common.configMessage("config.yml", player, "messages.not-owner");
            // play bass sound
            Utils.bassSound(player);
            return;
        }
        // check if player is eligible
        if (player.hasPermission("nextgens.break.others") || player.getUniqueId().equals(active.getOwner())) {
            // call the custom events
            GeneratorBreakEvent breakEvent = new GeneratorBreakEvent(generator, player);
            Bukkit.getPluginManager().callEvent(breakEvent);
            if (breakEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            // unregister generator
            this.generatorManager.unregisterGenerator(block);
            // remove block
            block.setType(Material.AIR);
            // give back the item
            if (config.getBoolean("drop-on-break")) {
                block.getWorld().dropItemNaturally(block.getLocation(), generator.createItem(1));
            } else {
                Common.addInventoryItem(player, generator.createItem(1));
            }
            // visual action
            Player owner = Bukkit.getPlayer(active.getOwner());
            if (owner != null) {
                VisualAction.send(owner, config, "generator-break-options", new Placeholder()
                        .add("{gen}", generator.displayName())
                        .add("{current}", this.generatorManager.getGeneratorCount(owner))
                        .add("{max}", this.userManager.getMaxSlot(owner)));
            }
            // play particle
            Executor.async(() -> {
                if (config.getBoolean("generator-break-options.particles")) {
                    // cloud particles
                    block.getWorld().spawnParticle(Particle.CLOUD, block.getLocation().add(0.5, 0, 0.5), 30, 0.25, 0.25, 0.25, 3);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void generatorPlace(BlockPlaceEvent event) {
        // get all variables we want
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack stack = event.getItemInHand();
        Generator generator = this.generatorManager.getGenerator(stack);
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        // skip if the item is not generator
        if (generator == null || NextGens.STOPPING) {
            return;
        }
        int current = this.generatorManager.getGeneratorCount(player);
        int max = this.userManager.getMaxSlot(player);
        // check if player has exceeded max
        if (current >= max) {
            event.setCancelled(true);
            // send message
            Common.configMessage("config.yml", player, "messages.max-gen");
            // play bass sound
            Utils.bassSound(player);
            return;
        }
        if (config.getBoolean("place-permission") && !player.hasPermission("nextgens.generator." + generator.id()) && !player.hasPermission("nextgens.generator.*")) {
            event.setCancelled(true);
            // send message
            Common.configMessage("config.yml", player, "messages.no-permission-gen");
            // bass sound
            Utils.bassSound(player);
            return;
        }
        if (config.getStringList("blacklisted-worlds").contains(player.getWorld().getName())) {
            event.setCancelled(true);
            // send message
            Common.configMessage("config.yml", player, "messages.invalid-world");
            // bass sound
            Utils.bassSound(player);
            return;
        }
        // generator distance
        if (config.getBoolean("generator-place-distance.enabled")) {
            double distance = config.getInt("generator-place-distance.distance");
            // loop through all generators
            for (ActiveGenerator active : this.generatorManager.getActiveGenerator()) {
                if (!active.getLocation().getWorld().equals(block.getWorld())) {
                    continue;
                }
                if (active.getLocation().distance(block.getLocation()) < distance) {
                    event.setCancelled(true);
                    // send message
                    Common.configMessage("config.yml", player, "messages.too-close");
                    // bass sound
                    Utils.bassSound(player);
                    return;
                }
            }
        }
        // call the custom events
        GeneratorPlaceEvent placeEvent = new GeneratorPlaceEvent(generator, player);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        // register active gen
        this.generatorManager.registerGenerator(player, generator, block);
        // visual action
        VisualAction.send(player, config, "generator-place-options", new Placeholder()
                .add("{gen}", generator.displayName())
                .add("{current}", this.generatorManager.getGeneratorCount(player))
                .add("{max}", max));
        // play particle on the block
        Executor.async(() -> {
            if (config.getBoolean("generator-place-options.particles")) {
                // block crack particle
                block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, generator.item().getType().createBlockData());
                // happy villager particle
                block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        // check if option is enabled
        if (config.getBoolean("first-join-generator.enabled")) {
            // if it's not first join, skip it
            if (player.hasPlayedBefore()) {
                return;
            }
            // get the generator
            Generator generator = this.generatorManager.getGenerator(config.getString("first-join-generator.generator"));
            // give the generator to the player
            if (generator != null) {
                ItemStack stack = generator.createItem(config.getInt("first-join-generator.amount", 1));
                Common.addInventoryItem(player, stack);
            }
            // execute the commands
            for (String command : config.getStringList("first-join-generator.commands")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void prevention(CraftItemEvent event) {
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        // if it's not enabled, skip it
        if (!config.getBoolean("disable-crafting.enabled")) {
            return;
        }
        for (ItemStack stack : event.getInventory()) {
            if (this.generatorManager.isGeneratorItem(stack) || this.generatorManager.isDropItem(stack)) {
                event.setCancelled(true);
                // send message
                Common.configMessage("config.yml", event.getWhoClicked(), "disable-crafting.message");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void prevention(BlockBreakEvent event) {
        // get all variables we want
        Block block = event.getBlock();
        ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
        // cancel if the block is active generator
        if (active != null) {
            event.setCancelled(true);
            // play bass sound
            Utils.bassSound(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void prevention(EntityExplodeEvent event) {
        this.checkExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void prevention(BlockExplodeEvent event) {
        this.checkExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void prevention(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        // check if requirements are correct
        if (Config.getFileConfiguration("config.yml").getBoolean("disable-drop-place") && this.generatorManager.isDropItem(hand)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void prevention(BlockPistonExtendEvent event) {
        this.checkPiston(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void prevention(BlockPistonRetractEvent event) {
        this.checkPiston(event, event.getBlocks());
    }

    private void checkPiston(BlockPistonEvent event, List<Block> blocks) {
        for (Block block : blocks) {
            // get active generator from block
            ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
            if (active != null) {
                event.setCancelled(true);
            }
            /*// if piston move is enabled
            if (enabled) {
                // change the active generator location
                Location previous = block.getLocation();
                Location next = block.getRelative(event.getDirection()).getLocation();
                boolean corrupted = active.isCorrupted();
                // unregister the generator
                this.generatorManager.unregisterGenerator(previous);
                // destroy the hologram
                GeneratorTask.destroy(active);
                // register back the generator
                Executor.syncLater(3L, () -> {
                    ActiveGenerator fresh = this.generatorManager.registerGenerator(active.getOwner(), active.getGenerator(), next.getBlock());
                    fresh.setCorrupted(corrupted);
                });
            } else {
                // cancel the event
                event.setCancelled(true);
            }*/
        }
    }

    private void checkExplosion(List<Block> blocks) {
        if (Config.getFileConfiguration("config.yml").getBoolean("anti-explosion")) {
            // remove generator blocks
            blocks.removeIf(block -> this.generatorManager.getActiveGenerator(block) != null);
        } else {
            // if option is disabled, unregister active generator
            for (Block block : blocks) {
                ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
                if (active != null) {
                    this.generatorManager.unregisterGenerator(active.getLocation());
                }
            }
        }
    }

}
