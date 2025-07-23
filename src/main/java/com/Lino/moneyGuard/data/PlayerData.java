package com.Lino.moneyGuard.data;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final List<Transaction> transactions;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.transactions = new ArrayList<>();
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

    public void clearTransactions() {
        transactions.clear();
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
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