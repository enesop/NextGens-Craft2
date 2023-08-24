package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.hooks.vault.VaultEconomy;
import com.muhammaddaffa.nextgens.utils.*;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class SellCommand {

    public static void register(EventManager eventManager) {
        SellCommand command = new SellCommand(eventManager);
        // check if sell command is enabled
        if (!Config.CONFIG.getBoolean("sell-command")) {
            return;
        }
        Executor.sync(() -> {
            Logger.info("Sell command is enabled, overriding and registering sell command...");
            // unregister the command
            CommandAPIBukkit.unregister("sell", true, true);
            // register back the command
            command.register();
        });
    }

    private final EventManager eventManager;
    private final CommandAPICommand command;
    public SellCommand(EventManager eventManager) {
        this.eventManager = eventManager;
        this.command = new CommandAPICommand(Config.CONFIG.getString("commands.sell.command"))
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) {
                        if (!sender.hasPermission("nextgens.sell")) {
                            Common.config(sender, "messages.no-permission");
                            return;
                        }
                        if (!(sender instanceof Player player)) {
                            Common.sendMessage(sender, "&cUsage: /sell <player>");
                            return;
                        }
                        // sell the item to the player
                        this.sell(player);
                    } else {
                        if (!sender.hasPermission("nextgens.sell.others")) {
                            Common.config(sender, "messages.no-permission");
                            return;
                        }
                        // sell the items
                        this.sell(target);
                    }
                });
        List<String> aliases = Config.CONFIG.getStringList("commands.sell.aliases");
        this.command.setAliases(aliases.toArray(new String[0]));
    }

    public void register() {
        this.command.register();
    }

    private void sell(Player player) {
        double totalValue = 0.0;
        int totalItems = 0;
        // loop through inventory contents
        for (ItemStack stack : player.getInventory()) {
            // get value
            double value = Utils.getPriceValue(player, stack);
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
        /**
         * Event-related code
         */
        Event event = this.eventManager.getActiveEvent();
        if (event != null && event.getType() == Event.Type.SELL_MULTIPLIER && event.getSellMultiplier() != null) {
            // set the total value
            totalValue = totalValue * Math.max(1.0, event.getSellMultiplier());
        }
        // deposit the money
        VaultEconomy.deposit(player, totalValue);
        // send the visual action
        VisualAction.send(player, Config.CONFIG.getConfig(), "sell-options", new Placeholder()
                .add("{amount}", Common.digits(totalItems))
                .add("{value}", Common.digits(totalValue)));
    }

}
