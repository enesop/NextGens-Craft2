package com.muhammaddaffa.nextgens.requirements.impl;

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
        return result.equalsIgnoreCase(this.getValue());
    }

}
