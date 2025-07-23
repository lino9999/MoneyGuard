package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;

import java.io.File;
import java.sql.*;

public class StatsManager {

    private final MoneyGuard plugin;
    private Connection connection;
    private final String dbPath;

    public StatsManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), "stats.db").getAbsolutePath();
        initDatabase();
    }

    private void initDatabase() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            createTables();

        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS stats (" +
                "id INTEGER PRIMARY KEY," +
                "stat_name TEXT UNIQUE NOT NULL," +
                "stat_value INTEGER NOT NULL DEFAULT 0" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);

            stmt.execute("INSERT OR IGNORE INTO stats (stat_name, stat_value) VALUES ('money_removed', 0)");
            stmt.execute("INSERT OR IGNORE INTO stats (stat_name, stat_value) VALUES ('suspicious_transactions', 0)");
            stmt.execute("INSERT OR IGNORE INTO stats (stat_name, stat_value) VALUES ('players_checked', 0)");
        }
    }

    private long getStat(String statName) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT stat_value FROM stats WHERE stat_name = ?")) {
            pstmt.setString(1, statName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("stat_value");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get stat " + statName + ": " + e.getMessage());
        }
        return 0;
    }

    private void updateStat(String statName, long value) {
        try (PreparedStatement pstmt = connection.prepareStatement("UPDATE stats SET stat_value = ? WHERE stat_name = ?")) {
            pstmt.setLong(1, value);
            pstmt.setString(2, statName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update stat " + statName + ": " + e.getMessage());
        }
    }

    public void addMoneyRemoved(double amount) {
        long current = getStat("money_removed");
        updateStat("money_removed", current + (long) amount);
    }

    public void incrementSuspiciousTransactions() {
        long current = getStat("suspicious_transactions");
        updateStat("suspicious_transactions", current + 1);
    }

    public void incrementPlayersChecked() {
        long current = getStat("players_checked");
        updateStat("players_checked", current + 1);
    }

    public long getTotalMoneyRemoved() {
        return getStat("money_removed");
    }

    public int getTotalSuspiciousTransactions() {
        return (int) getStat("suspicious_transactions");
    }

    public int getTotalPlayersChecked() {
        return (int) getStat("players_checked");
    }

    public void saveStats() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}