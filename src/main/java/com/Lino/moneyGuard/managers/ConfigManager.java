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
    private boolean logToFile;
    private boolean autoRemoveMoney;

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

        logToFile = config.getBoolean("logging.log-to-file", true);
        autoRemoveMoney = config.getBoolean("actions.auto-remove-money", true);

        config.options().copyDefaults(true);
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

    public boolean isLogToFile() {
        return logToFile;
    }

    public boolean isAutoRemoveMoney() {
        return autoRemoveMoney;
    }
}