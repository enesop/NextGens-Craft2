package com.muhammaddaffa.nextgens.sell;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.multipliers.MultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import com.muhammaddaffa.nextgens.utils.SellData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SellDataCalculator {

    public static SellData calculateSellData(Player player, User user, SellwandData sellwand, double totalValue, int totalItems) {
        double totalMultiplier = 0;

        // Get all multipliers
        for (MultiplierProvider provider : NextGens.getInstance().getMultiplierRegistry().getMultipliers()) {
            double multiplier = provider.getMultiplier(player, user, sellwand);
            if (multiplier > 0) {
                totalMultiplier += multiplier;
            }
        }

        // Apply multiplier limit if needed
        FileConfiguration config = NextGens.DEFAULT_CONFIG.getConfig();
        if (config.getBoolean("player-multiplier-limit.enabled")) {
            double limit = config.getDouble("player-multiplier-limit.limit");
            if (totalMultiplier > limit) {
                totalMultiplier = limit;
            }
        }

        double finalAmount;
        if (totalMultiplier < 1) {
            finalAmount = totalValue * (totalMultiplier + 1);
        } else {
            finalAmount = totalValue * totalMultiplier;
        }

        return new SellData(user, finalAmount, totalItems, totalMultiplier, sellwand);
    }

}
