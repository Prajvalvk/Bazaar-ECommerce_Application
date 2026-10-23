package com.ecommerce.config;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/** Salted PBKDF2 password hashing using only the JDK (no extra dependency). */
@Component
public class PasswordHasher {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;

    public String encode(String raw) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash(raw, salt));
    }

    public boolean matches(String raw, String stored) {
        try {
            String[] p = stored.split(":");
            byte[] salt = Base64.getDecoder().decode(p[0]);
            byte[] expected = Base64.getDecoder().decode(p[1]);
            return java.security.MessageDigest.isEqual(expected, hash(raw, salt));
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] hash(String raw, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, ITERATIONS, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }
}
