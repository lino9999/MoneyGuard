package com.Lino.moneyGuard.data;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final List<Transaction> transactions;
    private int warnings;
    private long lastWarning;
    private long lastReset;
    private double moneyGainedToday;
    private boolean isBanned;
    private long banExpiry;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.transactions = new ArrayList<>();
        this.warnings = 0;
        this.lastWarning = 0;
        this.lastReset = System.currentTimeMillis();
        this.moneyGainedToday = 0;
        this.isBanned = false;
        this.banExpiry = 0;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);

        if (transaction.getType() == Transaction.Type.GAIN) {
            moneyGainedToday += transaction.getAmount();
        }

        if (transactions.size() > 1000) {
            transactions.remove(0);
        }
    }

    public void addWarning() {
        warnings++;
        lastWarning = System.currentTimeMillis();
    }

    public void resetDailyStats() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long todayStart = cal.getTimeInMillis();

        moneyGainedToday = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.GAIN && t.getTimestamp() >= todayStart)
                .mapToDouble(Transaction::getAmount)
                .sum();

        lastReset = System.currentTimeMillis();
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

    public UUID getUuid() {
        return uuid;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public int getWarnings() {
        return warnings;
    }

    public long getLastWarning() {
        return lastWarning;
    }

    public double getMoneyGainedToday() {
        return moneyGainedToday;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public long getBanExpiry() {
        return banExpiry;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public void setMoneyGainedToday(double amount) {
        this.moneyGainedToday = amount;
    }
}