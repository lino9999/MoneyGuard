package com.Lino.moneyGuard.tasks;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;

public class EconomyCheckTask extends BukkitRunnable {

    private final MoneyGuard plugin;
    private int lastResetHour = -1;

    public EconomyCheckTask(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        checkDailyReset();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("moneyguard.bypass")) continue;

            plugin.getEconomyManager().checkBalanceChange(player);
            plugin.getActionManager().checkViolations(player);
        }
    }

    private void checkDailyReset() {
        if (!plugin.getConfigManager().isResetDailyStats()) return;

        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);

        if (currentHour == plugin.getConfigManager().getResetHour() && currentHour != lastResetHour) {
            plugin.getDataManager().resetAllDailyStats();
            plugin.getDataManager().saveAllData();
            lastResetHour = currentHour;

            plugin.getLogger().info("Daily statistics have been reset");
        }
    }
}