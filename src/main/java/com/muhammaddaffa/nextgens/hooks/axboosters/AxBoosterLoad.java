package com.muhammaddaffa.nextgens.hooks.axboosters;

import com.artillexstudios.axboosters.api.AxBoostersAPI;
import com.artillexstudios.axboosters.api.events.AxBoostersLoadEvent;
import com.muhammaddaffa.nextgens.NextGens;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AxBoosterLoad implements Listener {

    private final NextGens plugin;

    public AxBoosterLoad(NextGens plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLoad(AxBoostersLoadEvent event) {
        AxBoosterSpeedListener speedHook = new AxBoosterSpeedListener();
        plugin.getServer().getPluginManager().registerEvents(speedHook, plugin);
        AxBoostersAPI.registerBoosterHook(plugin, speedHook);
    }
}
