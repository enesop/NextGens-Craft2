package com.muhammaddaffa.nextgens.generators.action;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public enum InteractAction {
    LEFT,
    SHIFT_LEFT,
    RIGHT,
    SHIFT_RIGHT;


    public static InteractAction find(PlayerInteractEvent event, InteractAction fallback) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                return SHIFT_LEFT;
            }
            return LEFT;
        }
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                return SHIFT_RIGHT;
            }
            return RIGHT;
        }
        return fallback;
    }

    public static InteractAction find(String name, InteractAction fallback) {
        switch (name.toLowerCase()) {
            case "left" -> {
                return LEFT;
            }
            case "shift_left" -> {
                return SHIFT_LEFT;
            }
            case "right" -> {
                return RIGHT;
            }
            case "shift_right" -> {
                return SHIFT_RIGHT;
            }
        }
        return fallback;
    }

}
