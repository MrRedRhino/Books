package org.pipeman.books.ai;

import org.pipeman.books.Main;
import org.pipeman.books.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UsageLimit {
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
        if (amount == 0) return;

        int currentDay = Utils.getDay();
        if (lastReset < currentDay) {
            lastReset = currentDay;
            usage = 0;
        }
        usage += amount;
        save();
    }

    private void save() {
        try {
            Files.writeString(Path.of("usage.txt"), lastReset + " " + usage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reserve(int amount) {
        if (reserved + usage >= Main.config().dailyUsageLimit) {
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
