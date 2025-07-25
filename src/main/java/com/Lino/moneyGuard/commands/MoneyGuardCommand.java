package com.Lino.moneyGuard.commands;

import com.Lino.moneyGuard.MoneyGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MoneyGuardCommand implements CommandExecutor, TabCompleter {

    private final MoneyGuard plugin;

    public MoneyGuardCommand(MoneyGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("moneyguard.admin")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }

                plugin.reload();
                sender.sendMessage(plugin.getMessageManager().getMessage("commands.reload-success"));
                break;

            case "check":
                if (!sender.hasPermission("moneyguard.check")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("commands.check-usage"));
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-not-found"));
                    return true;
                }

                showPlayerInfo(sender, target);
                break;

            case "stats":
                if (!sender.hasPermission("moneyguard.stats")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }

                showStats(sender);
                break;

            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "check", "stats", "help");

            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (sub.equals("reload") && !sender.hasPermission("moneyguard.admin")) continue;
                    if (sub.equals("check") && !sender.hasPermission("moneyguard.check")) continue;
                    if (sub.equals("stats") && !sender.hasPermission("moneyguard.stats")) continue;
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check") && sender.hasPermission("moneyguard.check")) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getMessage("help.header"));

        if (sender.hasPermission("moneyguard.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.reload"));
        }

        if (sender.hasPermission("moneyguard.check")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.check"));
        }

        if (sender.hasPermission("moneyguard.stats")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.stats"));
        }

        sender.sendMessage(plugin.getMessageManager().getMessage("help.help"));
    }

    private void showPlayerInfo(CommandSender sender, Player target) {
        double balance = plugin.getEconomyManager().getBalance(target);
        double gainedMinute = plugin.getEconomyManager().getMoneyGainedInLastMinute(target.getUniqueId());
        double gained5Min = plugin.getEconomyManager().getMoneyGainedInLast5Minutes(target.getUniqueId());

        sender.sendMessage(plugin.getMessageManager().getMessage("info.header", "{player}", target.getName()));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.balance", "{amount}", String.format("%.2f", balance)));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.gained-minute", "{amount}", String.format("%.2f", gainedMinute)));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.gained-5min", "{amount}", String.format("%.2f", gained5Min)));
    }

    private void showStats(CommandSender sender) {
        long totalRemoved = plugin.getStatsManager().getTotalMoneyRemoved();
        int totalSuspicious = plugin.getStatsManager().getTotalSuspiciousTransactions();
        int totalChecked = plugin.getStatsManager().getTotalPlayersChecked();

        sender.sendMessage(plugin.getMessageManager().getMessage("stats.header"));
        sender.sendMessage(plugin.getMessageManager().getMessage("stats.money-removed", "{amount}", String.format("%,d", totalRemoved)));
        sender.sendMessage(plugin.getMessageManager().getMessage("stats.suspicious-transactions", "{count}", String.format("%,d", totalSuspicious)));
        sender.sendMessage(plugin.getMessageManager().getMessage("stats.players-checked", "{count}", String.format("%,d", totalChecked)));
        sender.sendMessage(plugin.getMessageManager().getMessage("stats.footer"));
    }
}