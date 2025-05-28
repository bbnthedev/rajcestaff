package bnthedev.rajce.pro.ketchupStaff.Commands;

import bnthedev.rajce.pro.ketchupStaff.KetchupStaff;
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

            case "checkmsg":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.checkmsg"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.usage-checkmsg")));
                    return true;
                }
                String nick = args[1];
                String time = args[2];

                int count = HelperManager.getMessageCount(nick, time);
                String msg = KetchupStaff.getInstance().getConfig().getString("messages.message-check")
                        .replace("%player%", nick)
                        .replace("%count%", String.valueOf(count));
                player.sendMessage(ColorUtil.color(msg));
                break;
            case "stats":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.stats"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.usage-stats")));
                    return true;
                }

                String staff = args[1];
                Map<String, Integer> stats = LiteBansDatabaseManager.getPunishmentCounts(staff);

                if (args.length == 3) {
                    String type = args[2].toLowerCase();
                    String key = switch (type) {
                        case "ban" -> "bans";
                        case "mute" -> "mutes";
                        case "warn" -> "warnings";
                        case "kick" -> "kicks";
                        default -> null;
                    };

                    if (key == null || !stats.containsKey(key)) {
                        player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-invalid-type")));
                    } else {
                        String single = KetchupStaff.getInstance().getConfig().getString("messages.stats-single")
                                .replace("%player%", staff)
                                .replace("%type%", type)
                                .replace("%count%", String.valueOf(stats.get(key)));
                        player.sendMessage(ColorUtil.color(single));
                    }
                } else {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-header").replace("%player%", staff)));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-bans").replace("%count%", String.valueOf(stats.get("bans")))));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-mutes").replace("%count%", String.valueOf(stats.get("mutes")))));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-warnings").replace("%count%", String.valueOf(stats.get("warnings")))));
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.stats-kicks").replace("%count%", String.valueOf(stats.get("kicks")))));
                }
                break;
            case "playtime":
                if (!player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.playtime"))) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.color(KetchupStaff.getInstance().getConfig().getString("messages.usage-playtime")));
                    return true;
                }

                String target = args[1];
                HelperManager.sendPlaytime(player, target);
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
            if (player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.list"))) subcommands.add("list");
            if (player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.reset"))) subcommands.add("reset");
            if (player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.checkmsg"))) subcommands.add("checkmsg");
            if (player.hasPermission(KetchupStaff.getInstance().getConfig().getString("permissions.playtime"))) subcommands.add("playtime");

            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("checkmsg") || args[0].equalsIgnoreCase("playtime")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
