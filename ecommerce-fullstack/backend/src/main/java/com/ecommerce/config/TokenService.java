package com.ecommerce.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Stateless signed login tokens (HMAC-SHA256), no extra dependencies. Format: base64(userId.expiry).base64(signature) */
@Component
public class TokenService {
    private static final long TTL_SECONDS = 7L * 24 * 3600;
    private final byte[] secret;

    public TokenService(@Value("${AUTH_SECRET:}") String configured) {
        if (configured == null || configured.isBlank()) {
            byte[] b = new byte[32];
            new SecureRandom().nextBytes(b);
            this.secret = b; // random per start: sessions end on restart. Set AUTH_SECRET to keep them.
        } else {
            this.secret = configured.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String issue(Long userId) {
        String payload = userId + "." + (Instant.now().getEpochSecond() + TTL_SECONDS);
        return enc(payload.getBytes(StandardCharsets.UTF_8)) + "." + enc(hmac(payload));
    }

    /** Returns the user id, or null when the token is invalid or expired. */
    public Long verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(Base64.getUrlDecoder().decode(parts[1]), hmac(payload))) return null;
            String[] p = payload.split("\\.");
            if (Long.parseLong(p[1]) < Instant.now().getEpochSecond()) return null;
            return Long.parseLong(p[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String enc(byte[] b) { return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
}
