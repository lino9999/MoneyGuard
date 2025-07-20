package com.Lino.moneyGuard.data;

import java.util.UUID;

public class Transaction {

    public enum Type {
        GAIN,
        LOSS,
        TRANSFER_IN,
        TRANSFER_OUT,
        UNKNOWN
    }

    private final UUID playerUuid;
    private final double amount;
    private final Type type;
    private final long timestamp;

    public Transaction(UUID playerUuid, double amount, Type type, long timestamp) {
        this.playerUuid = playerUuid;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public double getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
}