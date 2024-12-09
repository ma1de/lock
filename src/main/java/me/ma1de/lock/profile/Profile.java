package me.ma1de.lock.profile;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class Profile {
    @SerializedName("uuid")
    private UUID uuid;
    private String totpSecret;
}