package com.muhammaddaffa.nextgens.sell.multipliers.providers;

import com.muhammaddaffa.nextgens.sell.multipliers.SellMultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.entity.Player;

public class SellwandSellMultiplierProvider implements SellMultiplierProvider {

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        return (sellwand != null) ? sellwand.getMultiplier() : 0;
    }

}
