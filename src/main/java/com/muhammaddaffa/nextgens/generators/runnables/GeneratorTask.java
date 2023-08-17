package com.muhammaddaffa.nextgens.generators.runnables;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.events.Event;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.CorruptedHologram;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.Executor;
import com.muhammaddaffa.nextgens.utils.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class GeneratorTask extends BukkitRunnable {


    private static GeneratorTask runnable;

    public static void start(GeneratorManager generatorManager, EventManager eventManager) {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
        // set back the runnable
        runnable = new GeneratorTask(generatorManager, eventManager);
        // run the task
        runnable.runTaskTimerAsynchronously(NextGens.getInstance(), 20L, 2L);
    }

    public static void flush() {
        if (runnable == null || Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            return;
        }
        runnable.clearHologram();
    }

    public static void destroy(ActiveGenerator active) {
        if (runnable == null) {
            return;
        }
        runnable.forceRemoveHologram(active);
    }

    private final Map<String, CorruptedHologram> hologramMap = new HashMap<>();

    private final GeneratorManager generatorManager;
    private final EventManager eventManager;

    public GeneratorTask(GeneratorManager generatorManager, EventManager eventManager) {
        this.generatorManager = generatorManager;
        this.eventManager = eventManager;
    }

    @Override
    public void run() {
        // loop active generators
        for (ActiveGenerator active : this.generatorManager.getActiveGenerator()) {
            // get variables
            Generator generator = active.getGenerator();
            Player player = Bukkit.getPlayer(active.getOwner());
            Event event = this.eventManager.getActiveEvent();
            // if generator is invalid or chunk is not loaded, skip it
            if (generator == null || !active.isChunkLoaded()) {
                continue;
            }
            if (Config.CONFIG.getStringList("blacklisted-worlds").contains(active.getLocation().getWorld().getName())) {
                continue;
            }
            // check for online-only option
            if (Config.CONFIG.getBoolean("online-only")) {
                if (player == null || !player.isOnline()) {
                    continue;
                }
            }
            String serialized = LocationSerializer.serialize(active.getLocation());
            // check for corruption option
            if (Config.CONFIG.getBoolean("corruption.enabled") && active.isCorrupted()) {
                // check if hologram is enabled
                if (Config.CONFIG.getBoolean("corruption.hologram.enabled") && !this.hologramMap.containsKey(serialized)) {
                    CorruptedHologram hologram = new CorruptedHologram(active);
                    // show the hologram
                    hologram.spawn();
                    // store it on the cache
                    this.hologramMap.put(serialized, hologram);
                }
                continue;
            }
            // if the generator not corrupt but exists on the hologram map
            CorruptedHologram hologram = this.hologramMap.remove(serialized);
            if (!active.isCorrupted() && hologram != null) {
                hologram.destroy();
            }
            Generator chosenGenerator = generator;
            double interval = generator.interval();
            int dropAmount = 1;
            /**
             * Event-related code
             */
            if (event != null) {
                if (event.getType() == Event.Type.GENERATOR_SPEED &&
                        event.getSpeedMultiplier() != null &&
                        !event.getBlacklistedGenerators().contains(generator.id())) {
                    // get the speed boost
                    Double boost = event.getSpeedMultiplier();
                    double discount = (generator.interval() * boost) / 100;
                    // deduct the interval
                    interval = interval - discount;
                }
                if (event.getType() == Event.Type.GENERATOR_UPGRADE &&
                        event.getTierUpgrade() != null &&
                        !event.getBlacklistedGenerators().contains(generator.id())) {
                    // get the amount of tier upgrade
                    Integer amount = event.getTierUpgrade();
                    // make a for-each and upgrade the generator
                    for (int i = 0; i < amount; i++) {
                        if (chosenGenerator.nextTier() == null) {
                            break;
                        }
                        Generator upgraded = this.generatorManager.getGenerator(chosenGenerator.nextTier());
                        if (upgraded != null) {
                            chosenGenerator = upgraded;
                        }
                    }
                }
                if (event.getType() == Event.Type.MIXED_UP &&
                        !event.getBlacklistedGenerators().contains(generator.id())) {
                    // choose a random generator
                    chosenGenerator = this.generatorManager.getRandomGenerator();
                }
                if (event.getType() == Event.Type.DROP_MULTIPLIER &&
                        event.getDropMultiplier() != null &&
                        !event.getBlacklistedGenerators().contains(generator.id())) {
                    // get the drop multiplier and set the drop amount
                    dropAmount = Math.max(1, event.getDropMultiplier());
                }
            }
            // add timer
            active.addTimer(0.1);
            // check if the generator should drop
            if (active.getTimer() >= interval) {
                // set the timer back to 0
                active.setTimer(0);
                // get the final variable
                Generator finalGenerator = chosenGenerator;
                int finalDropAmount = dropAmount;
                // execute drop mechanics
                Block block = active.getLocation().getBlock();
                Executor.sync(() -> {
                    for (int i = 0; i < finalDropAmount; i++) {
                        finalGenerator.drop(block, active.getOwner());
                    }
                    // set the block to desired type
                    block.setType(generator.item().getType());
                });

            }
        }
    }

    public void forceRemoveHologram(ActiveGenerator active) {
        CorruptedHologram removed = this.hologramMap.remove(LocationSerializer.serialize(active.getLocation()));
        // if hologram is present, remove it
        if (removed != null) {
            removed.destroy();
        }
    }

    public void clearHologram() {
        for (CorruptedHologram hologram : this.hologramMap.values()) {
            hologram.destroy();
        }
        // clear the hologram map
        this.hologramMap.clear();
    }

}
