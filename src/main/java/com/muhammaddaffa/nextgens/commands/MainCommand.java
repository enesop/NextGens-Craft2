package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.generators.runnables.GeneratorTask;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.Common;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.Executor;
import com.muhammaddaffa.nextgens.utils.Placeholder;
import com.muhammaddaffa.nextgens.utils.gui.SimpleInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record MainCommand(
        GeneratorManager generatorManager,
        UserManager userManager
) implements TabExecutor {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            // add suggestion for admin commands
            if (sender.hasPermission("nextgens.admin")) {
                suggestions.add("give");
                suggestions.add("giveall");
                suggestions.add("reload");
                suggestions.add("addmax");
                suggestions.add("removemax");
                suggestions.add("resetmax");
            }

            return suggestions;
        }

        if (sender.hasPermission("nextgens.admin")) {
            // suggestions for args.length == 2
            if (args.length == 2 && List.of("give", "addmax", "removemax", "resetmax").contains(args[0].toLowerCase())) {
                return null;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("giveall")) {
                return StringUtil.copyPartialMatches(args[2], this.generatorManager
                        .getGeneratorIDs().stream().toList(), new ArrayList<>());
            }

            // suggestions for args.length == 3
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("give")) {
                    return StringUtil.copyPartialMatches(args[2], this.generatorManager
                            .getGeneratorIDs().stream().toList(), new ArrayList<>());
                }
                if (List.of("addmax", "removemax", "giveall").contains(args[0].toLowerCase())) {
                    return List.of("<amount>");
                }
            }

            // suggestions for args.length == 4
            if (args.length == 4) {
                if (args[0].equalsIgnoreCase("give")) {
                    return List.of("[amount]");
                }
            }

        }

        return new ArrayList<>();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return true;
        }

        if (args.length == 0) {
            Common.config(sender, "messages.help");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> this.give(sender, args);
            case "addmax" -> this.addMax(sender, args);
            case "removemax" -> this.removeMax(sender, args);
            case "resetmax" -> this.resetMax(sender, args);
            case "repair" -> this.repair(sender, args);
            case "giveall" -> this.giveAll(sender, args);
            case "reload" -> this.reload(sender);
            default -> Common.config(sender, "messages.help");
        }

        return true;
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // gens give <player> <tier> [amount]
        if (args.length < 3) {
            Common.sendMessage(sender, "&cUsage: /gens give <player> <generator> [amount]");
            return;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            Common.config(sender, "messages.target-not-found");
            return;
        }
        Generator generator = this.generatorManager.getGenerator(args[2]);
        if (generator == null) {
            Common.config(sender, "messages.invalid-gen");
            return;
        }
        int amount = 1;
        if (args.length >= 4 && Common.isInt(args[3])) {
            amount = Integer.parseInt(args[3]);
        }
        amount = Math.max(1, amount);
        // actually give the item to the player
        Common.addInventoryItem(player, generator.createItem(amount));
        // send message to the sender
        Common.config(sender, "messages.give-gen", new Placeholder()
                .add("{amount}", amount)
                .add("{gen}", generator.displayName())
                .add("{player}", player.getName()));
        // send message to the receiver
        Common.config(player, "messages.receive-gen", new Placeholder()
                .add("{amount}", amount)
                .add("{gen}", generator.displayName()));
    }

    private void addMax(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // gens addmax <player> <amount>
        if (args.length < 3) {
            Common.sendMessage(sender, "&cUsage: /gens addmax <player> <amount>");
            return;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            Common.config(sender, "messages.target-not-found");
            return;
        }
        if (!Common.isInt(args[2])) {
            Common.config(sender, "messages.not-int");
            return;
        }
        int amount = Math.max(0, Integer.parseInt(args[2]));
        // actually set the bonus generator place
        this.userManager.getUser(player).addBonus(amount);
        // send message to the command sender
        Common.config(sender, "messages.add-max", new Placeholder()
                .add("{amount}", amount)
                .add("{player}", player.getName()));
        // send message to the player
        Common.config(player, "messages.max-added", new Placeholder()
                .add("{amount}", amount)
                .add("{current}", this.generatorManager.getGeneratorCount(player))
                .add("{max}", this.userManager.getMaxSlot(player)));
    }

    private void removeMax(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // gens removemax <player> <amount>
        if (args.length < 3) {
            Common.sendMessage(sender, "&cUsage: /gens removemax <player> <amount>");
            return;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            Common.config(sender, "messages.target-not-found");
            return;
        }
        if (!Common.isInt(args[2])) {
            Common.config(sender, "messages.not-int");
            return;
        }
        int amount = Math.max(0, Integer.parseInt(args[2]));
        // actually set the bonus generator place
        this.userManager.getUser(player).removeBonus(amount);
        // send message to the command sender
        Common.config(sender, "messages.remove-max", new Placeholder()
                .add("{amount}", amount)
                .add("{player}", player.getName()));
        // send message to the player
        Common.config(player, "messages.max-removed", new Placeholder()
                .add("{amount}", amount)
                .add("{current}", this.generatorManager.getGeneratorCount(player))
                .add("{max}", this.userManager.getMaxSlot(player)));
    }

    private void resetMax(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // gens resetmax <player>
        if (args.length < 2) {
            Common.sendMessage(sender, "&cUsage: /gens resetmax <player>");
            return;
        }
        if (args[1].equalsIgnoreCase("all")) {
            this.userManager.getUsers().forEach(user -> user.setBonus(0));
            Common.config(sender, "messages.global-reset");
            return;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            Common.config(sender, "messages.target-not-found");
            return;
        }
        // reset the bonus
        this.userManager.getUser(player).setBonus(0);
        // send message to the command sender
        Common.config(sender, "messages.reset-max", new Placeholder()
                .add("{player}", player.getName()));
        // send message to the player
        Common.config(player, "messages.max-resetted", new Placeholder()
                .add("{current}", this.generatorManager.getGeneratorCount(player))
                .add("{max}", this.userManager.getMaxSlot(player)));
    }

    private void repair(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // gens repair <player>
        if (args.length < 2) {
            Common.sendMessage(sender, "&cUsage: /gens repair <player|all>");
            return;
        }
        if (args[1].equalsIgnoreCase("all")) {
            Executor.async(() -> {
                this.generatorManager.getActiveGenerator().forEach(active -> active.setCorrupted(false));
                Common.config(sender, "messages.global-repair");
            });
            return;
        }
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            Common.config(sender, "messages.target-not-found");
            return;
        }
        // reset the bonus
        Executor.async(() -> {
            this.generatorManager.getActiveGenerator(player).forEach(active -> active.setCorrupted(false));
        });
        // send message to the command sender
        Common.config(sender, "messages.player-repair", new Placeholder()
                .add("{player}", player.getName()));
        // send message to the player
        Common.config(player, "messages.gens-repaired");
    }

    private void giveAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // gens giveall <player>
        if (args.length < 3) {
            Common.sendMessage(sender, "&cUsage: /gens giveall <generator> <amount>");
            return;
        }
        Generator generator = this.generatorManager.getGenerator(args[1]);
        if (generator == null) {
            Common.config(sender, "messages.invalid-gen");
            return;
        }
        int amount = 1;
        if (args.length >= 4 && Common.isInt(args[2])) {
            amount = Integer.parseInt(args[2]);
        }
        amount = Math.max(1, amount);
        // send message to command sender
        Common.config(sender, "messages.give-all", new Placeholder()
                .add("{amount}", Common.digits(amount))
                .add("{gen}", generator.displayName()));
        // loop online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // actually give the generator
            Common.addInventoryItem(player, generator.createItem(amount));
            // send message to the receiver
            Common.config(player, "messages.receive-gen", new Placeholder()
                    .add("{amount}", Common.digits(amount))
                    .add("{gen}", generator.displayName()));
        }

    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("nextgens.admin")) {
            Common.config(sender, "messages.no-permission");
            return;
        }
        // actually reload the config
        Config.reload();
        // remove all holograms
        GeneratorTask.flush();
        // load back the generator
        this.generatorManager.loadGenerators();
        // start back the auto save
        this.generatorManager.startAutosaveTask();
        // send message to the sender
        Common.config(sender, "messages.reload");
        // close all gui
        SimpleInventoryManager.closeAll();
    }

}
