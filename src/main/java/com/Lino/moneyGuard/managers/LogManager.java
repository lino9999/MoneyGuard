package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.Transaction;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private final MoneyGuard plugin;
    private final SimpleDateFormat dateFormat;
    private File logFile;

    public LogManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.logFile = new File(plugin.getDataFolder(), "moneyguard.log");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create log file!");
            }
        }
    }

    public void logTransaction(Player player, double amount, Transaction.Type type, String flag) {
        if (!plugin.getConfigManager().isLogToFile()) return;

        String entry = String.format("[%s] TRANSACTION | Player: %s | Amount: %.2f | Type: %s | Flag: %s",
                dateFormat.format(new Date()),
                player.getName(),
                amount,
                type.toString(),
                flag);

        writeToLog(entry);
    }

    public void logWarning(Player player, String reason, int totalWarnings) {
        if (!plugin.getConfigManager().isLogToFile()) return;

        String entry = String.format("[%s] WARNING | Player: %s | Reason: %s | Total Warnings: %d",
                dateFormat.format(new Date()),
                player.getName(),
                reason,
                totalWarnings);

        writeToLog(entry);
    }

    public void logBan(Player player, String reason, long duration) {
        if (!plugin.getConfigManager().isLogToFile()) return;

        String entry = String.format("[%s] BAN | Player: %s | UUID: %s | Reason: %s | Duration: %d minutes",
                dateFormat.format(new Date()),
                player.getName(),
                player.getUniqueId().toString(),
                reason,
                duration / 60000);

        writeToLog(entry);
    }

    public void logAlert(String type, String message) {
        if (!plugin.getConfigManager().isLogToFile()) return;

        String entry = String.format("[%s] ALERT | Type: %s | %s",
                dateFormat.format(new Date()),
                type,
                message);

        writeToLog(entry);
    }

    private void writeToLog(String entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(entry);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write to log file: " + e.getMessage());
        }
    }
}