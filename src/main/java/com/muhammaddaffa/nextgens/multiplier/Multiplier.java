package com.muhammaddaffa.nextgens.multiplier;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Multiplier {

    public static int getSellMultiplier(Player player) {
        int max = 0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (!permission.startsWith("nextgens.multiplier.sell")) {
                continue;
            }
            int current = Integer.parseInt(permission.split("\\.")[3]);
            if (current > max) {
                max = current;
            }
        }
        // get the bonus slot
        return max;
    }

}
