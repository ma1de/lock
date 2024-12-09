package me.ma1de.lock.util;

import lombok.experimental.UtilityClass;
import me.ma1de.lock.Lock;
import org.bukkit.Bukkit;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Future;

/**
 * Original code can be found <a href="https://howtodoinjava.com/java/java-security/aes-256-encryption-decryption/">here</a>
 * <br><br>
 * New features compared to the original code: <br>
 * - Multithreaded (using Future API)<br>
 * - Somewhat clean code
 **/
@UtilityClass
public class CryptoUtil {
    public String encrypt(String strToEncrypt) {
        String passphrase = Lock.getInstance().getConfig().getString("ENCRYPTION.PASSPHRASE");
        String salt = Lock.getInstance().getConfig().getString("ENCRYPTION.SALT");

        if (passphrase == null || salt == null) {
            Lock.getInstance().getLogger().severe("Passphrase or salt is null");
            Bukkit.getPluginManager().disablePlugin(Lock.getInstance());
            return null;
        }

        Future<String> future = Lock.getInstance().getService().submit(() -> {
            try {
                byte[] iv = new byte[16];
                SecureRandom.getInstanceStrong().nextBytes(iv);

                SecretKeySpec keySpec = new SecretKeySpec(
                        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(
                                new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), 65536, 256)
                        ).getEncoded(),
                        "AES"
                );

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

                byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes(Charset.defaultCharset()));
                byte[] encryptedData = new byte[iv.length + cipherText.length];
                System.arraycopy(iv, 0, encryptedData, 0, iv.length);
                System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

                return Base64.getEncoder().encodeToString(encryptedData);
            } catch (Exception ex) {
                Lock.getInstance().getLogger().severe("Unable to encrypt data: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
                return null;
            }
        });

        boolean temp = false;

        while (!future.isDone()) {
            temp = !temp;
        }

        try {
            return future.get();
        } catch (Exception ex) {
            return "";
        }
    }

    public String decrypt(String strToDecrypt) {
        String passphrase = Lock.getInstance().getConfig().getString("ENCRYPTION.PASSPHRASE");
        String salt = Lock.getInstance().getConfig().getString("ENCRYPTION.SALT");

        if (passphrase == null || salt == null) {
            Lock.getInstance().getLogger().severe("Passphrase or salt is null");
            Bukkit.getPluginManager().disablePlugin(Lock.getInstance());
            return null;
        }

        Future<String> future = Lock.getInstance().getService().submit(() -> {
            try {
                byte[] encryptedData = Base64.getDecoder().decode(strToDecrypt);
                byte[] iv = new byte[16];
                System.arraycopy(encryptedData, 0, iv, 0, iv.length);

                SecretKeySpec keySpec = new SecretKeySpec(
                        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(
                                new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), 65536, 256)
                        ).getEncoded(),
                        "AES"
                );

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

                byte[] cipherText = new byte[encryptedData.length - 16];
                System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

                return new String(cipher.doFinal(cipherText), Charset.defaultCharset());
            } catch (Exception ex) {
                Lock.getInstance().getLogger().severe("Unable to decrypt data: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
                return null;
            }
        });

        boolean temp = false;

        while (!future.isDone()) {
            temp = !temp;
        }

        try {
            return future.get();
        } catch (Exception ex) {
            return "";
        }
    }
}
