package com.muhammaddaffa.nextgens.generators.managers;

import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.runnables.GeneratorTask;
import com.muhammaddaffa.nextgens.gui.FixInventory;
import com.muhammaddaffa.nextgens.gui.UpgradeInventory;
import com.muhammaddaffa.nextgens.hooks.vault.VaultEconomy;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        if (event.getHand() != EquipmentSlot.HAND ||
                event.getAction() != Action.RIGHT_CLICK_BLOCK ||
                !player.isSneaking()) {
            return;
        }
        // get active generator
        ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
        // skip if not active generator
        if (block == null || active == null) {
            return;
        }
        // get the generator
        Generator generator = active.getGenerator();
        // check if player is the owner
        if (!player.getUniqueId().equals(active.getOwner())) {
            Common.config(player, "messages.not-owner");
            // play bass sound
            Common.playBassSound(player);
            return;
        }
        // corruption check
        if (active.isCorrupted()) {
            // create gui
            FixInventory gui = new FixInventory(player, active, generator);
            // open the gui
            gui.open(player);
            return;
        }
        // check for next tier
        Generator nextGenerator = this.generatorManager.getGenerator(generator.nextTier());
        // upgrade gui option
        if (Config.CONFIG.getBoolean("upgrade-gui")) {
            // create the gui object
            UpgradeInventory gui = new UpgradeInventory(player, active, generator, nextGenerator, this.generatorManager);
            // open the gui for player
            gui.open(player);
        } else {
            if (nextGenerator == null) {
                Common.config(player, "messages.no-upgrade");
                // play bass sound
                Common.playBassSound(player);
                return;
            }
            // money check
            if (VaultEconomy.getBalance(player) < generator.cost()) {
                Common.config(player, "messages.not-enough-money", new Placeholder()
                        .add("{money}", Common.digits(VaultEconomy.getBalance(player)))
                        .add("{upgradecost}", Common.digits(generator.cost()))
                        .add("{remaining}", Common.digits(VaultEconomy.getBalance(player) - generator.cost())));
                // play bass sound
                Common.playBassSound(player);
                return;
            }
            // take the money from player
            VaultEconomy.withdraw(player, generator.cost());
            // register the generator again
            this.generatorManager.registerGenerator(player, nextGenerator, block);
            // visual actions
            VisualAction.send(player, Config.CONFIG.getConfig(), "generator-upgrade-options", new Placeholder()
                    .add("{previous}", generator.displayName())
                    .add("{current}", nextGenerator.displayName())
                    .add("{cost}", Common.digits(generator.cost())));
            // play particle
            Executor.async(() -> {
                if (Config.CONFIG.getBoolean("generator-upgrade-options.particles")) {
                    // block crack particle
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, generator.item().getType().createBlockData());
                    // happy villager particle
                    block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void generatorBreak(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK ||
                event.getHand() != EquipmentSlot.HAND ||
                block == null) {
            return;
        }
        ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
        // skip if not active generator
        if (active == null) {
            return;
        }
        Generator generator = active.getGenerator();
        // cancel event
        event.setCancelled(true);
        // check if shift pickup option
        if (Config.CONFIG.getBoolean("shift-pickup") && !player.isSneaking()) {
            // play bass sound
            Common.playBassSound(player);
            return;
        }
        // pickup broken check
        if (active.isCorrupted()) {
            Common.config(player, "messages.pickup-broken");
            // play bass sound
            Common.playBassSound(player);
            return;
        }
        // disable breaking others gens
        if (!player.hasPermission("nextgens.admin") && !player.getUniqueId().equals(active.getOwner())) {
            Common.config(player, "messages.not-owner");
            // play bass sound
            Common.playBassSound(player);
            return;
        }
        // check if player is eligible
        if (player.hasPermission("nextgens.admin") || player.getUniqueId().equals(active.getOwner())) {
            // unregister generator
            this.generatorManager.unregisterGenerator(block);
            // remove block
            block.setType(Material.AIR);
            // give back the item
            if (Config.CONFIG.getBoolean("drop-on-break")) {
                block.getWorld().dropItemNaturally(block.getLocation(), generator.createItem(1));
            } else {
                Common.addInventoryItem(player, generator.createItem(1));
            }
            // visual action
            Player owner = Bukkit.getPlayer(active.getOwner());
            if (owner != null) {
                VisualAction.send(owner, Config.CONFIG.getConfig(), "generator-break-options", new Placeholder()
                        .add("{gen}", generator.displayName())
                        .add("{current}", this.generatorManager.getGeneratorCount(player))
                        .add("{max}", this.userManager.getMaxSlot(player)));
            }
            // play particle
            Executor.async(() -> {
                if (Config.CONFIG.getBoolean("generator-break-options.particles")) {
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
        // skip if the item is not generator
        if (generator == null) {
            return;
        }
        int current = this.generatorManager.getGeneratorCount(player);
        int max = this.userManager.getMaxSlot(player);
        // check if player has exceeded max
        if (current >= max) {
            event.setCancelled(true);
            // send message
            Common.config(player, "messages.max-gen");
            // play bass sound
            Common.playBassSound(player);
            return;
        }
        if (Config.CONFIG.getBoolean("place-permission") && !player.hasPermission("nextgens.generator." + generator.id()) && !player.hasPermission("nextgens.generator.*")) {
            Common.config(player, "messages.no-permission-gen");
            // bass sound
            Common.playBassSound(player);
            return;
        }
        if (Config.CONFIG.getStringList("blacklisted-worlds").contains(player.getWorld().getName())) {
            Common.config(player, "messages.invalid-world");
            // bass sound
            Common.playBassSound(player);
            return;
        }
        // register active gen
        this.generatorManager.registerGenerator(player, generator, block);
        // visual action
        VisualAction.send(player, Config.CONFIG.getConfig(), "generator-place-options", new Placeholder()
                .add("{gen}", generator.displayName())
                .add("{current}", this.generatorManager.getGeneratorCount(player))
                .add("{max}", max));
        // play particle on the block
        Executor.async(() -> {
            if (Config.CONFIG.getBoolean("generator-place-options.particles")) {
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
        FileConfiguration config = Config.CONFIG.getConfig();
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void prevention(BlockBreakEvent event) {
        // get all variables we want
        Block block = event.getBlock();
        ActiveGenerator active = this.generatorManager.getActiveGenerator(block);
        // cancel if the block is active generator
        if (active != null) {
            event.setCancelled(true);
            // play bass sound
            Common.playBassSound(event.getPlayer());
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
        if (Config.CONFIG.getBoolean("disable-drop-place") && this.generatorManager.isDropItem(hand)) {
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
        if (Config.CONFIG.getBoolean("anti-explosion")) {
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
