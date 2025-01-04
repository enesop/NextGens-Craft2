package com.muhammaddaffa.nextgens.multipliers.providers;

import com.muhammaddaffa.nextgens.multipliers.MultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionMultiplierProvider implements MultiplierProvider {

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        return this.getSellMultiplier(player);
    }

    private double getSellMultiplier(Player player) {
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
