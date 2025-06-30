package com.res.server.removebgbackend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ClerkJwksProvider {

    /**
     * The URL to fetch the JSON Web Key Set (JWKS) for Clerk authentication.
     * This URL is used to validate JWT tokens issued by Clerk.
     */
    @Value("${clerk.jwks.url}")
    String jwksUrl;
    private Map<String, PublicKey> keyCache = new HashMap<>();
    private long lastFetchTime=0;
    private static final long CACHE_EXPIRY_TIME = 3600000; // 1 hour in milliseconds Time to live

    public PublicKey getPublicKey(String kId) throws Exception {
        if (keyCache.containsKey(kId) && (System.currentTimeMillis() - lastFetchTime < CACHE_EXPIRY_TIME)) {
            return keyCache.get(kId);

        }
        refreshKeys();
        return keyCache.get(kId);

    }

    // Logic to fetch the  JWKS from the jwksUrl and populate keyCache
    // This method should be called periodically or when a key is not found in the cache
    private void refreshKeys() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jwks = mapper.readTree(new URL(jwksUrl));

        JsonNode keys = jwks.get("keys");
        for (JsonNode keyNode : keys) {
            String kid = keyNode.get("kid").asText();  // Key ID
            String kty = keyNode.get("kty").asText();  // Key type, e.g., RSA
            String alg = keyNode.get("alg").asText();  // Algorithm, e.g., RS256

            if ("RSA".equals(kty) && "RS256".equals(alg)) {
                String n = keyNode.get("n").asText(); // Modulus
                String e = keyNode.get("e").asText(); // Exponent

                PublicKey publicKey = createPublicKey(n, e); // Build public key
                keyCache.put(kid, publicKey); // Cache it
            }
        }
        lastFetchTime = System.currentTimeMillis();
    }
    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {
        byte[] modBytes = Base64.getUrlDecoder().decode(modulus);
        byte[] expBytes = Base64.getUrlDecoder().decode(exponent);

        BigInteger mod = new BigInteger(1, modBytes);
        BigInteger exp = new BigInteger(1, expBytes);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }

}

