package me.ma1de.lock.util;

import lombok.experimental.UtilityClass;
import me.ma1de.lock.Lock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.concurrent.Future;

@UtilityClass
public class AsyncIO {
    public String readFile(File file) {
        if (!file.exists()) {
            return "";
        }

        Future<String> future = Lock.getInstance().getService().submit(() -> {
            StringBuilder buffer = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            }

            return buffer.toString();
        });

        boolean temp = false;

        while (!future.isDone()) {
            temp = !temp;
        }

        if (future.isCancelled() || !future.state().equals(Future.State.SUCCESS)) {
            return "";
        }

        try {
            return future.get();
        } catch (Exception ex) {
            Lock.getInstance().getLogger().warning("Unable to read " + file.getName() + ": " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
            return "";
        }
    }

    public void writeToFile(File file, String contents) throws Exception {
        if (!file.exists() && !file.createNewFile()) {
            return;
        }

        Lock.getInstance().getService().execute(() -> {
            try {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.write(contents);
                }
            } catch (Exception ex) {
                Lock.getInstance().getLogger().warning("Unable to write to " + file.getName() + ": " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
            }
        });
    }
}