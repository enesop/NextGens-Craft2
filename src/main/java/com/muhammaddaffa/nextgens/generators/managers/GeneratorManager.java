package com.muhammaddaffa.nextgens.generators.managers;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.database.DatabaseManager;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Drop;
import com.muhammaddaffa.nextgens.generators.Generator;
import com.muhammaddaffa.nextgens.generators.runnables.GeneratorTask;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class GeneratorManager {

    private final Map<String, Generator> generatorMap = new HashMap<>();
    private final ConcurrentMap<String, ActiveGenerator> activeGenerators = new ConcurrentHashMap<>();

    private final Map<UUID, Integer> generatorCount = new HashMap<>();

    private BukkitTask saveTask;

    private final DatabaseManager dbm;
    public GeneratorManager(DatabaseManager dbm) {
        this.dbm = dbm;
    }

    @Nullable
    public Generator getGenerator(String id) {
        return this.generatorMap.get(id);
    }

    @Nullable
    public Generator getGenerator(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || stack.getItemMeta() == null) {
            return null;
        }
        // scrap the id from the item
        String id = stack.getItemMeta().getPersistentDataContainer().get(NextGens.generator_id, PersistentDataType.STRING);
        if (id == null) {
            return null;
        }
        return this.getGenerator(id);
    }

    public Set<String> getGeneratorIDs() {
        return this.generatorMap.keySet();
    }

    public boolean isGeneratorItem(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || stack.getItemMeta() == null) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(NextGens.generator_id, PersistentDataType.STRING);
    }

    public boolean isDropItem(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || stack.getItemMeta() == null) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(NextGens.drop_value, PersistentDataType.DOUBLE);
    }

    public Collection<Generator> getGenerators() {
        return this.generatorMap.values();
    }

    public int getGeneratorCount(Player player) {
        return this.getGeneratorCount(player.getUniqueId());
    }

    public int getGeneratorCount(UUID uuid) {
        return this.generatorCount.getOrDefault(uuid, 0);
    }

    public void addGeneratorCount(Player player, int amount) {
        this.addGeneratorCount(player.getUniqueId(), amount);
    }

    public void addGeneratorCount(UUID uuid, int amount) {
        this.generatorCount.put(uuid, this.generatorCount.getOrDefault(uuid, 0) + amount);
    }

    public void removeGeneratorCount(Player player, int amount) {
        this.removeGeneratorCount(player.getUniqueId(), amount);
    }

    public void removeGeneratorCount(UUID uuid, int amount) {
        this.generatorCount.put(uuid, this.generatorCount.getOrDefault(uuid, 0) - amount);
    }

    public Collection<ActiveGenerator> getActiveGenerator() {
        return this.activeGenerators.values();
    }

    @NotNull
    public List<ActiveGenerator> getActiveGenerator(Player player) {
        return this.getActiveGenerator(player.getUniqueId());
    }

    @NotNull
    public List<ActiveGenerator> getActiveGenerator(UUID uuid) {
        return this.getActiveGenerator().stream()
                .filter(active -> active.getOwner().equals(uuid))
                .collect(Collectors.toList());
    }

    @Nullable
    public ActiveGenerator getActiveGenerator(@Nullable Block block) {
        if (block == null) return null;
        return this.getActiveGenerator(block.getLocation());
    }

    @Nullable
    public ActiveGenerator getActiveGenerator(@NotNull Location location) {
        return this.getActiveGenerator(LocationSerializer.serialize(location));
    }

    @Nullable
    public ActiveGenerator getActiveGenerator(String serialized) {
        return this.activeGenerators.get(serialized);
    }

    public ActiveGenerator registerGenerator(Player owner, @NotNull Generator generator, @NotNull Block block) {
        return this.registerGenerator(owner.getUniqueId(), generator, block);
    }

    public ActiveGenerator registerGenerator(UUID owner, @NotNull Generator generator, @NotNull Block block) {
        ActiveGenerator active = this.getActiveGenerator(block);
        // check if block is already active generator or not
        if (active == null) {
            // register the new one
            String serialized = LocationSerializer.serialize(block.getLocation());
            active = new ActiveGenerator(owner, block.getLocation(), generator);
            this.activeGenerators.put(serialized, active);
            // add generator count
            this.addGeneratorCount(owner, 1);
        } else {
            // change the generator id
            active.setGenerator(generator);
            // set the block
            Executor.syncLater(2L, () -> block.setType(generator.item().getType()));
        }
        ActiveGenerator finalActive = active;
        // save the generator on the database
        Executor.async(() -> this.dbm.saveGenerator(finalActive));
        return active;
    }

    public void unregisterGenerator(@Nullable Block block) {
        if (block == null) return;
        this.unregisterGenerator(block.getLocation());
    }

    public void unregisterGenerator(Location location) {
        this.unregisterGenerator(LocationSerializer.serialize(location));
    }

    public void unregisterGenerator(String serialized) {
        ActiveGenerator removed = this.activeGenerators.remove(serialized);
        // check if the remove is successful
        if (removed != null) {
            // remove the corrupt status
            removed.setCorrupted(false);
            // force remove
            GeneratorTask.destroy(removed);
            // remove the generator count
            this.removeGeneratorCount(removed.getOwner(), 1);
            // remove the generator from the database
            Executor.async(() -> this.dbm.deleteGenerator(removed));
        }
    }

    public void loadActiveGenerator() {
        String query = "SELECT * FROM " + DatabaseManager.GENERATOR_TABLE;
        this.dbm.executeQuery(query, result -> {
            while (result.next()) {
                UUID owner = UUID.fromString(result.getString(1));
                String serialized = result.getString(2);
                Location location = LocationSerializer.deserialize(serialized);
                String generatorId = result.getString(3);
                double timer = result.getDouble(4);
                boolean isCorrupted = result.getBoolean(5);

                // store it on the map
                this.activeGenerators.put(serialized, new ActiveGenerator(owner, location, this.getGenerator(generatorId), timer, isCorrupted));
                // add generator count
                this.addGeneratorCount(owner, 1);
            }
            // send log message
            Logger.info("Successfully loaded " + this.activeGenerators.size() + " active generators!");
        });
    }

    public void saveActiveGenerator() {
        this.dbm.saveGenerator(this.activeGenerators.values());
    }

    public void loadGenerators() {
        // log message
        Logger.info("Starting to load all generators...");
        // load all generators files inside the 'generators' directory
        File directory = this.getMainDirectory();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    this.loadGenerators(YamlConfiguration.loadConfiguration(file));
                }
            }
        } else {
            directory.mkdirs();
        }
        // load generator from 'generators.yml'
        this.loadGenerators(Config.GENERATORS.getConfig());
        // send log message
        Logger.info("Successfully loaded " + this.generatorMap.size() + " generators!");
    }

    private void loadGenerators(FileConfiguration config) {
        // get all sections on the config
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            // load the generator
            this.loadGenerators(id, section);
        }
    }

    private void loadGenerators(String id, ConfigurationSection section) {
        // get all data
        String displayName = section.getString("display-name");
        int interval = section.getInt("interval");
        boolean corrupted = section.getBoolean("corrupted.enabled");
        double fixCost = section.getDouble("corrupted.cost");
        double corruptChance = section.getDouble("corrupted.chance");
        String nextTier = section.getString("upgrade.next-generator");
        double upgradeCost = section.getDouble("upgrade.upgrade-cost");

        ConfigurationSection itemSection = section.getConfigurationSection("item");
        if (itemSection == null) {
            Logger.warning("Failed to load generator '" + id + "'");
            Logger.warning("Reason: There is no generator item configuration!");
            return;
        }
        ItemBuilder builder = ItemBuilder.fromConfig(itemSection);
        if (builder == null) {
            Logger.warning("Failed to load generator '" + id + "'");
            Logger.warning("Reason: Failed to load the generator item configuration!");
            return;
        }
        ItemStack item = builder.build();
        List<Drop> drops = new ArrayList<>();

        if (section.isConfigurationSection("drops")) {
            for (String key : section.getConfigurationSection("drops").getKeys(false)) {
                ConfigurationSection dropSection = section.getConfigurationSection("drops." + key);
                if (dropSection == null) {
                    continue;
                }
                drops.add(Drop.fromConfig(dropSection));
            }
        }

        // store it on the map
        this.generatorMap.put(id, new Generator(id, displayName, interval, item, drops, nextTier, upgradeCost,
                corrupted, fixCost, corruptChance));
        // send log message
        Logger.info("Loaded generator '" + id + "'");
    }

    public void startAutosaveTask() {
        if (this.saveTask != null) {
            this.saveTask.cancel();
            this.saveTask = null;
        }
        // check for auto save enabled
        if (Config.CONFIG.getBoolean("auto-save.enabled")) {
            // get the interval
            int interval = Config.CONFIG.getInt("auto-save.interval");
            long timer = 20L * interval;
            // start the auto-save task
            this.saveTask = Executor.asyncTimer(timer, timer, this::saveActiveGenerator);
        }
    }

    private File getMainDirectory() {
        return new File(NextGens.getInstance().getDataFolder() + File.separator + "generators");
    }

}
