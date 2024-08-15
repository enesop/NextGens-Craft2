package com.muhammaddaffa.nextgens.hooks.bento;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.refund.RefundManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.api.events.island.*;
import world.bentobox.bentobox.api.events.team.TeamJoinEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;

import java.util.List;
import java.util.UUID;

public record BentoListener(
        GeneratorManager generatorManager,
        RefundManager refundManager
) implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onIslandDelete(IslandPreclearEvent event) {
        event.getOldIsland().getMemberSet().forEach(this::check);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onTeamKick(TeamKickEvent event) {
        this.check(event.getPlayerUUID());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onTeamJoin(TeamJoinEvent event) {
        this.check(event.getPlayerUUID());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onTeamLeave(TeamLeaveEvent event) {
        this.check(event.getPlayerUUID());
    }

    private void check(UUID uuid) {
        // get all variables we need
        Player player = Bukkit.getPlayer(uuid);
        List<ActiveGenerator> generators = this.generatorManager.getActiveGenerator(uuid);
        // loop through them all
        for (ActiveGenerator active : generators) {
            // unregister the generator
            this.generatorManager.unregisterGenerator(active.getLocation());
            // set the block to air
            active.getLocation().getBlock().setType(Material.AIR);
            // check for island pickup option
            if (NextGens.DEFAULT_CONFIG.getConfig().getBoolean("island-pickup")) {
                // give the generator back
                if (player == null) {
                    // if player not online, register it to item join
                    this.refundManager.delayedGiveGeneratorItem(uuid, active.getGenerator().id());
                } else {
                    // if player is online, give them the generators
                    Common.addInventoryItem(player, active.getGenerator().createItem(1));
                }
            }
        }
    }

}
