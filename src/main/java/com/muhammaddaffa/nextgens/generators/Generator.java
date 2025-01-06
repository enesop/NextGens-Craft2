package com.muhammaddaffa.nextgens.generators;

import com.muhammaddaffa.mdlib.utils.ItemBuilder;
import com.muhammaddaffa.nextgens.NextGens;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        double corruptChance,
        Boolean onlineOnly
) {

    public Drop getRandomDrop() {
        for (Drop drop : this.drops)
            if (drop.shouldUse())
                return drop;

        return null;
    }

    @Nullable
    public Drop getDrop(String id) {
        return this.drops.stream()
                .filter(drop -> drop.id().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
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

    public void addDrop(Drop drop) {
        this.drops.add(drop);
    }

}
