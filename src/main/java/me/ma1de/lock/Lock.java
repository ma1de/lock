package me.ma1de.lock;

import com.google.common.base.Stopwatch;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import me.ma1de.lock.data.DataEntry;
import me.ma1de.lock.data.DataHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class Lock extends JavaPlugin {
    @Getter(AccessLevel.PUBLIC)
    private static Lock instance;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private DataHandler dataHandler;

    @Override
    public void onEnable() {
        instance = this;

        Stopwatch stopwatch = Stopwatch.createStarted();

        this.dataHandler = new DataHandler();
        this.service.execute(() -> {
            try {
                this.dataHandler.onLoad();
                this.dataHandler.setInitialized(true);
            } catch (Exception ex) {
                this.getLogger().severe("Unable to initialize dataHandler: " + ex.getMessage() + " (" + ex.getClass() + ")");
                this.dataHandler.setInitialized(false);
            }
        });

        try {
            String dbName = Objects.requireNonNull(getConfig().getString("MONGO.DB"));

            MongoClientSettings.Builder builder = MongoClientSettings.builder()
                    .applyToClusterSettings(b -> b.hosts(Collections.singletonList(new ServerAddress(
                            getConfig().getString("MONGO.HOST"),
                            getConfig().getInt("MONGO.PORT")
                    ))));

            if (getConfig().getBoolean("MONGODB.AUTH.ENABLED")) {
                builder.credential(MongoCredential.createScramSha256Credential(
                        Objects.requireNonNull(getConfig().getString("MONGO.AUTH.USER")),
                        dbName,
                        Objects.requireNonNull(getConfig().getString("MONGO.AUTH.PASS")).toCharArray()));
            }

            this.mongoClient = MongoClients.create(builder.timeout(getConfig().getInt("MONGO.TIMEOUT", 5), TimeUnit.SECONDS).build());
            this.mongoDatabase = mongoClient.getDatabase(dbName);
        } catch (Exception ex) {
            getLogger().warning("Unable to initialize MongoDB: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
            return;
        }

        stopwatch.stop();

        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        getLogger().info("It took about " + elapsed + "ms to initialize the plugin.");

        this.dataHandler.getEntry("loadTimes").ifPresentOrElse(entry -> {
            List<Number> numbers = entry.getValueAs();
            numbers.add(elapsed);

            double maxValue = numbers.stream().mapToDouble(v -> (double) v).max().getAsDouble();

            if (maxValue < elapsed && (elapsed - maxValue) > 50) {
                this.getLogger().warning("Unusual behaviour, it took significantly more time to initialize the plugin than before. (probably not a bug, don't report it)");
            }

            this.dataHandler.addEntry(entry);
        }, () -> {
            List<Number> numbers = Collections.singletonList(elapsed);
            DataEntry entry = new DataEntry("loadTimes", numbers);

            this.dataHandler.addEntry(entry);
        });
    }

    @Override
    public void onDisable() {
        this.mongoClient.close();

        instance = null;
    }
}