package org.bigear.api.bigearbackend.googleAuth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GoogleTokenVerifier {

    @Value("${google.oauth.client-id}")
    private String googleWebClientId;

    // Add Android client ID - it should be the same as your web client ID
    // but you might have a separate Android client ID
    @Value("${google.oauth.android-client-id:${google.oauth.client-id}}")
    private String googleAndroidClientId;

    public GoogleIdToken.Payload verifyToken(String idToken) throws Exception {
        System.out.println("🔍 Verifying Google ID token...");
        System.out.println("📋 Accepted audiences:");
        System.out.println("   - Web Client ID: " + googleWebClientId);
        System.out.println("   - Android Client ID: " + googleAndroidClientId);

        // Accept tokens from both web and Android clients
        List<String> audiences = Arrays.asList(googleWebClientId, googleAndroidClientId);

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(audiences)
                .build();

        GoogleIdToken googleIdToken = verifier.verify(idToken);
        if (googleIdToken != null) {
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            System.out.println("✅ Token verified successfully");
            System.out.println("📧 Email: " + payload.getEmail());
            System.out.println("👤 Name: " + payload.get("name"));
            System.out.println("🎯 Audience: " + payload.getAudience());
            return payload;
        }

        System.out.println("❌ Token verification failed");
        throw new RuntimeException("Invalid Google ID token");
    }
}