package me.ma1de.lock.data;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import me.ma1de.lock.Lock;
import me.ma1de.lock.util.AsyncIO;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Getter
public class DataHandler {
    private final File file = new File(Lock.getInstance().getDataFolder(), "data.json");
    private final List<DataEntry> entries = Lists.newArrayList();

    @Setter private boolean initialized;

    public Optional<DataEntry> getEntry(String id) {
        if (!initialized) {
            return Optional.empty();
        }

        return entries.stream().filter(entry -> entry.getId().equalsIgnoreCase(id)).findAny();
    }

    public void addEntry(DataEntry entry) {
        if (!initialized) {
            Lock.getInstance().getLogger().warning("Attempted to save data, but dataHandler isn't initialized. Stop the plugin");
            return;
        }

        this.getEntry(entry.getId()).ifPresent(entries::remove);
        this.entries.add(entry);
    }

    public void onLoad() throws Exception {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new Exception("Unable to create a new data.json file");
            }

            AsyncIO.writeToFile(file, "[]");
        }

        List<DataEntry> parsedEntries = Lock.getInstance().getGson().fromJson(AsyncIO.readFile(file), new TypeToken<List<DataEntry>>() {}.getType());

        if (parsedEntries == null || parsedEntries.isEmpty()) {
            return;
        }

        this.entries.addAll(parsedEntries);
    }

    public void onShutdown() {
        try {
            AsyncIO.writeToFile(file, Lock.getInstance().getGson().toJson(entries));
        } catch (Exception ex) {
            Lock.getInstance().getLogger().warning("Unable to save data.json: " + ex.getMessage() + " (" + ex.getClass() + ")");
        }
    }
}