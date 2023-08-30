package com.muhammaddaffa.nextgens.generators.runnables;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CorruptionTask extends BukkitRunnable {

    private static CorruptionTask runnable;

    public static void start(GeneratorManager generatorManager) {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
        // set back the runnable
        runnable = new CorruptionTask(generatorManager);
        // run the task
        runnable.runTaskTimerAsynchronously(NextGens.getInstance(), 20L, 20L);
    }

    public static int getTimeLeft() {
        if (runnable == null) {
            return -1;
        }
        return runnable.getCorruptionTime() - runnable.getTimer();
    }

    private final GeneratorManager generatorManager;
    public CorruptionTask(GeneratorManager generatorManager) {
        this.generatorManager = generatorManager;
    }

    private int timer;

    @Override
    public void run() {
        // if corruption is disabled, skip this
        if (!Config.getFileConfiguration("config.yml").getBoolean("corruption.enabled")) {
            return;
        }
        // increase the timer
        this.timer++;
        // check if the timer exceed the interval
        if (this.timer >= this.getCorruptionTime()) {
            // set the timer back to 0
            this.timer = 0;
            // get possibly infected generators
            int actuallyCorrupted = 0;
            for (ActiveGenerator active : this.getPossiblyInfectedGenerators()) {
                // check for chances
                if (ThreadLocalRandom.current().nextDouble(101) <= active.getGenerator().corruptChance()) {
                    // actually set the generator to be corrupted
                    active.setCorrupted(true);
                    // increment the counter
                    actuallyCorrupted++;
                }
            }
            // broadcast the corrupt event
            if (actuallyCorrupted > 0) {
                Common.configBroadcast("config.yml", "corruption.broadcast", new Placeholder()
                        .add("{amount}", actuallyCorrupted));
            }
        }
    }

    private List<ActiveGenerator> getPossiblyInfectedGenerators() {
        // get the percentage
        int percentage = Config.getFileConfiguration("config.yml").getInt("corruption.percentage");
        // get total generators that will be infected
        List<ActiveGenerator> activeGenerators = this.generatorManager.getActiveGenerator()
                .stream()
                .filter(active -> !active.isCorrupted())
                .toList();
        int total = activeGenerators.size();
        int totalInfected = (total * percentage) / 100;
        // get random active generator
        List<String> blacklisted = Config.getFileConfiguration("config.yml").getStringList("corruption.blacklisted-generators");
        Set<Integer> checked = new HashSet<>();
        List<ActiveGenerator> corrupted = new ArrayList<>();

        while (corrupted.size() < totalInfected) {
            // get random active generator
            int index = ThreadLocalRandom.current().nextInt(total);
            if (checked.contains(index)) {
                continue;
            }
            ActiveGenerator active = activeGenerators.get(index);
            // blacklist check, or corrupted check
            if (blacklisted.contains(active.getGenerator().id()) || active.isCorrupted()) {
                continue;
            }
            // proceed to corrupt the generator
            corrupted.add(active);
            // add the random number
            checked.add(index);
        }

        return corrupted;
    }

    public int getTimer() {
        return timer;
    }

    public int getCorruptionTime() {
        return (int) TimeUnit.MINUTES.toSeconds(Config.getFileConfiguration("config.yml").getInt("corruption.interval"));
    }

}
