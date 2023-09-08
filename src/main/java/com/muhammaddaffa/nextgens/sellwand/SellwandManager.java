package com.muhammaddaffa.nextgens.sellwand;

import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.ItemBuilder;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.events.managers.EventManager;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import com.muhammaddaffa.nextgens.utils.SellData;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public record SellwandManager(
        UserManager userManager
) {

    public String getUsesPlaceholder(int uses) {
        if (uses == -1) {
            return Config.getFileConfiguration("config.yml").getString("sellwand.unlimited-placeholder");
        }
        return Common.digits(uses);
    }

    public boolean isSellwand(ItemStack stack) {
        if (stack == null || stack.getItemMeta() == null) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(NextGens.sellwand_global, PersistentDataType.STRING);
    }

    public boolean action(Player player, ItemStack stack, Inventory inventory) {
        // scrap the data from the item
        ItemBuilder builder = new ItemBuilder(stack);
        ItemMeta meta = builder.getItemMeta();
        Double multiplier = meta.getPersistentDataContainer().get(NextGens.sellwand_multiplier, PersistentDataType.DOUBLE);
        Integer uses = meta.getPersistentDataContainer().get(NextGens.sellwand_uses, PersistentDataType.INTEGER);
        Double totalSold = meta.getPersistentDataContainer().get(NextGens.sellwand_total_sold, PersistentDataType.DOUBLE);
        Integer totalItemsSold = meta.getPersistentDataContainer().get(NextGens.sellwand_total_items, PersistentDataType.INTEGER);
        // if one of the data is null, skip it
        if (multiplier == null || uses == null || totalSold == null || totalItemsSold == null) {
            return false;
        }
        // perform the sell
        SellData data = this.userManager.performSell(player, inventory, new SellwandData(stack, multiplier));
        if (data == null) return true;
        // final uses
        int finalUses = uses - 1;
        // if the final uses is 0
        if (finalUses <= 0) {
            // destroy the item
            stack.setAmount(0);
            // send message
            Common.configMessage("config.yml", player, "messages.sellwand-broke");
            // play break sound
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            return true;
        }
        // set back the pdc
        if (uses > 0) {
            builder.pdc(NextGens.sellwand_uses, finalUses);
        }
        builder.pdc(NextGens.sellwand_total_sold, totalSold + data.totalValue());
        builder.pdc(NextGens.sellwand_total_items, totalItemsSold + data.totalItems());
        // update the item
        update(builder.build());
        return true;
    }

    public void update(ItemStack stack) {
        if (stack == null) return;
        // update the item
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        ItemBuilder builder = new ItemBuilder(stack);
        // get the multiplier and uses
        ItemMeta meta = builder.getItemMeta();
        Double multiplier = meta.getPersistentDataContainer().get(NextGens.sellwand_multiplier, PersistentDataType.DOUBLE);
        Integer uses = meta.getPersistentDataContainer().get(NextGens.sellwand_uses, PersistentDataType.INTEGER);
        Double totalSold = meta.getPersistentDataContainer().get(NextGens.sellwand_total_sold, PersistentDataType.DOUBLE);
        Integer totalItemsSold = meta.getPersistentDataContainer().get(NextGens.sellwand_total_items, PersistentDataType.INTEGER);
        // if one of the data is null, skip it
        if (multiplier == null || uses == null || totalSold == null || totalItemsSold == null) {
            return;
        }
        // set the display name and lore back
        builder.name(config.getString("sellwand.item.display-name"));
        builder.lore(config.getStringList("sellwand.item.lore"));
        builder.placeholder(new Placeholder()
                .add("{multiplier}", Common.digits(multiplier))
                .add("{uses}", getUsesPlaceholder(uses))
                .add("{total_sold}", Common.digits(totalSold))
                .add("{total_items}", Common.digits(totalItemsSold)));
        // set the item meta
        builder.build();
    }

    public ItemStack create(double multiplier, int uses) {
        // create the item
        ItemBuilder builder = ItemBuilder.fromConfig(Config.getFileConfiguration("config.yml"), "sellwand.item");
        builder.pdc(NextGens.sellwand_global, UUID.randomUUID().toString());
        builder.pdc(NextGens.sellwand_multiplier, multiplier);
        builder.pdc(NextGens.sellwand_uses, uses);
        builder.pdc(NextGens.sellwand_total_sold, 0D);
        builder.pdc(NextGens.sellwand_total_items, 0);
        builder.placeholder(new Placeholder()
                .add("{multiplier}", Common.digits(multiplier))
                .add("{uses}", getUsesPlaceholder(uses))
                .add("{total_sold}", Common.digits(0))
                .add("{total_items}", Common.digits(0)));

        return builder.build();
    }

}
