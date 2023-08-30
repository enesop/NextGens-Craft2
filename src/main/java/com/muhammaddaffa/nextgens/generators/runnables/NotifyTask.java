package com.muhammaddaffa.nextgens.generators.runnables;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NotifyTask extends BukkitRunnable {

    private static NotifyTask runnable;

    public static void start(GeneratorManager generatorManager) {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
        // set back the runnable
        runnable = new NotifyTask(generatorManager);
        // get the interval
        long interval = 20L * TimeUnit.MINUTES.toSeconds(Config.getFileConfiguration("config.yml").getInt("corruption.notify.interval"));
        // run the task
        runnable.runTaskTimerAsynchronously(NextGens.getInstance(), interval, interval);
    }

    private final GeneratorManager generatorManager;
    public NotifyTask(GeneratorManager generatorManager) {
        this.generatorManager = generatorManager;
    }

    @Override
    public void run() {
        // if corruption is not enabled, skip this
        if (!Config.getFileConfiguration("config.yml").getBoolean("corruption.enabled")) {
            return;
        }
        // create the map to store data
        Map<UUID, Integer> counter = new HashMap<>();
        // loop through all active generator
        for (ActiveGenerator active : this.generatorManager.getActiveGenerator()) {
            // if the generator is corrupted, store it
            if (active.isCorrupted()) {
                counter.put(active.getOwner(), counter.getOrDefault(active.getOwner(), 0) + 1);
            }
        }
        // after the data has been collected
        // proceed to notify the player if online
        counter.forEach((owner, amount) -> {
            Player player = Bukkit.getPlayer(owner);
            if (player == null) {
                return;
            }
            // send the message
            Common.configMessage("config.yml", player, "corruption.notify.messages", new Placeholder()
                    .add("{amount}", Common.digits(amount)));
            // play note pling sound
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        });
    }

}
