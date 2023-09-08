package com.muhammaddaffa.nextgens.gui;

import com.muhammaddaffa.mdlib.gui.SimpleInventory;
import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.*;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public class UpgradeInventory extends SimpleInventory {

    private final Player player;
    private final ActiveGenerator active;
    private final Generator generator;
    private final Generator nextGenerator;
    private final GeneratorManager generatorManager;
    private final UserManager userManager;

    public UpgradeInventory(Player player, ActiveGenerator active, Generator generator, Generator nextGenerator,
                            GeneratorManager generatorManager, UserManager userManager) {
        super(Config.getFileConfiguration("upgrade_gui.yml").getInt("size"),
                Common.color(Config.getFileConfiguration("upgrade_gui.yml").getString("title")));
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
        List<Integer> slots = Config.getFileConfiguration("upgrade_gui.yml").getIntegerList("display-slots");
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
        FileConfiguration config = Config.getFileConfiguration("upgrade_gui.yml");
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

        if (config.getBoolean("display-enough-money.glowing")) {
            builder.enchant(Enchantment.DURABILITY);
        }

        // set the item
        this.setItems(slots, builder.build(), event -> {
            Block block = this.active.getLocation().getBlock();
            // money check
            if (VaultEconomy.getBalance(this.player) < this.generator.cost()) {
                Common.configMessage("config.yml", this.player, "messages.not-enough-money", new Placeholder()
                        .add("{money}", Common.digits(VaultEconomy.getBalance(this.player)))
                        .add("{upgradecost}", Common.digits(this.generator.cost()))
                        .add("{remaining}", Common.digits(VaultEconomy.getBalance(this.player) - this.generator.cost())));
                // play bass sound
                Utils.bassSound(this.player);
                // close the gui
                this.player.closeInventory();
                return;
            }
            // take the money from player
            VaultEconomy.withdraw(this.player, this.generator.cost());
            // register the generator again
            this.generatorManager.registerGenerator(this.player, this.nextGenerator, block);
            // visual actions
            VisualAction.send(this.player, Config.getFileConfiguration("config.yml"), "generator-upgrade-options", new Placeholder()
                    .add("{previous}", this.generator.displayName())
                    .add("{current}", this.nextGenerator.displayName())
                    .add("{cost}", Common.digits(this.generator.cost())));
            // play particle
            Executor.async(() -> {
                if (Config.getFileConfiguration("config.yml").getBoolean("generator-upgrade-options.particles")) {
                    // block crack particle
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, this.nextGenerator.item().getType().createBlockData());
                    // happy villager particle
                    block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
                }
            });
            // give cashback to the player
            Utils.performCashback(player, this.userManager, this.generator.cost());
            // close the inventory
            this.player.closeInventory();
        });
    }

    private void setNoMoneyButton(List<Integer> slots) {
        FileConfiguration config = Config.getFileConfiguration("upgrade_gui.yml");
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

        if (config.getBoolean("display-no-money.glowing")) {
            builder.enchant(Enchantment.DURABILITY);
        }

        // set the item
        this.setItems(slots, builder.build(), event -> {
            Common.configMessage("config.yml", this.player, "messages.not-enough-money", new Placeholder()
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
        ItemBuilder builder = ItemBuilder.fromConfig(Config.getFileConfiguration("upgrade_gui.yml"), "no-upgrade-button");
        if (builder == null) {
            return;
        }
        this.setItems(slots, builder.build());
    }

    private void setAcceptButton() {
        FileConfiguration config = Config.getFileConfiguration("upgrade_gui.yml");
        // get the slots
        List<Integer> slots = config.getIntegerList("confirm-slots");
        // create the item
        ItemBuilder builder = ItemBuilder.fromConfig(config, "confirm-button");
        if (builder == null) {
            return;
        }
        // set the item
        this.setItems(slots, builder.build(), event -> {
            // if the next generator is null, skip it
            if (this.nextGenerator == null) {
                return;
            }
            Block block = active.getLocation().getBlock();
            // money check
            if (VaultEconomy.getBalance(this.player) < this.generator.cost()) {
                Common.configMessage("config.yml", this.player, "messages.not-enough-money", new Placeholder()
                        .add("{money}", Common.digits(VaultEconomy.getBalance(this.player)))
                        .add("{upgradecost}", Common.digits(this.generator.cost()))
                        .add("{remaining}", Common.digits(VaultEconomy.getBalance(this.player) - this.generator.cost())));
                // play bass sound
                Utils.bassSound(this.player);
                // close the gui
                this.player.closeInventory();
                return;
            }
            // take the money from player
            VaultEconomy.withdraw(this.player, this.generator.cost());
            // register the generator again
            this.generatorManager.registerGenerator(this.player, this.nextGenerator, block);
            // visual actions
            VisualAction.send(this.player, Config.getFileConfiguration("config.yml"), "generator-upgrade-options", new Placeholder()
                    .add("{previous}", this.generator.displayName())
                    .add("{current}", this.nextGenerator.displayName())
                    .add("{cost}", Common.digits(this.generator.cost())));
            // play particle
            Executor.async(() -> {
                if (Config.getFileConfiguration("config.yml").getBoolean("generator-upgrade-options.particles")) {
                    // block crack particle
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 0.85, 0.5), 30, 0.5, 0.5, 0.5, 2.5, this.nextGenerator.item().getType().createBlockData());
                    // happy villager particle
                    block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.85, 0.5), 50, 0.5, 0.5, 0.5, 2.5);
                }
            });
            // give cashback to the player
            Utils.performCashback(player, this.userManager, this.generator.cost());
            // close the inventory
            this.player.closeInventory();
        });
    }

    private void setCancelButton() {
        FileConfiguration config = Config.getFileConfiguration("upgrade_gui.yml");
        // get the slots
        List<Integer> slots = config.getIntegerList("cancel-slots");
        // create the item
        ItemBuilder builder = ItemBuilder.fromConfig(config, "cancel-button");
        if (builder == null) {
            return;
        }
        // set the item
        this.setItems(slots, builder.build(), event -> {
            // close the inventory
            this.player.closeInventory();
        });
    }

}
