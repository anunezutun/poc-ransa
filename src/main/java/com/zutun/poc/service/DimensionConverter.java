package com.zutun.poc.service;

import com.zutun.poc.util.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DimensionConverter {

    public static BigDecimal convert(BigDecimal value, String from, String to, Integer decimals) {
        BigDecimal valueInMeters = toMeters(value, from, decimals);
        return fromMeters(valueInMeters, to, decimals);
    }

    private static BigDecimal fromMeters(BigDecimal value, String from, Integer decimals) {
        switch (from) {
            case "metro":
                return value;
            case "centimetro":
                return value.divide(Constants.CENTIMETER_FACTOR, decimals, RoundingMode.HALF_UP);
            case "milimetro":
                return value.divide(Constants.MILIMETER_FACTOR, decimals, RoundingMode.HALF_UP);
            case "yarda":
                return value.multiply(Constants.YARD_FACTOR)
                        .setScale(decimals, RoundingMode.HALF_UP);
            case "pie":
                return value.multiply(Constants.FOOT_FACTOR)
                        .setScale(decimals, RoundingMode.HALF_UP);
            case "pulgada":
                return value.multiply(Constants.INCH_FACTOR)
                        .setScale(decimals, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Unsupported unit: " + from);
        }
    }

    private static BigDecimal toMeters(BigDecimal value, String to, Integer decimals) {
        switch (to) {
            case "metro":
                return value;
            case "centimetro":
                return value.multiply(Constants.CENTIMETER_FACTOR)
                        .setScale(decimals, RoundingMode.HALF_UP);
            case "milimetro":
                return value.multiply(Constants.MILIMETER_FACTOR)
                        .setScale(decimals, RoundingMode.HALF_UP);
            case "yarda":
                return value.divide(Constants.YARD_FACTOR,decimals, RoundingMode.HALF_UP);
            case "pie":
                return value.divide(Constants.FOOT_FACTOR, decimals, RoundingMode.HALF_UP);
            case "pulgada":
                return value.divide(Constants.INCH_FACTOR, decimals, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Unsupported unit: " + to);
        }
    }
}