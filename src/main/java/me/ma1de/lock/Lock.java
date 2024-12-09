package me.ma1de.lock;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.spi.dns.DnsClient;
import com.mongodb.spi.dns.DnsClientProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collections;
import java.util.Objects;

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

        // TODO improve this
        try {
            String host = getConfig().getString("MONGO.HOST");
            int port = getConfig().getInt("MONGO.PORT");

            String dbName = Objects.requireNonNull(getConfig().getString("MONGO.DB"));
            boolean authEnabled = getConfig().getBoolean("MONGODB.AUTH.ENABLED");

            if (authEnabled) {
                this.mongoClient = MongoClients.create(
                        MongoClientSettings.builder()
                                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                                .credential(MongoCredential.createScramSha256Credential(
                                        Objects.requireNonNull(getConfig().getString("MONGO.AUTH.USER")),
                                        dbName,
                                        Objects.requireNonNull(getConfig().getString("MONGO.AUTH.PASS")).toCharArray()))
                                .build()
                );
            } else {
                this.mongoClient = MongoClients.create(
                        MongoClientSettings.builder()
                                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                                .build()
                );
            }

            this.mongoDatabase = mongoClient.getDatabase(dbName);
        } catch (Exception ex) {
            getLogger().warning("Unable to initialize MongoDB: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
        }
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}