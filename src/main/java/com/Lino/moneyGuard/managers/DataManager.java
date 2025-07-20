package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final MoneyGuard plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(MoneyGuard plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.contains("players")) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                PlayerData data = new PlayerData(uuid);

                String path = "players." + uuidString + ".";
                data.setWarnings(dataConfig.getInt(path + "warnings", 0));
                data.setMoneyGainedToday(dataConfig.getDouble(path + "money-gained-today", 0.0));

                if (dataConfig.getBoolean(path + "banned", false)) {
                    long banExpiry = dataConfig.getLong(path + "ban-expiry", 0);
                    if (banExpiry > System.currentTimeMillis()) {
                        data.ban(banExpiry - System.currentTimeMillis());
                    }
                }

                playerDataMap.put(uuid, data);
            }
        }
    }

    public void saveAllData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            savePlayerData(entry.getKey(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
        }
    }

    private void savePlayerData(UUID uuid, PlayerData data) {
        String path = "players." + uuid.toString() + ".";

        dataConfig.set(path + "warnings", data.getWarnings());
        dataConfig.set(path + "money-gained-today", data.getMoneyGainedToday());
        dataConfig.set(path + "banned", data.isBanned());

        if (data.isBanned()) {
            dataConfig.set(path + "ban-expiry", data.getBanExpiry());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
        dataConfig.set("players." + uuid.toString(), null);
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return new ConcurrentHashMap<>(playerDataMap);
    }

    public void resetAllDailyStats() {
        for (PlayerData data : playerDataMap.values()) {
            data.resetDailyStats();
        }
    }
}