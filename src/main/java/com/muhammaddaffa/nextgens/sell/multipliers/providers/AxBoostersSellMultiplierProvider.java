package com.muhammaddaffa.nextgens.sell.multipliers.providers;

import com.artillexstudios.axboosters.hooks.booster.BoosterHook;
import com.artillexstudios.axboosters.libs.kyori.adventure.key.Key;
import com.artillexstudios.axboosters.users.UserList;
import com.muhammaddaffa.nextgens.sell.multipliers.SellMultiplierProvider;
import com.muhammaddaffa.nextgens.sellwand.models.SellwandData;
import com.muhammaddaffa.nextgens.users.models.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AxBoostersSellMultiplierProvider implements SellMultiplierProvider {

    private static final BoosterHook HOOK = new BoosterHook() {
        @Override
        public Key getKey() {
            return Key.key("nextgens", "sell_multiplier");
        }

        @Override
        public org.bukkit.Material getIcon() {
            return org.bukkit.Material.GOLD_INGOT;
        }

        @Override
        public boolean isPersistent() {
            return true;
        }
    };

    @Override
    public double getMultiplier(Player player, User user, SellwandData sellwand) {
        if (!Bukkit.getPluginManager().isPluginEnabled("AxBoosters")) return 0;
        var boosterUser = UserList.getUser(player);
        if (boosterUser == null) return 0;
        float boost = boosterUser.getBoost(HOOK);
        return (boost > 1.0f) ? (boost - 1.0f) : 0;
    }
}
