package com.muhammaddaffa.nextgens.hooks.axboosters;

import com.artillexstudios.axboosters.hooks.booster.BoosterHook;
import com.artillexstudios.axboosters.libs.kyori.adventure.key.Key;
import com.artillexstudios.axboosters.users.User;
import com.artillexstudios.axboosters.users.UserList;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AxBoostersMultiplier implements BoosterHook {

    @Override
    public Key getKey() {
        return Key.key("nextgens", "sell_multiplier");
    }

    @Override
    public Material getIcon() {
        return Material.GOLD_INGOT;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public float getMultiplier(Player player) {
        User user = UserList.getUser(player);
        if (user == null) {
            return 1.0f;
        }
        return user.getBoost(this);
    }


}
