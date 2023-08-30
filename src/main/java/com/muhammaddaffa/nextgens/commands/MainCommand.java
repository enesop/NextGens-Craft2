package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.commandapi.arguments.*;
import com.muhammaddaffa.mdlib.gui.SimpleInventoryManager;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.Executor;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.generators.runnables.GeneratorTask;
import com.muhammaddaffa.nextgens.sellwand.Sellwand;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import org.bukkit.entity.Player;

import java.util.List;

public class MainCommand {

    public static void register(GeneratorManager generatorManager, UserManager userManager, EventManager eventManager) {
        MainCommand command = new MainCommand(generatorManager, userManager, eventManager);
        // register the command
        command.register();
    }

    private final GeneratorManager generatorManager;
    private final UserManager userManager;
    private final EventManager eventManager;
    private final CommandAPICommand command;
    public MainCommand(GeneratorManager generatorManager, UserManager userManager, EventManager eventManager) {
        this.generatorManager = generatorManager;
        this.userManager = userManager;
        this.eventManager = eventManager;
        this.command = new CommandAPICommand(Config.getFileConfiguration("config.yml").getString("commands.nextgens.command"))
                .withSubcommand(this.getGiveSubcommand())
                .withSubcommand(this.getAddMaxSubCommand())
                .withSubcommand(this.getRemoveMaxSubcommand())
                .withSubcommand(this.getResetMaxSubcommand())
                .withSubcommand(this.getRepairSubcommand())
                .withSubcommand(this.getReloadSubcommand())
                .withSubcommand(this.getSellwandSubcommand())
                .withSubcommand(this.getStartEventCommand())
                .withSubcommand(this.getStopEventCommand())
                .executes((sender, args) -> {
                    if (sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.help");
                    }
                });
        List<String> aliases = Config.getFileConfiguration("config.yml").getStringList("commands.nextgens.aliases");
        this.command.setAliases(aliases.toArray(new String[0]));
    }

    public void register() {
        this.command.register();
    }

    private CommandAPICommand getGiveSubcommand() {
        return new CommandAPICommand("give")
                .withArguments(new PlayerArgument("target"))
                .withArguments(new StringArgument("generator_id")
                        .replaceSuggestions(ArgumentSuggestions.strings(this.generatorManager.getGeneratorIDs())))
                .withOptionalArguments(new IntegerArgument("generator_amount")
                        .replaceSuggestions(ArgumentSuggestions.strings("[<amount>]")))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    // get all variables
                    Player target = (Player) args.get("target");
                    String generatorId = (String) args.get("generator_id");
                    Integer amount = (Integer) args.get("generator_amount");
                    // get the generator object
                    Generator generator = this.generatorManager.getGenerator(generatorId);
                    if (generator == null) {
                        Common.configMessage("config.yml", sender, "messages.invalid-gen");
                        return;
                    }
                    int actualAmount = 1;
                    if (amount != null) {
                        actualAmount = Math.max(1, amount);
                    }
                    // actually give the item to the player
                    Common.addInventoryItem(target, generator.createItem(actualAmount));
                    // send message to the sender
                    Common.configMessage("config.yml", sender, "messages.give-gen", new Placeholder()
                            .add("{amount}", actualAmount)
                            .add("{gen}", generator.displayName())
                            .add("{player}", target.getName()));
                    // send message to the receiver
                    Common.configMessage("config.yml", target, "messages.receive-gen", new Placeholder()
                            .add("{amount}", actualAmount)
                            .add("{gen}", generator.displayName()));
                });
    }

    private CommandAPICommand getAddMaxSubCommand() {
        return new CommandAPICommand("addmax")
                .withArguments(new PlayerArgument("target"))
                .withArguments(new IntegerArgument("amount")
                        .replaceSuggestions(ArgumentSuggestions.strings("[<amount>]")))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    // get all variables
                    Player target = (Player) args.get("target");
                    int amount = (int) args.get("amount");
                    // actually set the bonus generator place
                    this.userManager.getUser(target).addBonus(amount);
                    // send message to the command sender
                    Common.configMessage("config.yml", sender, "messages.add-max", new Placeholder()
                            .add("{amount}", amount)
                            .add("{player}", target.getName()));
                    // send message to the player
                    Common.configMessage("config.yml", target, "messages.max-added", new Placeholder()
                            .add("{amount}", amount)
                            .add("{current}", this.generatorManager.getGeneratorCount(target))
                            .add("{max}", this.userManager.getMaxSlot(target)));
                });
    }

    private CommandAPICommand getRemoveMaxSubcommand() {
        return new CommandAPICommand("removemax")
                .withArguments(new PlayerArgument("target"))
                .withArguments(new IntegerArgument("amount")
                        .replaceSuggestions(ArgumentSuggestions.strings("[<amount>]")))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    // get all variables
                    Player target = (Player) args.get("target");
                    int amount = (int) args.get("amount");
                    // actually set the bonus generator place
                    this.userManager.getUser(target).removeBonus(amount);
                    // send message to the command sender
                    Common.configMessage("config.yml", sender, "messages.remove-max", new Placeholder()
                            .add("{amount}", amount)
                            .add("{player}", target.getName()));
                    // send message to the player
                    Common.configMessage("config.yml", target, "messages.max-removed", new Placeholder()
                            .add("{amount}", amount)
                            .add("{current}", this.generatorManager.getGeneratorCount(target))
                            .add("{max}", this.userManager.getMaxSlot(target)));
                });
    }

    private CommandAPICommand getResetMaxSubcommand() {
        return new CommandAPICommand("resetmax")
                .withArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    Player target = (Player) args.get("target");
                    // reset the bonus
                    this.userManager.getUser(target).setBonus(0);
                    // send message to the command sender
                    Common.configMessage("config.yml", sender, "messages.reset-max", new Placeholder()
                            .add("{player}", target.getName()));
                    // send message to the player
                    Common.configMessage("config.yml", target, "messages.max-resetted", new Placeholder()
                            .add("{current}", this.generatorManager.getGeneratorCount(target))
                            .add("{max}", this.userManager.getMaxSlot(target)));
                });
    }

    private CommandAPICommand getRepairSubcommand() {
        return new CommandAPICommand("repair")
                .withArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    Player target = (Player) args.get("target");
                    // repair the generators
                    Executor.async(() -> {
                        this.generatorManager.getActiveGenerator(target).forEach(active -> active.setCorrupted(false));
                        // send message to the command sender
                        Common.configMessage("config.yml", sender, "messages.player-repair", new Placeholder()
                                .add("{player}", target.getName()));
                        // send message to the player
                        Common.configMessage("config.yml", target, "messages.gens-repaired");
                    });
                });
    }

    private CommandAPICommand getReloadSubcommand() {
        return new CommandAPICommand("reload")
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    // actually reload the config
                    Config.reload();
                    // remove all holograms
                    GeneratorTask.flush();
                    // load back the generator
                    this.generatorManager.loadGenerators();
                    // refresh the active generator
                    Executor.async(this.generatorManager::refreshActiveGenerator);
                    // start back the auto save
                    this.generatorManager.startAutosaveTask();
                    // events stuff
                    this.eventManager.loadEvents();
                    this.eventManager.refresh();
                    // send message to the sender
                    Common.configMessage("config.yml", sender, "messages.reload");
                    // close all gui
                    SimpleInventoryManager.closeAll();
                });
    }

    private CommandAPICommand getSellwandSubcommand() {
        // gens sellwand <player> <multiplier> <uses>
        return new CommandAPICommand("sellwand")
                .withAliases("sellwands")
                .withArguments(new PlayerArgument("target"))
                .withArguments(new DoubleArgument("multiplier")
                        .replaceSuggestions(ArgumentSuggestions.strings("[<multiplier>]")))
                .withArguments(new IntegerArgument("uses")
                        .replaceSuggestions(ArgumentSuggestions.strings("[<uses>]")))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    Player target = (Player) args.get("target");
                    double multiplier = (double) args.get("multiplier");
                    int uses = (int) args.get("uses");
                    // give player the sellwand
                    Common.addInventoryItem(target, Sellwand.create(multiplier, uses));
                    // send message
                    Common.configMessage("config.yml", sender, "messages.sellwand-give", new Placeholder()
                            .add("{player}", target.getName())
                            .add("{multiplier}", Common.digits(multiplier))
                            .add("{uses}", Sellwand.getUsesPlaceholder(uses)));
                    Common.configMessage("config.yml", target, "messages.sellwand-receive", new Placeholder()
                            .add("{multiplier}", Common.digits(multiplier))
                            .add("{uses}", Sellwand.getUsesPlaceholder(uses)));
                });
    }

    private CommandAPICommand getStartEventCommand() {
        // gens startevent <event>
        return new CommandAPICommand("startevent")
                .withArguments(new StringArgument("event")
                        .replaceSuggestions(ArgumentSuggestions.strings(this.eventManager.getEventName())))
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    // if there is an event running
                    if (this.eventManager.getActiveEvent() != null) {
                        Common.configMessage("config.yml", sender, "messages.event-is-running");
                        return;
                    }
                    String eventId = (String) args.get("event");
                    Event event;
                    if (eventId.equalsIgnoreCase("random")) {
                        event = this.eventManager.getRandomEvent();
                    } else {
                        event = this.eventManager.getEvent(eventId);
                    }
                    // check if event is invalid
                    if (event == null) {
                        Common.configMessage("config.yml", sender, "messages.invalid-event");
                        return;
                    }
                    // actually start the event
                    this.eventManager.forceStart(event);
                    // send message
                    Common.configMessage("config.yml", sender, "messages.event-start", new Placeholder()
                            .add("{event}", event.getDisplayName()));
                });
    }

    private CommandAPICommand getStopEventCommand() {
        // gens stopevent
        return new CommandAPICommand("stopevent")
                .executes((sender, args) -> {
                    // permission check
                    if (!sender.hasPermission("nextgens.admin")) {
                        Common.configMessage("config.yml", sender, "messages.no-permission");
                        return;
                    }
                    if (this.eventManager.forceEnd()) {
                        Common.configMessage("config.yml", sender, "messages.event-stop");
                    } else {
                        Common.configMessage("config.yml", sender, "messages.no-event");
                    }
                });
    }

}
