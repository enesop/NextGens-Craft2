package com.muhammaddaffa.nextgens.utils;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class FormatBalance {

    private final NavigableMap<Long, String> suffixes = new TreeMap<>();

    public FormatBalance() {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "Q");
    }

    public String format(long value){
        if(value == Long.MIN_VALUE)
            return format(Long.MIN_VALUE + 1);

        if(value < 0)
            return "-" + format(-value);

        if(value < 1000)
            return Long.toString(value);

        Map.Entry<Long, String> entry = suffixes.floorEntry(value);
        long divideBy = entry.getKey();
        String suffix = entry.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);

        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

}
