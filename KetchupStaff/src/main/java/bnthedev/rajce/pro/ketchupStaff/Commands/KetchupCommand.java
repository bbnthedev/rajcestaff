package bnthedev.rajce.pro.ketchupStaff.Commands;

import bnthedev.rajce.pro.ketchupStaff.KetchupStaff;
import bnthedev.rajce.pro.ketchupStaff.Managers.ConfigManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.DatabaseManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.HelperManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.LiteBansDatabaseManager;
import bnthedev.rajce.pro.ketchupStaff.Utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class KetchupCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.only-player")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.usage-main")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.list"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }
                player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.cmd-list")));
                HelperManager.sendPlaytimeList(player);
                break;

            case "reset":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.reset"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }
                player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.cmd-reset")));
                DatabaseManager.resetDatabase();
                break;
            case "reload":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.reload"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }
                ConfigManager.reload(KetchupStaff.getInstance());
                player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.reload-success")));
                break;
            case "stats":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.stats"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }

                if (args.length == 2) {
                    String staff = args[1];
                    Map<String, Integer> stats = LiteBansDatabaseManager.getPunishmentCounts(staff);

                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-header")
                            .replace("%player%", staff)));

                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-bans")
                            .replace("%count%", String.valueOf(stats.getOrDefault("bans", 0)))));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-mutes")
                            .replace("%count%", String.valueOf(stats.getOrDefault("mutes", 0)))));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-warnings")
                            .replace("%count%", String.valueOf(stats.getOrDefault("warnings", 0)))));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-kicks")
                            .replace("%count%", String.valueOf(stats.getOrDefault("kicks", 0)))));

                    long playtimeMinutes = HelperManager.getPlaytimeRaw(staff); // minutes
                    String timeFormatted;

                    if (playtimeMinutes < 60) {
                        timeFormatted = playtimeMinutes + "m";
                    } else if (playtimeMinutes < 60 * 24) {
                        timeFormatted = String.format("%.1f", playtimeMinutes / 60.0) + "h";
                    } else {
                        timeFormatted = String.format("%.1f", playtimeMinutes / 60.0 / 24.0) + "d";
                    }

                    player.sendMessage(ColorUtil.color(
                            KetchupStaff.getInstance().getConfig().getString("messages.stats-playtime")
                                    .replace("%time%", timeFormatted)
                    ));
                    int msgCountAll = HelperManager.getMessageCount(staff, "7d");
                    player.sendMessage(ColorUtil.color(
                            KetchupStaff.getInstance().getConfig().getString("messages.stats-messages-7d")
                                    .replace("%count%", String.valueOf(msgCountAll))
                    ));

                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.usage-stats")));
                    return true;
                }

                String typeInput = args[1].toLowerCase();

                if (typeInput.equals("playtime")) {
                    String nickPlaytime = args[2];
                    HelperManager.sendPlaytime(player, nickPlaytime);

                } else if (typeInput.equals("checkmsg")) {
                    if (args.length < 4) {
                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.usage-checkmsg")));
                        return true;
                    }
                    String nickCheck = args[2];
                    String timeCheck = args[3];
                    int messageCount = HelperManager.getMessageCount(nickCheck, timeCheck);
                    String msgCheck = KetchupStaff.getInstance().getConfig().getString("messages.message-check")
                            .replace("%player%", nickCheck)
                            .replace("%count%", String.valueOf(messageCount));
                    player.sendMessage(ColorUtil.color(msgCheck));

                } else {
                    String staff = args[2];
                    String timeRange = args.length >= 4 ? args[3] : null;
                    Map<String, Integer> stats = LiteBansDatabaseManager.getPunishmentCounts(staff, timeRange);

                    if (typeInput.equals("all")) {
                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-header")
                                .replace("%player%", staff)));

                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-bans")
                                .replace("%count%", String.valueOf(stats.getOrDefault("bans", 0)))));
                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-mutes")
                                .replace("%count%", String.valueOf(stats.getOrDefault("mutes", 0)))));
                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-warnings")
                                .replace("%count%", String.valueOf(stats.getOrDefault("warnings", 0)))));
                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-kicks")
                                .replace("%count%", String.valueOf(stats.getOrDefault("kicks", 0)))));

                        long playtimeAll = HelperManager.getPlaytimeRaw(staff);
                        String timeFmt = String.format("%.1f", playtimeAll / 60.0);
                        player.sendMessage(ColorUtil.color("&ePlaytime: &f" + timeFmt + "h"));

                        int msgCountAll = HelperManager.getMessageCount(staff, "7d");
                        player.sendMessage(ColorUtil.color("&eMessages (7d): &f" + msgCountAll));

                    } else {
                        String key = switch (typeInput) {
                            case "ban", "bans" -> "bans";
                            case "mute", "mutes" -> "mutes";
                            case "warn", "warning", "warnings" -> "warnings";
                            case "kick", "kicks" -> "kicks";
                            default -> null;
                        };

                        if (key == null || !stats.containsKey(key)) {
                            player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-invalid-type")));
                        } else {
                            String single = KetchupStaff.getInstance().getConfig().getString("messages.stats-single")
                                    .replace("%player%", staff)
                                    .replace("%type%", typeInput)
                                    .replace("%count%", String.valueOf(stats.getOrDefault(key, 0)));
                            player.sendMessage(ColorUtil.color(single));
                        }
                    }
                }
                break;

            default:
                player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.unknown-subcommand")));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.stats"))) subcommands.add("stats");
            return subcommands.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            List<String> options = new ArrayList<>(Arrays.asList("ban", "mute", "warn", "kick", "playtime", "checkmsg"));
            options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            return options.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("stats") &&
                (args[1].equalsIgnoreCase("ban") || args[1].equalsIgnoreCase("mute") || args[1].equalsIgnoreCase("warn") || args[1].equalsIgnoreCase("kick") || args[1].equalsIgnoreCase("playtime") || args[1].equalsIgnoreCase("checkmsg"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("stats") &&
                (args[1].equalsIgnoreCase("ban") || args[1].equalsIgnoreCase("mute") || args[1].equalsIgnoreCase("warn") || args[1].equalsIgnoreCase("kick"))) {
            return List.of("1d", "7d", "30d", "24h", "12h", "90d", "365d");
        }

        return Collections.emptyList();
    }
}
