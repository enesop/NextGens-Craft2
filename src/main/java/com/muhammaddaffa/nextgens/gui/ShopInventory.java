package com.muhammaddaffa.nextgens.gui;

import com.muhammaddaffa.mdlib.gui.SimpleInventory;
import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.ItemBuilder;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopInventory extends SimpleInventory {

    public static void openInventory(Player player, GeneratorManager generatorManager) {
        ShopInventory gui = new ShopInventory(player, generatorManager);
        // open the gui
        gui.open(player);
    }

    private final Player player;
    private final GeneratorManager generatorManager;

    public ShopInventory(Player player, GeneratorManager generatorManager) {
        super(Config.getFileConfiguration("shop.yml").getInt("size"),
                Common.color(Config.getFileConfiguration("shop.yml").getString("title")));
        this.player = player;
        this.generatorManager = generatorManager;

        this.setAllItems();
    }

    private void setAllItems() {
        FileConfiguration config = Config.getFileConfiguration("shop.yml");
        // loop the items
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            // get the data
            String type = config.getString("items." + key + ".type");
            List<Integer> slots = config.getIntegerList("items." + key + ".slots");
            // build the item
            ItemBuilder builder = ItemBuilder.fromConfig(config, "items." + key);
            if (builder == null) {
                continue;
            }
            ItemStack stack = builder.build();

            if (type == null || !type.equalsIgnoreCase("GENERATOR")) {
                this.setItems(slots, stack);
                continue;
            }

            String id = config.getString("items." + key + ".generator");
            Generator generator = this.generatorManager.getGenerator(id);
            double cost = config.getDouble("items." + key + ".cost");

            // skip if the generator is invalid
            if (generator == null) {
                continue;
            }

            this.setItems(slots, stack, event -> {
                // money check
                if (VaultEconomy.getBalance(this.player) <= cost) {
                    Common.configMessage("config.yml", this.player, "messages.not-enough-money", new Placeholder()
                            .add("{money}", Common.digits(VaultEconomy.getBalance(this.player)))
                            .add("{upgradecost}", Common.digits(cost))
                            .add("{remaining}", Common.digits(VaultEconomy.getBalance(this.player) - cost)));
                    // play bass sound
                    Utils.bassSound(this.player);
                    // close on no money
                    if (Config.getFileConfiguration("config.yml").getBoolean("close-on-no-money")) {
                        this.player.closeInventory();
                    }
                    return;
                }
                // reduce the amount
                VaultEconomy.withdraw(this.player, cost);
                // give the generator
                Common.addInventoryItem(this.player, generator.createItem(1));
                // send message
                Common.configMessage("config.yml", this.player, "messages.gen-purchase", new Placeholder()
                        .add("{gen}", generator.displayName())
                        .add("{cost}", Common.digits(cost)));
                // close on purchase
                if (Config.getFileConfiguration("config.yml").getBoolean("close-on-purchase")) {
                    this.player.closeInventory();
                }
            });

        }
    }

}
