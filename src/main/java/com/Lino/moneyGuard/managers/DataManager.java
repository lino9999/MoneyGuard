package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final MoneyGuard plugin;
    private final Map<UUID, PlayerData> playerDataMap;

    public DataManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
    }

    public void loadData() {
    }

    public void saveAllData() {
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public void clearAll5MinuteData() {
        for (PlayerData data : playerDataMap.values()) {
            data.clearTransactions();
        }
    }
}