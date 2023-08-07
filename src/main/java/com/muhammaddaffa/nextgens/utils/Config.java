package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.nextgens.NextGens;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {

    public static Config CONFIG, GENERATORS, UPGRADE_GUI, SHOP, CORRUPT_GUI, DATA;

    public static void init() {
        CONFIG = new Config("config.yml", null);
        GENERATORS = new Config("generators.yml", null);
        UPGRADE_GUI = new Config("upgrade_gui.yml", "gui");
        SHOP = new Config("shop.yml", null);
        CORRUPT_GUI = new Config("corrupt_gui.yml", "gui");
        DATA = new Config("data.yml", null);
    }

    public static void reload() {
        CONFIG.reloadConfig();
        GENERATORS.reloadConfig();
        UPGRADE_GUI.reloadConfig();
        SHOP.reloadConfig();
        CORRUPT_GUI.reloadConfig();
    }

    // -----------------------------------------------------------

    private final File file;
    private FileConfiguration config;

    public Config(String configName, String directory) {
        JavaPlugin plugin = NextGens.getInstance();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (directory == null) {
            file = new File(plugin.getDataFolder(), configName);

            if (!file.exists()) {
                plugin.saveResource(configName, false);
            }

        } else {
            File directoryFile = new File(plugin.getDataFolder() + File.separator + directory);
            if (!directoryFile.exists()) {
                directoryFile.mkdirs();
            }

            file = new File(plugin.getDataFolder() + File.separator + directory, configName);

            if (!file.exists()) {
                plugin.saveResource(directory + File.separator + configName, false);
            }

        }

        config = YamlConfiguration.loadConfiguration(file);

    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getString(String path) {
        return this.getConfig().getString(path);
    }

    public List<String> getStringList(String path) {
        return this.getConfig().getStringList(path);
    }

    public int getInt(String path) {
        return this.getConfig().getInt(path);
    }

    public List<Integer> getIntegerList(String path) {
        return this.getConfig().getIntegerList(path);
    }

    public double getDouble(String path) {
        return this.getConfig().getDouble(path);
    }

    public boolean getBoolean(String path) {
        return this.getConfig().getBoolean(path);
    }

    public long getLong(String path) {
        return this.getConfig().getLong(path);
    }

    public Location getLocation(String path) {
        return this.getConfig().getLocation(path);
    }

    public ItemStack getItemStack(String path) {
        return this.getConfig().getItemStack(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.getConfig().getConfigurationSection(path);
    }

    public boolean isConfigurationSection(String path) {
        return this.getConfig().isConfigurationSection(path);
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

}
