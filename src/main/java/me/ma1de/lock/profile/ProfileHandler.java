package me.ma1de.lock.profile;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import me.ma1de.lock.Lock;
import org.bson.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class ProfileHandler {
    private final MongoCollection<Document> collection = Lock.getInstance().getMongoDatabase().getCollection("profiles");
    private final List<Profile> profiles = Lists.newArrayList();

    public Optional<Profile> getProfile(UUID uuid) {
        return profiles.stream().filter(profile -> profile.getUuid().equals(uuid)).findAny();
    }

    public void onLoad() {
        for (Document doc : this.collection.find()) {
            this.profiles.add(Lock.getInstance().getGson().fromJson(doc.toJson(), new TypeToken<Profile>() {}.getType()));
        }
    }

    public void onShutdown() {
        this.profiles.forEach(profile -> {
            if (collection.countDocuments(Filters.eq("uuid", profile.getUuid().toString())) != 0) {
                return;
            }

            collection.insertOne(Document.parse(Lock.getInstance().getGson().toJson(profile)));
        });
    }
}