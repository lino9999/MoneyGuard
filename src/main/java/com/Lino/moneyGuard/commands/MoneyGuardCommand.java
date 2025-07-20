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
            if (sender instanceof Player && sender.hasPermission("moneyguard.admin")) {
                plugin.getGuiManager().openMainMenu((Player) sender);
                return true;
            }

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

            case "warn":
                if (!sender.hasPermission("moneyguard.warn")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("commands.warn-usage"));
                    return true;
                }

                Player warnTarget = plugin.getServer().getPlayer(args[1]);
                if (warnTarget == null) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-not-found"));
                    return true;
                }

                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                plugin.getActionManager().warnPlayer(warnTarget, reason);
                sender.sendMessage(plugin.getMessageManager().getMessage("commands.warn-success",
                        "{player}", warnTarget.getName()));
                break;

            case "reset":
                if (!sender.hasPermission("moneyguard.reset")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.no-permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("commands.reset-usage"));
                    return true;
                }

                Player resetTarget = plugin.getServer().getPlayer(args[1]);
                if (resetTarget == null) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("errors.player-not-found"));
                    return true;
                }

                plugin.getActionManager().resetWarnings(resetTarget.getUniqueId());
                sender.sendMessage(plugin.getMessageManager().getMessage("commands.reset-success",
                        "{player}", resetTarget.getName()));
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
            List<String> subcommands = Arrays.asList("reload", "check", "warn", "reset", "help");

            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase()) &&
                        sender.hasPermission("moneyguard." + sub)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("warn") ||
                    args[0].equalsIgnoreCase("reset")) {

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
            sender.sendMessage(plugin.getMessageManager().getMessage("help.admin"));
        }

        if (sender.hasPermission("moneyguard.check")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.check"));
        }

        if (sender.hasPermission("moneyguard.warn")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.warn"));
        }

        if (sender.hasPermission("moneyguard.reset")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("help.reset"));
        }

        sender.sendMessage(plugin.getMessageManager().getMessage("help.help"));
    }

    private void showPlayerInfo(CommandSender sender, Player target) {
        double balance = plugin.getEconomyManager().getBalance(target);
        double gainedHour = plugin.getEconomyManager().getMoneyGainedInLastHour(target.getUniqueId());
        double gainedDay = plugin.getEconomyManager().getMoneyGainedToday(target.getUniqueId());
        int warnings = plugin.getDataManager().getPlayerData(target.getUniqueId()).getWarnings();

        sender.sendMessage(plugin.getMessageManager().getMessage("info.header", "{player}", target.getName()));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.balance", "{amount}", String.format("%.2f", balance)));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.gained-hour", "{amount}", String.format("%.2f", gainedHour)));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.gained-day", "{amount}", String.format("%.2f", gainedDay)));
        sender.sendMessage(plugin.getMessageManager().getMessage("info.warnings", "{count}", String.valueOf(warnings)));
    }
}