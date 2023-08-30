package com.muhammaddaffa.nextgens.generators;

import com.muhammaddaffa.mdlib.utils.ItemBuilder;
import com.muhammaddaffa.nextgens.NextGens;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public record Generator(
        String id,
        String displayName,
        double interval,
        ItemStack item,
        List<Drop> drops,
        String nextTier,
        double cost,
        boolean corrupted,
        double fixCost,
        double corruptChance
) {

    public void drop(Block block, UUID uuid) {
        // drop algorithm
        boolean check = false;

        while (!check) {
            for (Drop drop : this.drops()) {
                if (drop.tryDrop(block, Bukkit.getOfflinePlayer(uuid))) {
                    check = true;
                    break;
                }
            }
        }

    }

    public ItemStack createItem(int amount) {
        ItemBuilder builder = new ItemBuilder(this.item().clone());
        // apply data to the item
        builder.pdc(NextGens.generator_id, this.id());
        // set the amount
        ItemStack stack = builder.build();
        stack.setAmount(Math.max(1, amount));

        return stack;
    }

}
