package com.zutun.poc.service;

import com.zutun.poc.util.Constants;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeightConverter {

    public static BigDecimal convert(BigDecimal value, String from, String to, Integer decimals) {
        BigDecimal valueInMeters = toKilograms(value, from, decimals);
        return fromKilograms(valueInMeters, to, decimals);
    }

    private static BigDecimal fromKilograms(BigDecimal value, String from, Integer decimals) {
        switch (from) {
            case "kilogramo":
                return value;
            case "tonelada":
                return value.divide(Constants.TON_FACTOR, decimals, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Unsupported unit: " + from);
        }
    }

    private static BigDecimal toKilograms(BigDecimal value, String to, Integer decimals) {
        switch (to) {
            case "kilogramo":
                return value;
            case "tonelada":
                return value.multiply(Constants.TON_FACTOR)
                        .setScale(decimals, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Unsupported unit: " + to);
        }
    }
}