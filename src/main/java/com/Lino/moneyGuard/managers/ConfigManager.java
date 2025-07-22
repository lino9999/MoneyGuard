package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MoneyGuard plugin;
    private FileConfiguration config;

    private double maxMoneyPerMinute;
    private double suspiciousTransactionAmount;
    private int checkInterval;
    private int playersPerScan;
    private int banDuration;
    private boolean logToFile;

    public ConfigManager(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        maxMoneyPerMinute = config.getDouble("limits.max-money-per-minute", 10000.0);
        suspiciousTransactionAmount = config.getDouble("limits.suspicious-transaction", 50000.0);

        checkInterval = config.getInt("monitoring.check-interval-seconds", 1);
        playersPerScan = config.getInt("monitoring.players-per-scan", 3);
        banDuration = config.getInt("monitoring.ban-duration-minutes", 1440);

        logToFile = config.getBoolean("logging.log-to-file", true);

        createDefaults();
    }

    private void createDefaults() {
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public double getMaxMoneyPerMinute() {
        return maxMoneyPerMinute;
    }

    public double getSuspiciousTransactionAmount() {
        return suspiciousTransactionAmount;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public int getPlayersPerScan() {
        return playersPerScan;
    }

    public int getBanDuration() {
        return banDuration;
    }

    public boolean isLogToFile() {
        return logToFile;
    }
}