import java.util.*;

public class GamingStats {
    private static class GameSession {
        private String gameName;
        private Date timestamp;
        private int kills;
        private int deaths;
        private int assists;

        public GameSession(String gameName, Date timestamp) {
            this.gameName = gameName;
            this.timestamp = timestamp;
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

    private Map<String, List<GameSession>> gameSessions;

    public GamingStats() {
        gameSessions = new HashMap<>();
    }

    public void addSession(String gameName, Date timestamp) {
        gameSessions.computeIfAbsent(gameName, k -> new ArrayList<>())
                .add(new GameSession(gameName, timestamp));
    }

    public void updateSession(String gameName, Date timestamp, int kills, int deaths, int assists) {
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
        return gameSessions.getOrDefault(gameName, new ArrayList<>());
    }

    public static void main(String[] args) {
        GamingStats stats = new GamingStats();

        // Example usage
        Date friday9pm = new Date(); // In real code, you'd set specific dates
        Date friday10pm = new Date();

        stats.addSession("Call of Duty", friday9pm);
        stats.updateSession("Call of Duty", friday9pm, 15, 2, 8); // Great game!

        stats.addSession("Call of Duty", friday10pm);
        stats.updateSession("Call of Duty", friday10pm, 2, 12, 3); // Terrible game!

        // Print all sessions for Call of Duty
        System.out.println("Call of Duty Sessions:");
        for (GameSession session : stats.getGameSessions("Call of Duty")) {
            System.out.println(session);
        }
    }
}