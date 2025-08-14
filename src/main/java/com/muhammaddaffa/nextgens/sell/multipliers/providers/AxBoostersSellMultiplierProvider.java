package com.muhammaddaffa.nextgens.sell.multipliers.providers;

import com.artillexstudios.axboosters.hooks.booster.BoosterHook;
import com.artillexstudios.axboosters.libs.kyori.adventure.key.Key;
import com.artillexstudios.axboosters.users.UserList;
import com.muhammaddaffa.nextgens.hooks.axboosters.AxBoostersMultiplier;
import com.muhammaddaffa.nextgens.sell.multipliers.SellMultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AxBoostersSellMultiplierProvider implements SellMultiplierProvider {

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        AxBoostersMultiplier hook = new AxBoostersMultiplier();
        return hook.getMultiplier(player);
    }
}
