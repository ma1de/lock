package me.ma1de.lock;

import com.google.common.base.Stopwatch;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class Lock extends JavaPlugin {
    @Getter(AccessLevel.PUBLIC)
    private static Lock instance;

    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    @Override
    public void onEnable() {
        instance = this;

        Stopwatch stopwatch = Stopwatch.createStarted();

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
        }

        stopwatch.stop();
        getLogger().info("It took about " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms to initialize the plugin.");
    }

    @Override
    public void onDisable() {
        this.mongoClient.close();

        instance = null;
    }
}