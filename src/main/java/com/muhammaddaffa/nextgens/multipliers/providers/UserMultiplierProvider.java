package com.muhammaddaffa.nextgens.multipliers.providers;

import com.muhammaddaffa.nextgens.multipliers.MultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.entity.Player;

public class UserMultiplierProvider implements MultiplierProvider {

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        return user.getMultiplier();
    }

}
