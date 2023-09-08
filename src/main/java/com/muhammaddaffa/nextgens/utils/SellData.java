package com.muhammaddaffa.nextgens.utils;

import com.muhammaddaffa.nextgens.sellwand.SellwandData;

public record SellData(
        double totalValue,
        int totalItems,
        double multiplier,
        SellwandData sellwandData
) {
}
