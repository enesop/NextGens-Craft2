package com.muhammaddaffa.nextgens.requirements.impl;

import com.muhammaddaffa.nextgens.requirements.GensRequirement;
import org.bukkit.entity.Player;

public class PermissionRequirement extends GensRequirement {

    private final String message;
    private final String permission;

    public PermissionRequirement(String message, String permission) {
        this.message = message;
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String getId() {
        return "PERMISSION";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isSuccessful(Player player) {
        return player.hasPermission(this.getPermission());
    }

}
