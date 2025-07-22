package com.Lino.moneyGuard.data;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final List<Transaction> transactions;
    private boolean isBanned;
    private long banExpiry;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.transactions = new ArrayList<>();
        this.isBanned = false;
        this.banExpiry = 0;
    }

    public void addTransaction(Transaction transaction) {
        if (transaction.getType() != Transaction.Type.GAIN) {
            return;
        }

        transactions.add(transaction);

        if (transactions.size() > 100) {
            transactions.remove(0);
        }
    }

    public void ban(long duration) {
        isBanned = true;
        banExpiry = System.currentTimeMillis() + duration;
    }

    public void unban() {
        isBanned = false;
        banExpiry = 0;
    }

    public boolean checkBanExpiry() {
        if (isBanned && System.currentTimeMillis() > banExpiry) {
            unban();
            return true;
        }
        return false;
    }

    public void clearTransactions() {
        transactions.clear();
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public boolean isBanned() {
        return isBanned;
    }

    public long getBanExpiry() {
        return banExpiry;
    }

    public double getMoneyGainedLast5Minutes() {
        long fiveMinutesAgo = System.currentTimeMillis() - 300000;
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.GAIN && t.getTimestamp() > fiveMinutesAgo)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getMoneyGainedLastMinute() {
        long oneMinuteAgo = System.currentTimeMillis() - 60000;
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.GAIN && t.getTimestamp() > oneMinuteAgo)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}