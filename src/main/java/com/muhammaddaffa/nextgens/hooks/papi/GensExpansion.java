package com.muhammaddaffa.nextgens.hooks.papi;

import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.generators.runnables.CorruptionTask;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.Common;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.TimeFormat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GensExpansion extends PlaceholderExpansion {

    private final GeneratorManager generatorManager;
    private final UserManager userManager;
    private final EventManager eventManager;

    public GensExpansion(GeneratorManager generatorManager, UserManager userManager, EventManager eventManager) {
        this.generatorManager = generatorManager;
        this.userManager = userManager;
        this.eventManager = eventManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nextgens";
    }

    @Override
    public @NotNull String getAuthor() {
        return "aglerr";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        if (params.equalsIgnoreCase("currentplaced")) {
            return Common.digits(this.generatorManager.getGeneratorCount(player));
        }

        if (params.equalsIgnoreCase("max")) {
            return Common.digits(this.userManager.getMaxSlot(player));
        }

        if (params.equalsIgnoreCase("total_generator")) {
            return Common.digits(this.generatorManager.getActiveGenerator().size());
        }

        if (params.equalsIgnoreCase("corrupt_time")) {
            return TimeFormat.parse(CorruptionTask.getTimeLeft());
        }

        if (params.equalsIgnoreCase("event_name")) {
            Event event = this.eventManager.getActiveEvent();
            if (event == null) {
                return Config.EVENTS.getString("events.placeholders.no-event");
            }
            return Config.EVENTS.getString("events.placeholders.active-event")
                    .replace("{display_name}", event.getDisplayName());
        }

        if (params.equalsIgnoreCase("event_time")) {
            Event event = this.eventManager.getActiveEvent();
            if (event == null) {
                return Config.EVENTS.getString("events.placeholders.no-event-timer")
                        .replace("{timer}", TimeFormat.parse((long) this.eventManager.getWaitTime()));
            }
            return Config.EVENTS.getString("events.placeholders.active-event-timer")
                    .replace("{timer}", TimeFormat.parse((long) event.getDuration()));
        }

        return null; // Placeholder is unknown by the Expansion
    }

}
