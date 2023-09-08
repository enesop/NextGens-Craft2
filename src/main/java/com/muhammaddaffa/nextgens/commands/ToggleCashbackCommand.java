package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.commandapi.arguments.PlayerArgument;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.users.User;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ToggleCashbackCommand {

    public static void register(UserManager userManager) {
        // check if the command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("commands.cashback.enabled")) {
            return;
        }
        ToggleCashbackCommand command = new ToggleCashbackCommand(userManager);
        // register the command
        command.register();
    }

    private final UserManager userManager;
    private final CommandAPICommand command;
    public ToggleCashbackCommand(UserManager userManager) {
        this.userManager = userManager;

        // get the variables
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        String mainCommand = config.getString("commands.cashback.command");
        List<String> aliases = config.getStringList("commands.cashback.aliases");

        this.command = new CommandAPICommand(mainCommand)
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player player = null;
                    if (args.get("target") == null) {
                        if (!sender.hasPermission("nextgens.togglecashback")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        // check if sender is a player
                        if (!(sender instanceof Player target)) {
                            Common.sendMessage(sender, "&cUsage: /{command} <player>", new Placeholder()
                                    .add("{command}", mainCommand));
                            return;
                        }
                        // if so, set the player object
                        player = target;
                    } else {
                        if (!sender.hasPermission("nextgens.togglecashback.others")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        player = (Player) args.get("target");
                    }
                    // check if player is not null
                    if (player != null) {
                        User user = this.userManager.getUser(player);
                        // if toggle cashback is on
                        if (user.isToggleCashback()) {
                            user.setToggleCashback(false);
                            // send off message
                            Common.configMessage("config.yml", player, "messages.cashback-off");
                        } else {
                            user.setToggleCashback(true);
                            // send on message
                            Common.configMessage("config.yml", player, "messages.cashback-on");
                        }
                    }
                });
        // set the aliases
        this.command.setAliases(aliases.toArray(new String[0]));
    }

    public void register() {
        this.command.register();
    }

}
