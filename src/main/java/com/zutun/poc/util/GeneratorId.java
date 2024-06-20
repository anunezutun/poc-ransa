package com.zutun.poc.util;

import java.util.Random;

public class GeneratorId {
    public static String generateId() {
        String letters = "0123456789";
        Random random = new Random();
        StringBuilder uuid = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(letters.length());
            uuid.append(letters.charAt(index));
        }

        return "#" + uuid.toString();
    }
}