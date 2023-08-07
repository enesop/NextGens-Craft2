package com.muhammaddaffa.nextgens.utils;

import com.cryptomorin.xseries.SkullUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Simple {@link ItemStack} builder
 *
 * @author MrMicky
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = Objects.requireNonNull(item, "item");
        this.meta = item.getItemMeta();

        if (this.meta == null) {
            throw new IllegalArgumentException("The type " + item.getType() + " doesn't support item meta");
        }
    }

    public ItemBuilder type(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemBuilder data(int data) {
        return durability((short) data);
    }

    @Deprecated
    public ItemBuilder durability(short durability) {
        this.item.setDurability(durability);
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        this.meta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder removeEnchants() {
        this.meta.getEnchants().keySet().forEach(this.meta::removeEnchant);
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        metaConsumer.accept(this.meta);
        return this;
    }

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> metaConsumer) {
        if (metaClass.isInstance(this.meta)) {
            metaConsumer.accept(metaClass.cast(this.meta));
        }
        return this;
    }

    public ItemBuilder name(String name) {
        this.meta.setDisplayName(Common.color(name));
        return this;
    }

    public ItemBuilder lore(String lore) {
        return lore(Collections.singletonList(lore));
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        this.meta.setLore(Common.color(lore));
        return this;
    }

    public ItemBuilder addLore(String line) {
        List<String> lore = this.meta.getLore();

        if (lore == null) {
            return lore(line);
        }

        lore.add(line);
        return lore(lore);
    }

    public ItemBuilder addLore(String... lines) {
        return addLore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(List<String> lines) {
        List<String> lore = this.meta.getLore();

        if (lore == null) {
            return lore(lines);
        }

        lore.addAll(lines);
        return lore(lore);
    }

    public ItemBuilder flags(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder flags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        this.meta.removeItemFlags(flags);
        return this;
    }

    public ItemBuilder removeFlags() {
        return removeFlags(ItemFlag.values());
    }

    public ItemBuilder armorColor(Color color) {
        return meta(LeatherArmorMeta.class, m -> m.setColor(color));
    }

    public ItemBuilder customModelData(int data){
        this.meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, String s){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, s);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Double d){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, d);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Float f){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.FLOAT, f);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Integer i){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Long l){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, l);
        return this;
    }

    public ItemBuilder pdc(NamespacedKey key, Byte b){
        this.meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, b);
        return this;
    }

    public ItemBuilder skull(String identifier){
        SkullUtils.applySkin(this.meta, identifier);
        return this;
    }

    public ItemBuilder skull(OfflinePlayer identifier){
        SkullUtils.applySkin(this.meta, identifier);
        return this;
    }

    public ItemBuilder skull(UUID identifier){
        SkullUtils.applySkin(this.meta, identifier);
        return this;
    }

    public ItemBuilder placeholder(Placeholder placeholder) {
        this.name(placeholder.translate(this.meta.getDisplayName()));
        if (this.meta.getLore() != null) {
            this.lore(placeholder.translate(this.meta.getLore()));
        }
        return this;
    }

    @Nullable
    public static ItemBuilder fromConfig(FileConfiguration config, String path) {
        return fromConfig(config, path, null);
    }

    @Nullable
    public static ItemBuilder fromConfig(FileConfiguration config, String path, @Nullable Placeholder placeholder) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return null;
        }
        return fromConfig(section, placeholder);
    }

    @Nullable
    public static ItemBuilder fromConfig(ConfigurationSection section) {
        return fromConfig(section, null);
    }

    @Nullable
    public static ItemBuilder fromConfig(ConfigurationSection section, @Nullable Placeholder placeholder) {
        if (section == null) {
            return null;
        }
        // get all the available variables
        String materialString = section.getString("material");
        Integer cmd = section.get("custom-model-data") == null ? null : section.getInt("custom-model-data");
        int amount = section.getInt("amount");
        String displayName = section.getString("display-name");
        boolean glowing = section.getBoolean("glowing");
        List<String> lore = section.getStringList("lore");

        // start building the itemstack
        ItemStack stack;
        if (materialString.contains(";")) {
            String value = materialString.split(";")[1];
            stack = new ItemStack(Material.PLAYER_HEAD, 1);
            // get the item meta of the item
            ItemMeta meta = stack.getItemMeta();
            // apply the custmo skull
            SkullUtils.applySkin(meta, value);
            // set the item meta of the item
            stack.setItemMeta(meta);
        } else {
            Material material = Material.matchMaterial(materialString);
            if (material == null) {
                material = Material.DIRT;
            }
            stack = new ItemStack(material, 1);
        }

        // create the itembuilder
        ItemBuilder builder = new ItemBuilder(stack);
        // set the amount
        builder.amount(Math.max(1, amount));
        // set the cmd
        if (cmd != null) {
            builder.customModelData(cmd);
        }
        // set the display name
        builder.name(displayName);
        // set the lore
        builder.lore(lore);
        // set the item flag
        builder.flags(ItemFlag.HIDE_ATTRIBUTES);
        // glowing set
        if (glowing) {
            builder.enchant(Enchantment.DURABILITY);
            builder.flags(ItemFlag.HIDE_ENCHANTS);
        }
        // set the placeholder
        if (placeholder != null) {
            builder.placeholder(placeholder);
        }

        return builder;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}
