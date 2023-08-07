package com.muhammaddaffa.nextgens.hooks.papi;

import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.Common;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GensExpansion extends PlaceholderExpansion {

    private final GeneratorManager generatorManager;
    private final UserManager userManager;

    public GensExpansion(GeneratorManager generatorManager, UserManager userManager) {
        this.generatorManager = generatorManager;
        this.userManager = userManager;
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

        return null; // Placeholder is unknown by the Expansion
    }

}
