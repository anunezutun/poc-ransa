package com.zutun.poc.service;

import java.math.BigDecimal;

public class UnitConverter {

    public static void main(String[] args) {

        BigDecimal convertedValue = DimensionConverter.convert(new BigDecimal(3.6), "pie", "metro", 2);
        //System.out.println(convertedValue);

        var res = WeightConverter.convert(new BigDecimal(1), "kilogramo", "tonelada", 3);
        System.out.println(res);
    }
}