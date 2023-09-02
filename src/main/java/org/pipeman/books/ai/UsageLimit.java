package org.pipeman.books.ai;

import org.pipeman.books.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UsageLimit {
    public static final int LIMIT = 233_333;
    private static int reserved = 0;
    private static int lastReset;
    private static int usage;

    static {
        File file = new File("usage.txt");
        try {
            file.createNewFile();
            String content = Files.readString(file.toPath());
            String[] elements = content.split(" ");
            lastReset = Integer.parseInt(elements[0]);
            usage = Integer.parseInt(elements[1]);
        } catch (Exception e) {
            lastReset = Utils.getDay();
            usage = 0;
        }
    }

    public void use(int amount) {
        usage += amount;
    }

    public void reserve(int amount) {
        if (reserved + usage >= LIMIT) {
            throw new LimitExceededException();
        }
        reserved += amount;
    }

    public void deReserve(int amount) {
        reserved = Math.max(0, reserved - amount);
    }

    public static class LimitExceededException extends RuntimeException {
    }
}
