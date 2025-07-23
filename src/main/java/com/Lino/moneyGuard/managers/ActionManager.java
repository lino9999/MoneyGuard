package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ActionManager {

    private final MoneyGuard plugin;

    public ActionManager(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    public void removeSuspiciousMoney(Player player, double amount) {
        if (!plugin.getConfigManager().isAutoRemoveMoney()) {
            return;
        }

        double currentBalance = plugin.getEconomy().getBalance(player);
        double newBalance = Math.max(0, currentBalance - amount);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                String.format("eco set %s %.2f", player.getName(), newBalance));

        String message = plugin.getMessageManager().getMessage("actions.money-removed",
                "{player}", player.getName(),
                "{amount}", String.format("%.2f", amount));

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("moneyguard.alerts")) {
                admin.sendMessage(message);
                admin.playSound(admin.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }

        Bukkit.getConsoleSender().sendMessage(message);

        plugin.getLogManager().logMoneyRemoval(player, amount);
        plugin.getStatsManager().addMoneyRemoved(amount);
    }
}