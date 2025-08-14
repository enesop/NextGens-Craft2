package com.muhammaddaffa.nextgens.hooks.axboosters;

import com.artillexstudios.axboosters.boosters.BoosterManager;
import com.artillexstudios.axboosters.hooks.booster.BoosterHook;
import com.artillexstudios.axboosters.libs.kyori.adventure.key.Key;
import com.artillexstudios.axboosters.users.User;
import com.artillexstudios.axboosters.users.UserList;
import com.muhammaddaffa.nextgens.api.events.generators.GeneratorGenerateItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AxBoostersSpeed implements Listener, BoosterHook {

    @Override
    public Key getKey() {
        return Key.key("nextgens", "speed_multiplier");
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
    public void onSpeed(@NotNull GeneratorGenerateItemEvent event) {
        UUID uuid = event.getActiveGenerator().getOwner();
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;

        User user = UserList.getUser(player);
        if (user == null) {
            return;
        }

        float boost = user.getBoost(this);

        if (boost == 1.0f) return;

        double before = event.getTimer();
        double boosted = BoosterManager.multiply(boost, before);
        if (boosted == before) return;

        event.setTimer(boosted);
    }

    @Override
    public void apply(User user) {
    }

    @Override
    public void unapply(User user) {
    }

}
