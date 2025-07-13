package com.muhammaddaffa.nextgens.hooks.axboosters;

import com.artillexstudios.axboosters.boosters.BoosterManager;
import com.artillexstudios.axboosters.hooks.booster.BoosterHook;
import com.artillexstudios.axboosters.libs.kyori.adventure.key.Key;
import com.artillexstudios.axboosters.users.User;
import com.artillexstudios.axboosters.users.UserList;
import com.muhammaddaffa.nextgens.api.events.sell.SellEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class AxBoosterSellListener implements BoosterHook, Listener {

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

    @EventHandler
    public void onSell(@NotNull SellEvent event) {
        /*if (!(event.getPlayer().isOnline())) return;

        User user = UserList.getUser(event.getPlayer());
        if (user == null) return;

        float boost = user.getBoost(this);
        if (boost == 1.0f) return;

        double boostedWorth = BoosterManager.multiply(boost, event.getMultiplier());
        event.setMultiplier(boostedWorth);*/
    }
}
