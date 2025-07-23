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
    private final Set<UUID> alertedPlayers;

    public AlertManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.alertedPlayers = new HashSet<>();
    }

    public void alertSuspiciousTransaction(Player player, double amount, Transaction.Type type) {
        if (alertedPlayers.contains(player.getUniqueId())) {
            return;
        }

        String message = plugin.getMessageManager().getMessage("alerts.suspicious-transaction",
                "{player}", player.getName(),
                "{amount}", String.format("%.2f", amount),
                "{type}", type.toString());

        notifyAdmins(message, Sound.BLOCK_BELL_RESONATE);
        plugin.getLogManager().logTransaction(player, amount, type, "SUSPICIOUS");
        plugin.getStatsManager().incrementSuspiciousTransactions();
        alertedPlayers.add(player.getUniqueId());
    }

    public void alertMaxMoneyPerMinute(Player player, double gained, double limit) {
        if (alertedPlayers.contains(player.getUniqueId())) {
            return;
        }

        String message = plugin.getMessageManager().getMessage("alerts.max-money-minute",
                "{player}", player.getName(),
                "{gained}", String.format("%.2f", gained),
                "{limit}", String.format("%.2f", limit));

        notifyAdmins(message, Sound.ENTITY_VILLAGER_NO);
        plugin.getLogManager().logTransaction(player, gained, Transaction.Type.GAIN, "EXCEEDED_MINUTE_LIMIT");

        plugin.getActionManager().removeSuspiciousMoney(player, gained);

        alertedPlayers.add(player.getUniqueId());
    }

    public void clearAlertCooldowns() {
        alertedPlayers.clear();
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
}