package com.muhammaddaffa.nextgens.requirements.impl;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.nextgens.requirements.GensRequirement;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderRequirement extends GensRequirement {

    private final String message;
    private final String placeholder;
    private final String value;

    public PlaceholderRequirement(String message, String placeholder, String value) {
        this.message = message;
        this.placeholder = placeholder;
        this.value = value;
    }

    @Override
    public String getId() {
        return "PLACEHOLDER";
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isSuccessful(Player player) {
        String result = PlaceholderAPI.setPlaceholders(player, this.getPlaceholder());

        // Check if the result is an integer value
        if (Common.isInt(value) && Common.isInt(result)) {
            // If the result equals or more than the value, requirement is successful
            return Integer.parseInt(result) >= Integer.parseInt(value);
        }

        // Double value
        if (Common.isDouble(value) && Common.isDouble(result)) {
            // If the result is equals or more thn the value, return true
            return Double.parseDouble(result) >= Double.parseDouble(value);
        }

        return result.equalsIgnoreCase(this.getValue());
    }

}
