package com.muhammaddaffa.nextgens.sellwand.hooks;

import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.muhammaddaffa.nextgens.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WTHook implements PricesProvider {

    @Override
    public double getPrice(Player player, ItemStack stack) {
        return Utils.getPriceValue(player, stack);
    }

}
