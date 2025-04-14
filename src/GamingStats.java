import java.util.*;
import java.time.Instant;

public class GamingStats {
    private static class GameSession {
        private final String gameName;
        private final Instant timestamp;
        private int kills;
        private int deaths;
        private int assists;

        private GameSession(String gameName, Instant timestamp) {
            this.gameName = Objects.requireNonNull(gameName, "Game name cannot be null");
            this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
            this.kills = 0;
            this.deaths = 0;
            this.assists = 0;
        }

        public void updateStats(int kills, int deaths, int assists) {
            this.kills += kills;
            this.deaths += deaths;
            this.assists += assists;
        }

        @Override
        public String toString() {
            return String.format("Game: %s, Time: %s, KDA: %d/%d/%d",
                    gameName, timestamp, kills, deaths, assists);
        }
    }

    // Builder for GameSession
    public static class GameSessionBuilder {
        private String gameName;
        private Instant timestamp;

        public GameSessionBuilder gameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public GameSessionBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public GameSession build() {
            return new GameSession(gameName, timestamp);
        }
    }

    private final Map<String, List<GameSession>> gameSessions;

    // Private constructor to enforce use of static factory method
    private GamingStats() {
        gameSessions = new HashMap<>();
    }

    // Static factory method
    public static GamingStats create() {
        return new GamingStats();
    }

    public void addSession(GameSession session) {
        gameSessions.computeIfAbsent(session.gameName, k -> new ArrayList<>())
                .add(session);
    }

    public void updateSession(String gameName, Instant timestamp, int kills, int deaths, int assists) {
        List<GameSession> sessions = gameSessions.get(gameName);
        if (sessions != null) {
            for (GameSession session : sessions) {
                if (session.timestamp.equals(timestamp)) {
                    session.updateStats(kills, deaths, assists);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Session not found");
    }

    public List<GameSession> getGameSessions(String gameName) {
        return Collections.unmodifiableList(
                gameSessions.getOrDefault(gameName, Collections.emptyList()));
    }

    public static void main(String[] args) {
        GamingStats stats = GamingStats.create();

        // Create sessions using builder
        GameSession session1 = new GameSessionBuilder()
                .gameName("Call of Duty")
                .timestamp(Instant.now())
                .build();

        GameSession session2 = new GameSessionBuilder()
                .gameName("Call of Duty")
                .timestamp(Instant.now().plusSeconds(3600)) // 1 hour later
                .build();

        stats.addSession(session1);
        stats.updateSession("Call of Duty", session1.timestamp, 15, 2, 8); // Great game!

        stats.addSession(session2);
        stats.updateSession("Call of Duty", session2.timestamp, 2, 12, 3); // Terrible game!

        // Print all sessions for Call of Duty
        System.out.println("Call of Duty Sessions:");
        for (GameSession session : stats.getGameSessions("Call of Duty")) {
            System.out.println(session);
        }
    }
}