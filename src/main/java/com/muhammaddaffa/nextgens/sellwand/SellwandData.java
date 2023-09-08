package com.muhammaddaffa.nextgens.sellwand;

import org.bukkit.inventory.ItemStack;

public record SellwandData(
        ItemStack stack,
        double multiplier
) {
}
