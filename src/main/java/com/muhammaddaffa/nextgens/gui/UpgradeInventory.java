package com.muhammaddaffa.nextgens.gui;

import com.muhammaddaffa.mdlib.fastinv.FastInv;
import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.*;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.listeners.helpers.GeneratorUpdateHelper;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.users.UserManager;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public class UpgradeInventory extends FastInv {

    private final Player player;
    private final ActiveGenerator active;
    private final Generator generator;
    private final Generator nextGenerator;
    private final GeneratorManager generatorManager;
    private final UserManager userManager;

    public UpgradeInventory(Player player, ActiveGenerator active, Generator generator, Generator nextGenerator,
                            GeneratorManager generatorManager, UserManager userManager) {
        super(NextGens.UPGRADE_GUI_CONFIG.getInt("size"),
                Common.color(NextGens.UPGRADE_GUI_CONFIG.getString("title")));
        this.player = player;
        this.active = active;
        this.generator = generator;
        this.nextGenerator = nextGenerator;
        this.generatorManager = generatorManager;
        this.userManager = userManager;

        this.setAcceptButton();
        this.setCancelButton();
        this.setDisplayButton();
    }

    private void setDisplayButton() {
        // get the slots
        List<Integer> slots = NextGens.UPGRADE_GUI_CONFIG.getIntegerList("display-slots");
        // set the item
        if (this.nextGenerator == null) {
            this.setNoUpgradeButton(slots);
            return;
        }
        // if player has enough money
        if (VaultEconomy.getBalance(this.player) >= this.generator.cost()) {
            this.setUpgradeButton(slots);
        } else {
            this.setNoMoneyButton(slots);
        }
    }

    private void setUpgradeButton(List<Integer> slots) {
        FileConfiguration config = NextGens.UPGRADE_GUI_CONFIG.getConfig();
        // build the item
        ItemBuilder builder = new ItemBuilder(this.generator.item().getType())
                .name(config.getString("display-enough-money.display-name"))
                .customModelData(config.getInt("display-enough-money.custom-model-data"))
                .lore(config.getStringList("display-enough-money.lore"))
                .flags(ItemFlag.values())
                .placeholder(new Placeholder()
                        .add("{current}", this.generator.displayName())
                        .add("{speed}", this.generator.interval())
                        .add("{corruption}", Common.digits(this.generator.corruptChance()))
                        .add("{repair}", Common.digits(this.generator.fixCost()))
                        .add("{next}", this.nextGenerator.displayName())
                        .add("{next_speed}", this.nextGenerator.interval())
                        .add("{next_corruption}", Common.digits(this.nextGenerator.corruptChance()))
                        .add("{next_repair}", Common.digits(this.nextGenerator.fixCost()))
                        .add("{cost}", Common.digits(this.generator.cost()))
                        .add("{balance}", Common.digits(VaultEconomy.getBalance(this.player))));

        // set the item
        this.setItems(slots, builder.build(), event -> {
            GeneratorUpdateHelper.upgradeGenerator(player, active, generator, nextGenerator);
            this.player.closeInventory();
        });
    }

    private void setNoMoneyButton(List<Integer> slots) {
        FileConfiguration config = NextGens.UPGRADE_GUI_CONFIG.getConfig();
        // build the item
        ItemBuilder builder = new ItemBuilder(this.generator.item().getType())
                .name(config.getString("display-no-money.display-name"))
                .customModelData(config.getInt("display-no-money.custom-model-data"))
                .lore(config.getStringList("display-no-money.lore"))
                .flags(ItemFlag.values())
                .placeholder(new Placeholder()
                        .add("{current}", this.generator.displayName())
                        .add("{speed}", this.generator.interval())
                        .add("{corruption}", Common.digits(this.generator.corruptChance()))
                        .add("{repair}", Common.digits(this.generator.fixCost()))
                        .add("{next}", this.nextGenerator.displayName())
                        .add("{next_speed}", this.nextGenerator.interval())
                        .add("{next_corruption}", Common.digits(this.nextGenerator.corruptChance()))
                        .add("{next_repair}", Common.digits(this.nextGenerator.fixCost()))
                        .add("{cost}", Common.digits(this.generator.cost()))
                        .add("{balance}", Common.digits(VaultEconomy.getBalance(this.player))));

        // set the item
        this.setItems(Utils.convertListToIntArray(slots), builder.build(), event -> {
            NextGens.DEFAULT_CONFIG.sendMessage(this.player, "messages.not-enough-money", new Placeholder()
                    .add("{money}", Common.digits(VaultEconomy.getBalance(this.player)))
                    .add("{upgradecost}", Common.digits(this.generator.cost()))
                    .add("{remaining}", Common.digits(VaultEconomy.getBalance(this.player) - this.generator.cost())));
            // play bass sound
            Utils.bassSound(this.player);
            // close the gui
            this.player.closeInventory();
        });
    }

    private void setNoUpgradeButton(List<Integer> slots) {
        ItemBuilder builder = ItemBuilder.fromConfig(NextGens.UPGRADE_GUI_CONFIG.getConfig(), "no-upgrade-button");
        if (builder == null) {
            return;
        }
        this.setItems(Utils.convertListToIntArray(slots), builder.build());
    }

    private void setAcceptButton() {
        FileConfiguration config = NextGens.UPGRADE_GUI_CONFIG.getConfig();
        // get the slots
        List<Integer> slots = config.getIntegerList("confirm-slots");
        // create the item
        ItemBuilder builder = ItemBuilder.fromConfig(config, "confirm-button");
        if (builder == null) {
            return;
        }
        // set the item
        this.setItems(Utils.convertListToIntArray(slots), builder.build(), event -> {
            // if the next generator is null, skip it
            if (this.nextGenerator == null) {
                return;
            }
            Block block = active.getLocation().getBlock();
            // money check
            if (VaultEconomy.getBalance(this.player) < this.generator.cost()) {
                NextGens.DEFAULT_CONFIG.sendMessage(this.player, "messages.not-enough-money", new Placeholder()
                        .add("{money}", Common.digits(VaultEconomy.getBalance(this.player)))
                        .add("{upgradecost}", Common.digits(this.generator.cost()))
                        .add("{remaining}", Common.digits(VaultEconomy.getBalance(this.player) - this.generator.cost())));
                // play bass sound
                Utils.bassSound(this.player);
                // close the gui
                this.player.closeInventory();
                return;
            }
            // if the block is no longer a generator, skip it
            if (this.generatorManager.getActiveGenerator(this.active.getLocation()) == null) {
                this.player.closeInventory();
                return;
            }
            // take the money from player
            VaultEconomy.withdraw(this.player, this.generator.cost());
            // register the generator again
            this.generatorManager.registerGenerator(this.player, this.nextGenerator, block);
            // visual actions
            VisualAction.send(this.player, NextGens.DEFAULT_CONFIG.getConfig(), "generator-upgrade-options", new Placeholder()
                    .add("{previous}", this.generator.displayName())
                    .add("{current}", this.nextGenerator.displayName())
                    .add("{cost}", Common.digits(this.generator.cost())));
            // play particle
            Executor.async(() -> {
                if (NextGens.DEFAULT_CONFIG.getConfig().getBoolean("generator-upgrade-options.particles")) {
                    // block crack particle
                    block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, this.nextGenerator.item().getType().createBlockData());
                    // happy villager particle
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
                }
            });
            // give cashback to the player
            Utils.performCashback(player, this.userManager, this.generator.cost());
            // close the inventory
            this.player.closeInventory();
        });
    }

    private void setCancelButton() {
        FileConfiguration config = NextGens.UPGRADE_GUI_CONFIG.getConfig();
        // get the slots
        List<Integer> slots = config.getIntegerList("cancel-slots");
        // create the item
        ItemBuilder builder = ItemBuilder.fromConfig(config, "cancel-button");
        if (builder == null) {
            return;
        }
        // set the item
        this.setItems(Utils.convertListToIntArray(slots), builder.build(), event -> {
            // close the inventory
            this.player.closeInventory();
        });
    }

}
