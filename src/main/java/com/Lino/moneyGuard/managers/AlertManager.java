package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertManager {

    private final MoneyGuard plugin;
    private final Set<UUID> recentAlerts;

    public AlertManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.recentAlerts = new HashSet<>();
    }

    public void alertSuspiciousTransaction(Player player, double amount, Transaction.Type type) {
        String message = plugin.getMessageManager().getMessage("alerts.suspicious-transaction",
                "{player}", player.getName(),
                "{amount}", String.format("%.2f", amount),
                "{type}", type.toString());

        notifyAdmins(message, Sound.BLOCK_BELL_RESONATE);
        plugin.getLogManager().logTransaction(player, amount, type, "SUSPICIOUS");
    }

    public void alertMaxMoneyPerHour(Player player, double gained, double limit) {
        if (hasRecentAlert(player.getUniqueId())) return;

        String message = plugin.getMessageManager().getMessage("alerts.max-money-hour",
                "{player}", player.getName(),
                "{gained}", String.format("%.2f", gained),
                "{limit}", String.format("%.2f", limit));

        notifyAdmins(message, Sound.ENTITY_VILLAGER_NO);
        addRecentAlert(player.getUniqueId());
    }

    public void alertMaxMoneyPerDay(Player player, double gained, double limit) {
        String message = plugin.getMessageManager().getMessage("alerts.max-money-day",
                "{player}", player.getName(),
                "{gained}", String.format("%.2f", gained),
                "{limit}", String.format("%.2f", limit));

        notifyAdmins(message, Sound.ENTITY_VILLAGER_NO);
    }

    public void alertMaxTotalMoney(Player player, double balance, double limit) {
        String message = plugin.getMessageManager().getMessage("alerts.max-total-money",
                "{player}", player.getName(),
                "{balance}", String.format("%.2f", balance),
                "{limit}", String.format("%.2f", limit));

        notifyAdmins(message, Sound.ENTITY_ENDER_DRAGON_GROWL);
    }

    public void alertPlayerWarned(Player player, int warnings, int maxWarnings) {
        String message = plugin.getMessageManager().getMessage("alerts.player-warned",
                "{player}", player.getName(),
                "{warnings}", String.valueOf(warnings),
                "{max}", String.valueOf(maxWarnings));

        notifyAdmins(message, Sound.BLOCK_NOTE_BLOCK_BASS);
    }

    public void alertPlayerBanned(Player player, String reason) {
        String message = plugin.getMessageManager().getMessage("alerts.player-banned",
                "{player}", player.getName(),
                "{reason}", reason);

        notifyAdmins(message, Sound.ENTITY_WITHER_SPAWN);
    }

    private void notifyAdmins(String message, Sound sound) {
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("moneyguard.alerts")) {
                admin.sendMessage(message);
                admin.playSound(admin.getLocation(), sound, 1.0f, 1.0f);
            }
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }

    private boolean hasRecentAlert(UUID uuid) {
        return recentAlerts.contains(uuid);
    }

    private void addRecentAlert(UUID uuid) {
        recentAlerts.add(uuid);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            recentAlerts.remove(uuid);
        }, 1200L);
    }
}