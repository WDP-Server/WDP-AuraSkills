package dev.aurelium.auraskills.common.skillcoins;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transient cache for token/coin rewards given during SkillLevelUp events.
 * Stored so the LevelUpMessenger can include the rewards in the default message.
 */
public class TokenCoinRewardCache {

    private static final Map<UUID, TokenCoinReward> cache = new ConcurrentHashMap<>();

    public static void put(UUID uuid, String skillId, int level, int coins, int tokens) {
        cache.put(uuid, new TokenCoinReward(skillId, level, coins, tokens, System.currentTimeMillis()));
    }

    /**
     * Get and remove the reward if it matches the skill id and level and is recent (5s)
     */
    public static TokenCoinReward getAndRemoveIfMatch(UUID uuid, String skillId, int level) {
        TokenCoinReward r = cache.get(uuid);
        if (r == null) return null;
        if (!r.skillId.equals(skillId)) return null;
        if (r.level != level) return null;
        // Expire after 5 seconds
        if (System.currentTimeMillis() - r.timestamp > 5000) {
            cache.remove(uuid);
            return null;
        }
        // Remove and return
        cache.remove(uuid);
        return r;
    }

    public static class TokenCoinReward {
        public final String skillId;
        public final int level;
        public final int coins;
        public final int tokens;
        public final long timestamp;

        public TokenCoinReward(String skillId, int level, int coins, int tokens, long timestamp) {
            this.skillId = skillId;
            this.level = level;
            this.coins = coins;
            this.tokens = tokens;
            this.timestamp = timestamp;
        }
    }
}
