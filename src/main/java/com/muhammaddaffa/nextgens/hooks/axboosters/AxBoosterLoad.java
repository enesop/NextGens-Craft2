package com.muhammaddaffa.nextgens.hooks.axboosters;

import com.artillexstudios.axboosters.api.AxBoostersAPI;
import com.artillexstudios.axboosters.api.events.AxBoostersLoadEvent;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.sell.multipliers.providers.AxBoostersSellMultiplierProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AxBoosterLoad implements Listener {

    private final JavaPlugin plugin;

    public AxBoosterLoad(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLoad(AxBoostersLoadEvent event) {
        final AxBoostersMultiplier multiplierHook = new AxBoostersMultiplier();
        final AxBoostersSpeed speedHook = new AxBoostersSpeed();

        plugin.getServer().getPluginManager().registerEvents(speedHook, plugin);

        AxBoostersAPI.registerBoosterHook(plugin, speedHook);
        AxBoostersAPI.registerBoosterHook(plugin, multiplierHook);

    }
}
