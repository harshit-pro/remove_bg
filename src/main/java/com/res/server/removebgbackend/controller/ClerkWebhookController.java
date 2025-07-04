package com.res.server.removebgbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.res.server.removebgbackend.dto.UserDto;
import com.res.server.removebgbackend.response.RemoveBgResponse;
import com.res.server.removebgbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/webhooks")
@RequiredArgsConstructor
public class ClerkWebhookController {

    private final UserService userService;

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    private byte[] decodedSecret;

    private static final long TOLERANCE_IN_SECONDS = 300; // 5 minutes

    @PostConstruct
    private void init() {
        this.decodedSecret = Base64.getDecoder().decode(webhookSecret.replaceFirst("^whsec_", ""));
    }

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(
            @RequestHeader("svix-id") String svixId,
            @RequestHeader("svix-timestamp") String svixTimestamp,
            @RequestHeader("svix-signature") String svixSignature,
            @RequestBody String payload) {

        log.info("Received Clerk webhook with svix-id: {}", svixId);

        try {
            if (!verifyWebhookSignature(svixId, svixTimestamp, svixSignature, payload)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        RemoveBgResponse.builder()
                                .statusCode(HttpStatus.UNAUTHORIZED)
                                .data("Invalid webhook signature")
                                .success(false)
                                .build()
                );
            }

            JsonNode rootNode = new ObjectMapper().readTree(payload);
            String eventType = rootNode.path("type").asText();
            JsonNode dataNode = rootNode.path("data");

            switch (eventType) {
                case "user.created":
                    handleUserCreated(dataNode);
                    break;
                case "user.updated":
                    handleUserUpdated(dataNode);
                    break;
                case "user.deleted":
                    handleUserDeleted(dataNode);
                    break;
                default:
                    log.warn("Unhandled Clerk event type: {}", eventType);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing Clerk webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    RemoveBgResponse.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                            .data("Webhook processing error: " + e.getMessage())
                            .success(false)
                            .build()
            );
        }
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        try {
            long now = Instant.now().getEpochSecond();
            long timestamp = Long.parseLong(svixTimestamp);

            if (Math.abs(now - timestamp) > TOLERANCE_IN_SECONDS) {
                log.warn("Webhook timestamp outside tolerance");
                return false;
            }

            String signedContent = svixId + "." + svixTimestamp + "." + payload;
            String expectedSignature = computeHMAC(signedContent);

            List<String> providedSignatures = List.of(svixSignature.split(" "));
            for (String sig : providedSignatures) {
                String cleanSig = sig.replaceFirst("^v1,", "");
                if (constantTimeEquals(cleanSig, expectedSignature)) {
                    return true;
                }
            }

            log.warn("No matching signature found in webhook header");
            return false;

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    private String computeHMAC(String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(decodedSecret, "HmacSHA256");
        hmac.init(keySpec);
        byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private void handleUserCreated(JsonNode data) {
        UserDto newUser = UserDto.builder()
                .clerkId(data.path("id").asText())
                .username(data.path("username").asText())
                .email(data.path("email_addresses").path(0).path("email_address").asText())
                .firstName(data.path("first_name").asText())
                .lastName(data.path("last_name").asText())
                .photoUrl(data.path("image_url").asText())
                .build();

        userService.saveUser(newUser);
        log.info("Created new user from webhook: {}", newUser.getEmail());
    }

    private void handleUserUpdated(JsonNode data) {
        String clerkId = data.path("id").asText();
        try {
            UserDto user = userService.getUserByClerkId(clerkId);
            user.setUsername(data.path("username").asText());
            user.setEmail(data.path("email_addresses").path(0).path("email_address").asText());
            user.setFirstName(data.path("first_name").asText());
            user.setLastName(data.path("last_name").asText());
            user.setPhotoUrl(data.path("image_url").asText());

            userService.saveUser(user);
            log.info("Updated user from webhook: {}", user.getEmail());
        } catch (UsernameNotFoundException e) {
            log.warn("User not found to update: {}", clerkId);
        }
    }

    @Transactional
    protected void handleUserDeleted(JsonNode data) {
        String clerkId = data.path("id").asText();
        try {
            userService.deleteUserByClerkId(clerkId);
            log.info("Deleted user with clerkId: {}", clerkId);
        } catch (UsernameNotFoundException e) {
            log.warn("User not found to delete: {}", clerkId);
        } catch (Exception e) {
            log.error("Failed to delete user: {}", clerkId, e);
        }
    }
}
