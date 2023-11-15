package com.muhammaddaffa.nextgens.autosell;

import com.muhammaddaffa.mdlib.utils.Common;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Autosell {

    /**
     * Permissions:
     * nextgens.autosell.inv.(interval in seconds)
     * nextgens.autosell.gens
     */

    public static int getAutosellInventoryInterval(Player player) {
        int interval = 0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith("nextgens.autosell.inv.")) {
                String split = permission.split("\\.")[3];
                if (!Common.isInt(split)) continue;
                int permissionInterval = Integer.parseInt(split);
                if (permissionInterval < interval) {
                    interval = permissionInterval;
                }
            }
        }
        return interval;
    }

    public static boolean hasAutosellInventoryPermission(Player player) {
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith("nextgens.autosell.inv.")) return true;
        }
        return false;
    }

    public static boolean hasAutosellGensPermission(Player player) {
        return player.hasPermission("nextgens.autosell.gens");
    }

}
