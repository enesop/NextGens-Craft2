package com.muhammaddaffa.nextgens.sell.multipliers.providers;

import com.muhammaddaffa.nextgens.sell.multipliers.SellMultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionSellMultiplierProvider implements SellMultiplierProvider {

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        return this.getSellMultiplier(player);
    }

    private double getSellMultiplier(Player player) {
        if (player == null) return 0;

        double multiplier = 0.0;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (!permission.startsWith("nextgens.multiplier.sell")) {
                continue;
            }
            try {
                String[] stripPermission = permission.split("\\.");
                String split = stripPermission[3].replace("_", ".");
                double current = Double.parseDouble(split);
                if (current > multiplier) {
                    multiplier = current;
                }
            } catch (Exception ignored) {

            }
        }
        // get the multiplier in decimals
        return multiplier;
    }

}
