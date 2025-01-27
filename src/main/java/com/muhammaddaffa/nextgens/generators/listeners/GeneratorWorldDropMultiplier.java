package com.muhammaddaffa.nextgens.generators.listeners;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.api.events.generators.GeneratorGenerateItemEvent;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GeneratorWorldDropMultiplier implements Listener {

    @EventHandler
    private void onGenerateItem(GeneratorGenerateItemEvent event) {
        ActiveGenerator active = event.getActiveGenerator();
        String worldName = active.getLocation().getWorld().getName();
        int dropAmount = NextGens.DEFAULT_CONFIG.getInt("world-multipliers." + worldName + ".drop-multiplier");
        // Set the drop amount if it's greater than zero
        if (dropAmount > 0) {
            event.setDropAmount(event.getDropAmount() + dropAmount);
        }
    }

}
