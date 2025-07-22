package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;
import com.Lino.moneyGuard.data.Transaction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {

    private final MoneyGuard plugin;
    private final Economy economy;
    private final Map<UUID, List<Transaction>> recentTransactions;
    private final Map<UUID, Double> lastBalances;
    private final Set<UUID> alertedSuspiciousTransactions;

    public EconomyManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.recentTransactions = new ConcurrentHashMap<>();
        this.lastBalances = new ConcurrentHashMap<>();
        this.alertedSuspiciousTransactions = new HashSet<>();
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public void recordTransaction(Player player, double amount, Transaction.Type type) {
        if (type != Transaction.Type.GAIN) {
            return;
        }

        UUID uuid = player.getUniqueId();
        List<Transaction> transactions = recentTransactions.computeIfAbsent(uuid, k -> new ArrayList<>());

        Transaction transaction = new Transaction(uuid, amount, type, System.currentTimeMillis());
        transactions.add(transaction);

        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        data.addTransaction(transaction);

        if (amount > plugin.getConfigManager().getSuspiciousTransactionAmount() &&
                !alertedSuspiciousTransactions.contains(uuid)) {
            plugin.getAlertManager().alertSuspiciousTransaction(player, amount, type);
            alertedSuspiciousTransactions.add(uuid);
        }
    }

    public void checkBalanceChange(Player player) {
        UUID uuid = player.getUniqueId();
        double currentBalance = getBalance(player);
        Double lastBalance = lastBalances.get(uuid);

        if (lastBalance != null) {
            double difference = currentBalance - lastBalance;

            if (difference > 0) {
                recordTransaction(player, difference, Transaction.Type.GAIN);
            }
        }

        lastBalances.put(uuid, currentBalance);
    }

    public double getMoneyGainedInLastMinute(UUID uuid) {
        List<Transaction> transactions = recentTransactions.get(uuid);
        if (transactions == null) return 0.0;

        long oneMinuteAgo = System.currentTimeMillis() - 60000;
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.GAIN && t.getTimestamp() > oneMinuteAgo)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getMoneyGainedInLast5Minutes(UUID uuid) {
        List<Transaction> transactions = recentTransactions.get(uuid);
        if (transactions == null) return 0.0;

        long fiveMinutesAgo = System.currentTimeMillis() - 300000;
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.GAIN && t.getTimestamp() > fiveMinutesAgo)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<UUID, List<Transaction>> getSuspiciousTransactionsLast5Minutes() {
        Map<UUID, List<Transaction>> suspicious = new HashMap<>();
        long fiveMinutesAgo = System.currentTimeMillis() - 300000;

        for (Map.Entry<UUID, List<Transaction>> entry : recentTransactions.entrySet()) {
            List<Transaction> playerSuspicious = new ArrayList<>();

            for (Transaction t : entry.getValue()) {
                if (t.getTimestamp() > fiveMinutesAgo &&
                        t.getAmount() > plugin.getConfigManager().getSuspiciousTransactionAmount()) {
                    playerSuspicious.add(t);
                }
            }

            if (!playerSuspicious.isEmpty()) {
                suspicious.put(entry.getKey(), playerSuspicious);
            }
        }

        return suspicious;
    }

    public void clearAllCaches() {
        recentTransactions.clear();
        lastBalances.clear();
        alertedSuspiciousTransactions.clear();
    }

    public void clearPlayerCache(UUID uuid) {
        recentTransactions.remove(uuid);
        lastBalances.remove(uuid);
        alertedSuspiciousTransactions.remove(uuid);
    }
}