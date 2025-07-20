package com.Lino.moneyGuard.managers;

import com.Lino.moneyGuard.MoneyGuard;
import com.Lino.moneyGuard.data.PlayerData;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class ActionManager {

    private final MoneyGuard plugin;

    public ActionManager(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    public void warnPlayer(Player player, String reason) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        data.addWarning();

        String message = plugin.getMessageManager().getMessage("warnings.player-warning",
                "{warnings}", String.valueOf(data.getWarnings()),
                "{max}", String.valueOf(plugin.getConfigManager().getWarningsBeforeBan()),
                "{reason}", reason);

        player.sendMessage(message);

        plugin.getAlertManager().alertPlayerWarned(player, data.getWarnings(),
                plugin.getConfigManager().getWarningsBeforeBan());

        plugin.getLogManager().logWarning(player, reason, data.getWarnings());

        if (data.getWarnings() >= plugin.getConfigManager().getWarningsBeforeBan() &&
                plugin.getConfigManager().isAutoBanEnabled()) {
            banPlayer(player, reason);
        }
    }

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

        plugin.getAlertManager().alertPlayerBanned(player, reason);
        plugin.getLogManager().logBan(player, reason, duration);
    }

    public void unbanPlayer(String playerName) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            data.unban();
        }
    }

    public void resetWarnings(UUID uuid) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        data.setWarnings(0);
    }

    public boolean checkViolations(Player player) {
        double balance = plugin.getEconomyManager().getBalance(player);
        double gainedHour = plugin.getEconomyManager().getMoneyGainedInLastHour(player.getUniqueId());
        double gainedDay = plugin.getEconomyManager().getMoneyGainedToday(player.getUniqueId());

        boolean violation = false;

        if (balance > plugin.getConfigManager().getMaxTotalMoney()) {
            plugin.getAlertManager().alertMaxTotalMoney(player, balance,
                    plugin.getConfigManager().getMaxTotalMoney());

            if (plugin.getConfigManager().isAutoWarnEnabled()) {
                warnPlayer(player, "Exceeded maximum total money limit");
            }
            violation = true;
        }

        if (gainedHour > plugin.getConfigManager().getMaxMoneyPerHour()) {
            plugin.getAlertManager().alertMaxMoneyPerHour(player, gainedHour,
                    plugin.getConfigManager().getMaxMoneyPerHour());

            if (plugin.getConfigManager().isAutoWarnEnabled()) {
                warnPlayer(player, "Exceeded hourly money gain limit");
            }
            violation = true;
        }

        if (gainedDay > plugin.getConfigManager().getMaxMoneyPerDay()) {
            plugin.getAlertManager().alertMaxMoneyPerDay(player, gainedDay,
                    plugin.getConfigManager().getMaxMoneyPerDay());

            if (plugin.getConfigManager().isAutoWarnEnabled()) {
                warnPlayer(player, "Exceeded daily money gain limit");
            }
            violation = true;
        }

        return violation;
    }
}