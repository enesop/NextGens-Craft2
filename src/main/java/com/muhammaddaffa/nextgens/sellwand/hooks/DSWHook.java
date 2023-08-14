package com.muhammaddaffa.nextgens.sellwand.hooks;

import com.muhammaddaffa.nextgens.utils.Utils;
import dev.norska.dsw.prices.DSWPriceHandlerInterface;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DSWHook implements DSWPriceHandlerInterface {

    @Override
    public Double getItemWorth(Player player, ItemStack stack, int i) {
        return Utils.getPriceValue(player, stack);
    }

}
