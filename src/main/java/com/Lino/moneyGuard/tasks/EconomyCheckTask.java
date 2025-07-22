package com.Lino.moneyGuard.tasks;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EconomyCheckTask extends BukkitRunnable {

    private final MoneyGuard plugin;
    private int tickCounter = 0;
    private final Queue<Player> playerQueue;

    public EconomyCheckTask(MoneyGuard plugin) {
        this.plugin = plugin;
        this.playerQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (playerQueue.isEmpty()) {
            playerQueue.addAll(onlinePlayers);
        }

        int playersToScan = Math.min(plugin.getConfigManager().getPlayersPerScan(), playerQueue.size());

        for (int i = 0; i < playersToScan; i++) {
            Player player = playerQueue.poll();
            if (player == null || !player.isOnline()) continue;

            if (!player.hasPermission("moneyguard.bypass")) {
                plugin.getEconomyManager().checkBalanceChange(player);

                double gainedMinute = plugin.getEconomyManager().getMoneyGainedInLastMinute(player.getUniqueId());
                double limitMinute = plugin.getConfigManager().getMaxMoneyPerMinute();

                if (gainedMinute > limitMinute) {
                    plugin.getAlertManager().alertMaxMoneyPerMinute(player, gainedMinute, limitMinute);
                }
            }
        }

        tickCounter++;
        if (tickCounter >= 300) {
            sendSuspiciousReport();
            plugin.getEconomyManager().clearAllCaches();
            plugin.getDataManager().clearAll5MinuteData();
            plugin.getAlertManager().clearAlertCooldowns();
            tickCounter = 0;
        }
    }

    private void sendSuspiciousReport() {
        Map<UUID, List<Transaction>> suspicious = plugin.getEconomyManager().getSuspiciousTransactionsLast5Minutes();

        if (suspicious.isEmpty()) {
            return;
        }

        String header = plugin.getMessageManager().getMessage("reports.header");
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("moneyguard.alerts")) {
                admin.sendMessage(header);

                for (Map.Entry<UUID, List<Transaction>> entry : suspicious.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        double total = entry.getValue().stream().mapToDouble(Transaction::getAmount).sum();
                        admin.sendMessage(plugin.getMessageManager().getMessage("reports.player-entry",
                                "{player}", player.getName(),
                                "{count}", String.valueOf(entry.getValue().size()),
                                "{total}", String.format("%.2f", total)));
                    }
                }

                admin.sendMessage(plugin.getMessageManager().getMessage("reports.footer"));
            }
        }
    }
}