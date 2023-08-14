package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.utils.Common;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.Placeholder;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class PickupCommand {

    public static void register(GeneratorManager generatorManager) {
        PickupCommand command = new PickupCommand(generatorManager);
        // register the command
        command.register();
    }

    private final GeneratorManager generatorManager;
    private final CommandAPICommand command;
    public PickupCommand(GeneratorManager generatorManager) {
        this.generatorManager = generatorManager;
        this.command = new CommandAPICommand("pickupgens")
                .withAliases("pickupgen", "pickgens", "pickgen")
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    Player actualTarget;
                    if (target == null) {
                        if (!sender.hasPermission("nextgens.pickup")) {
                            Common.config(sender, "messages.no-permission");
                            return;
                        }
                        if (!(sender instanceof Player player)) {
                            Common.sendMessage(sender, "&cUsage: /pickupgens <player>");
                            return;
                        }
                        actualTarget = player;
                    } else {
                        if (!sender.hasPermission("nextgens.pickup.others")) {
                            Common.config(sender, "messages.no-permission");
                            return;
                        }
                        actualTarget = target;
                    }
                    // get all generators player have
                    List<ActiveGenerator> generators = this.generatorManager.getActiveGenerator(actualTarget);
                    int total = generators.size();
                    // loop generators
                    for (ActiveGenerator active : generators) {
                        // check if broken pickup option is enabled
                        if (Config.CONFIG.getBoolean("broken-pickup") && active.isCorrupted()) {
                            total--;
                            continue;
                        }
                        // unregister the generator
                        this.generatorManager.unregisterGenerator(active.getLocation());
                        // set the block to air
                        active.getLocation().getBlock().setType(Material.AIR);
                        // give the item to the player
                        Common.addInventoryItem(actualTarget, active.getGenerator().createItem(1));
                    }
                    // send message
                    Common.config(sender, "messages.force-pickup", new Placeholder()
                            .add("{player}", actualTarget.getName())
                            .add("{amount}", Common.digits(total)));
                    Common.config(actualTarget, "messages.pickup-gens", new Placeholder()
                            .add("{amount}", Common.digits(total)));
                    // send sound
                    actualTarget.playSound(actualTarget.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                });
    }

    public void register() {
        this.command.register();
    }

}
