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

    public EconomyManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
        this.recentTransactions = new ConcurrentHashMap<>();
        this.lastBalances = new ConcurrentHashMap<>();
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public void recordTransaction(Player player, double amount, Transaction.Type type) {
        UUID uuid = player.getUniqueId();
        List<Transaction> transactions = recentTransactions.computeIfAbsent(uuid, k -> new ArrayList<>());

        Transaction transaction = new Transaction(uuid, amount, type, System.currentTimeMillis());
        transactions.add(transaction);

        cleanOldTransactions(uuid);

        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        data.addTransaction(transaction);

        if (amount > plugin.getConfigManager().getSuspiciousTransactionAmount()) {
            plugin.getAlertManager().alertSuspiciousTransaction(player, amount, type);
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
            } else if (difference < 0) {
                recordTransaction(player, Math.abs(difference), Transaction.Type.LOSS);
            }
        }

        lastBalances.put(uuid, currentBalance);
    }

    public double getMoneyGainedInLastHour(UUID uuid) {
        List<Transaction> transactions = recentTransactions.get(uuid);
        if (transactions == null) return 0.0;

        long hourAgo = System.currentTimeMillis() - 3600000;
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.GAIN && t.getTimestamp() > hourAgo)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getMoneyGainedToday(UUID uuid) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        return data.getMoneyGainedToday();
    }

    private void cleanOldTransactions(UUID uuid) {
        List<Transaction> transactions = recentTransactions.get(uuid);
        if (transactions == null) return;

        long dayAgo = System.currentTimeMillis() - 86400000;
        transactions.removeIf(t -> t.getTimestamp() < dayAgo);
    }

    public void clearPlayerCache(UUID uuid) {
        recentTransactions.remove(uuid);
        lastBalances.remove(uuid);
    }

    public Map<UUID, List<Transaction>> getAllRecentTransactions() {
        return new HashMap<>(recentTransactions);
    }
}