package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPIBukkit;
import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.commandapi.arguments.PlayerArgument;
import com.muhammaddaffa.mdlib.hooks.VaultEconomy;
import com.muhammaddaffa.mdlib.utils.*;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SellCommand {

    public static void register(EventManager eventManager) {
        SellCommand command = new SellCommand(eventManager);
        // check if sell command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("sell-command")) {
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
        this.command = new CommandAPICommand(Config.getFileConfiguration("config.yml").getString("commands.sell.command"))
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    if (target == null) {
                        if (!sender.hasPermission("nextgens.sell")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
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
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        // sell the items
                        this.sell(target);
                    }
                });
        List<String> aliases = Config.getFileConfiguration("config.yml").getStringList("commands.sell.aliases");
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
            Common.configMessage("config.yml", player, "messages.no-sell");
            // play bass sound
            Utils.bassSound(player);
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
        VisualAction.send(player, Config.getFileConfiguration("config.yml"), "sell-options", new Placeholder()
                .add("{amount}", Common.digits(totalItems))
                .add("{value}", Common.digits(totalValue)));
    }

}
