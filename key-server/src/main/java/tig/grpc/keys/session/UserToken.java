package tig.grpc.keys.session;

import java.time.LocalDateTime;

public class UserToken {

    private LocalDateTime expiration;
    private String username;

    public UserToken(LocalDateTime expiration, String username) {
        this.expiration = expiration;
        this.username = username;
    }

    public boolean authenticateToken() {
      return !expiration.isBefore(LocalDateTime.now());
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public String getUsername() {
        return username;
    }
}
