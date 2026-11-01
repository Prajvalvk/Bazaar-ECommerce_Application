package com.ecommerce.service;

import com.ecommerce.entity.Cart;
import com.ecommerce.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentService {
    private final CartService carts;
    private final ObjectMapper mapper;
    private final String secretKey;
    private final String publishableKey;

    public StripePaymentService(CartService carts, ObjectMapper mapper,
                                @Value("${STRIPE_SECRET_KEY:}") String secretKey,
                                @Value("${VITE_STRIPE_PUBLISHABLE_KEY:${STRIPE_PUBLISHABLE_KEY:}}") String publishableKey) {
        this.carts = carts;
        this.mapper = mapper;
        this.secretKey = secretKey;
        this.publishableKey = publishableKey;
    }

    public Map<String, String> createGooglePayIntent(Long userId) {
        requireConfigured();
        Cart cart = carts.mine(userId);
        if (cart.getItems().isEmpty()) throw ApiException.badRequest("Your cart is empty");
        long amountMinor = Math.round(cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum() * 100);
        String body = "amount=" + amountMinor + "&currency=inr&automatic_payment_methods[enabled]=true";
        JsonNode intent = stripePost("/v1/payment_intents", body);
        if (intent.path("error").isObject()) throw ApiException.badRequest(intent.path("error").path("message").asText("Stripe payment setup failed"));
        return Map.of(
                "publishableKey", publishableKey,
                "clientSecret", intent.path("client_secret").asText(),
                "amount", Long.toString(amountMinor),
                "paymentIntentId", intent.path("id").asText());
    }

    public void verifyGooglePayIntent(String intentId, double expectedAmount) {
        requireConfigured();
        if (intentId == null || intentId.isBlank()) throw ApiException.badRequest("Stripe PaymentIntent ID is required for Google Pay");
        JsonNode intent = stripeGet("/v1/payment_intents/" + encode(intentId));
        if (!"succeeded".equals(intent.path("status").asText())) {
            throw ApiException.badRequest("Google Pay payment has not completed");
        }
        if (!"inr".equalsIgnoreCase(intent.path("currency").asText())) throw ApiException.badRequest("Payment currency is invalid");
        long paidMinor = intent.path("amount_received").asLong(intent.path("amount").asLong(0));
        long expectedMinor = Math.round(expectedAmount * 100);
        if (paidMinor != expectedMinor) throw ApiException.badRequest("Payment amount does not match the order total");
    }

    private void requireConfigured() {
        if (secretKey == null || secretKey.isBlank() || publishableKey == null || publishableKey.isBlank()) {
            throw ApiException.badRequest("Stripe Google Pay is not configured on the server");
        }
    }

    private JsonNode stripeGet(String path) {
        return stripe(path, "GET", null);
    }

    private JsonNode stripePost(String path, String body) {
        return stripe(path, "POST", body);
    }

    private JsonNode stripe(String path, String method, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com" + path))
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/x-www-form-urlencoded");
            if ("POST".equals(method)) builder.POST(HttpRequest.BodyPublishers.ofString(body));
            else builder.GET();
            HttpResponse<String> response = HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(response.body());
            if (response.statusCode() >= 400) {
                throw ApiException.badRequest(json.path("error").path("message").asText("Stripe request failed"));
            }
            return json;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw ApiException.badRequest("Cannot reach Stripe: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
