package tig.grpc.server.session;

import java.security.Key;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class SessionAuthenticator {

    public static final ConcurrentHashMap<String, CustomUserToken> sessions = new ConcurrentHashMap<>();

    public static String insertCustomSession(String sessionId, Key sessionKey, PublicKey publicKey) {
        //Session Id valid for 5 minutes
        sessions.put(sessionId, new CustomUserToken(LocalDateTime.now().plusMinutes(5), sessionKey, publicKey));
        return sessionId;
    }


    public static CustomUserToken authenticateSession(String signerId) {

        if (!sessions.containsKey(signerId)) {
            throw new IllegalArgumentException("Invalid SessionId");
        }

        CustomUserToken token = sessions.get(signerId);

        if (!token.authenticateToken()) {
            sessions.remove(signerId);
            throw new IllegalArgumentException("Session has expired");
        }

        //Update expiration if user authenticates successfully
        token.setExpiration(LocalDateTime.now().plusMinutes(5));

        return token;
    }

    public static void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

}
