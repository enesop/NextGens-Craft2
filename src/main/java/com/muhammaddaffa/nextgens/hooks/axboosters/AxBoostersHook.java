package com.muhammaddaffa.nextgens.hooks.axboosters;

import org.bukkit.entity.Player;

public class AxBoostersHook implements AxBoostersWrapper {

    @Override
    public float getSpeedBoost(Player player) {
        com.artillexstudios.axboosters.users.User boosterUser = com.artillexstudios.axboosters.users.UserList.getUser(player);
        return boosterUser != null ? boosterUser.getBoost(AxBoosterSpeedListener.INSTANCE) : 1.0f;
    }

}
