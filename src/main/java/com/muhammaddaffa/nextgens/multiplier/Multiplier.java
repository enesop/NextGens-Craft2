package com.muhammaddaffa.nextgens.multiplier;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Multiplier {

    public static double getSellMultiplier(Player player) {
        if (player == null) return 0;

        int multiplier = 0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (!permission.startsWith("nextgens.multiplier.sell")) {
                continue;
            }
            int current = Integer.parseInt(permission.split("\\.")[3]);
            if (current > multiplier) {
                multiplier = current;
            }
        }
        // get the multiplier in decimals
        return ((double) multiplier / 100);
    }

}
