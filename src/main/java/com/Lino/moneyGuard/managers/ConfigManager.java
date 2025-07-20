package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MoneyGuard plugin;
    private FileConfiguration config;

    private double maxMoneyPerHour;
    private double maxMoneyPerDay;
    private double maxTotalMoney;
    private double suspiciousTransactionAmount;
    private int checkInterval;
    private boolean autoWarnEnabled;
    private boolean autoBanEnabled;
    private int warningsBeforeBan;
    private int banDuration;
    private boolean logToFile;
    private boolean resetDailyStats;
    private int resetHour;

    public ConfigManager(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        maxMoneyPerHour = config.getDouble("limits.max-money-per-hour", 10000.0);
        maxMoneyPerDay = config.getDouble("limits.max-money-per-day", 100000.0);
        maxTotalMoney = config.getDouble("limits.max-total-money", 1000000.0);
        suspiciousTransactionAmount = config.getDouble("limits.suspicious-transaction", 50000.0);

        checkInterval = config.getInt("monitoring.check-interval-seconds", 60);
        autoWarnEnabled = config.getBoolean("monitoring.auto-warn", true);
        autoBanEnabled = config.getBoolean("monitoring.auto-ban", false);
        warningsBeforeBan = config.getInt("monitoring.warnings-before-ban", 3);
        banDuration = config.getInt("monitoring.ban-duration-minutes", 1440);

        logToFile = config.getBoolean("logging.log-to-file", true);
        resetDailyStats = config.getBoolean("reset.daily-stats", true);
        resetHour = config.getInt("reset.hour", 0);

        createDefaults();
    }

    private void createDefaults() {
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public double getMaxMoneyPerHour() {
        return maxMoneyPerHour;
    }

    public double getMaxMoneyPerDay() {
        return maxMoneyPerDay;
    }

    public double getMaxTotalMoney() {
        return maxTotalMoney;
    }

    public double getSuspiciousTransactionAmount() {
        return suspiciousTransactionAmount;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public boolean isAutoWarnEnabled() {
        return autoWarnEnabled;
    }

    public boolean isAutoBanEnabled() {
        return autoBanEnabled;
    }

    public int getWarningsBeforeBan() {
        return warningsBeforeBan;
    }

    public int getBanDuration() {
        return banDuration;
    }

    public boolean isLogToFile() {
        return logToFile;
    }

    public boolean isResetDailyStats() {
        return resetDailyStats;
    }

    public int getResetHour() {
        return resetHour;
    }
}