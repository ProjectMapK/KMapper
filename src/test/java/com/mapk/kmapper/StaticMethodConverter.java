package com.mapk.kmapper;

import com.mapk.annotations.KConverter;

import java.util.Arrays;

public class StaticMethodConverter {
    private final int[] arg;

    private StaticMethodConverter(int[] arg) {
        this.arg = arg;
    }

    public int[] getArg() {
        return arg;
    }

    @KConverter
    private static StaticMethodConverter converter(String csv) {
        int[] arg = Arrays.stream(csv.split(",")).mapToInt(Integer::valueOf).toArray();
        return new StaticMethodConverter(arg);
    }
}
