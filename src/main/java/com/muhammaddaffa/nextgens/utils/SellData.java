package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.nextgens.sellwand.SellwandData;
import com.muhammaddaffa.nextgens.users.User;

public record SellData(
        User user,
        double totalValue,
        int totalItems,
        double multiplier,
        SellwandData sellwandData
) {
}
