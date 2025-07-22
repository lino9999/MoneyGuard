package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class ActionManager {

    private final MoneyGuard plugin;

    public ActionManager(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void banPlayer(Player player, String reason) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        long duration = plugin.getConfigManager().getBanDuration() * 60000L;

        data.ban(duration);

        Date expiry = new Date(System.currentTimeMillis() + duration);
        String banMessage = plugin.getMessageManager().getMessage("ban.message",
                "{reason}", reason,
                "{duration}", String.valueOf(plugin.getConfigManager().getBanDuration()));

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                banMessage,
                expiry,
                "MoneyGuard"
        );

        player.kickPlayer(banMessage);
        plugin.getLogManager().logBan(player, reason, duration);
    }

    @SuppressWarnings("deprecation")
    public void unbanPlayer(String playerName) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            data.unban();
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer.hasPlayedBefore()) {
                PlayerData data = plugin.getDataManager().getPlayerData(offlinePlayer.getUniqueId());
                data.unban();
            }
        }
    }
}