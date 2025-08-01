package net.cloud.security;

import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Repository
public class JwtBlackList {
    private final Map<String, String> blacklist = new ConcurrentHashMap<>();

    public void add(String username, String token) {
        blacklist.put(username, token);
    }

    public void remove(String username) {
        blacklist.remove(username);
    }

    public boolean containsUsernameAndToken(String username, String token) {
        return blacklist.containsKey(username) && blacklist.get(username).equals(token);
    }

}
