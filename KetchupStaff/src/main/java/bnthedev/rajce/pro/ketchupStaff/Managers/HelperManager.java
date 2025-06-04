package bnthedev.rajce.pro.ketchupStaff.Managers;

import bnthedev.rajce.pro.ketchupStaff.KetchupStaff;
import bnthedev.rajce.pro.ketchupStaff.Utils.ColorUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HelperManager {

    private static final Set<UUID> afkPlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();

    public static void loadHelpers() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isHelper(p)) continue;
                    if (isAFK(p.getUniqueId())) continue;
                    addPlaytime(p.getName(), 1);
                }
            }
        }.runTaskTimerAsynchronously(KetchupStaff.getInstance(), 20 * 60, 20 * 60); // každou minutu

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isHelper(p)) continue;

                    UUID uuid = p.getUniqueId();
                    boolean currentlyAfk = isAFK(uuid);
                    boolean wasAfk = afkPlayers.contains(uuid);

                    if (currentlyAfk && !wasAfk) {
                        afkPlayers.add(uuid);
                        WebhookManager.sendMessage("👻 Helper " + p.getName() + " je AFK");
                    } else if (!currentlyAfk && wasAfk) {
                        afkPlayers.remove(uuid);
                        WebhookManager.sendMessage("🎮 Helper " + p.getName() + " už není AFK");
                    }
                }
            }
        }.runTaskTimer(KetchupStaff.getInstance(), 20 * 10, 20 * 10); // každých 10s
    }

    public static void recordActivity(UUID uuid) {
        lastActivity.put(uuid, System.currentTimeMillis());
    }

    public static boolean isAFK(UUID uuid) {
        Long last = lastActivity.get(uuid);
        if (last == null) return false;

        long delay = ConfigManager.getAfkDelayMillis();
        return System.currentTimeMillis() - last >= delay;
    }

    public static boolean isHelper(Player player) {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user == null) return false;

            String group = user.getPrimaryGroup();
            List<String> helperGroups = KetchupStaff.getInstance().getConfig().getStringList("helper-groups");

            return helperGroups.contains(group);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static void addPlaytime(String nickname, long minutes) {
        try {
            PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO playtime (nickname, time) VALUES (?, ?) ON DUPLICATE KEY UPDATE time = time + ?"
            );
            stmt.setString(1, nickname);
            stmt.setLong(2, minutes);
            stmt.setLong(3, minutes);
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Long> getAllPlaytime() {
        Map<String, Long> result = new HashMap<>();
        try {
            ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery("SELECT * FROM playtime");
            while (rs.next()) {
                result.put(rs.getString("nickname"), rs.getLong("time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void sendPlaytimeList(CommandSender sender) {
        Map<String, Long> data = getAllPlaytime();
        if (data.isEmpty()) {
            sender.sendMessage(ColorUtil.color("&cŽádná uložená data."));
            return;
        }

        String format = KetchupStaff.getInstance().getConfig().getString("messages.playtime-format", "&7%player%: &e%time%m");
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            String formattedTime = formatTime(entry.getValue());
            String line = format
                    .replace("%player%", entry.getKey())
                    .replace("%time%", formattedTime);
            sender.sendMessage(ColorUtil.color(line));
        }
    }

    public static long getPlaytimeRaw(String nickname) {
        long playtime = -1;
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT time FROM playtime WHERE nickname = ?");
            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                playtime = rs.getLong("time");
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playtime;
    }

    public static void sendPlaytime(CommandSender sender, String nick) {
        long playtime = getPlaytimeRaw(nick);
        if (playtime <= 0) {
            String notFoundMsg = KetchupStaff.getInstance().getConfig().getString("messages.playtime-not-found", "&cHelper %player% nemá žádný záznam.");
            sender.sendMessage(ColorUtil.color(notFoundMsg.replace("%player%", nick)));
            return;
        }

        String formattedTime = formatTime(playtime);
        String playtimeMsg = KetchupStaff.getInstance().getConfig().getString("messages.playtime-show", "&e%player% má odehráno %time%.");
        sender.sendMessage(ColorUtil.color(playtimeMsg.replace("%player%", nick).replace("%time%", formattedTime)));
    }

    public static int getMessageCount(String nick, String time) {
        long fromTimestamp = parseTimeToTimestamp(time);
        int count = 0;
        try {
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS count FROM chatlogs WHERE nickname = ? AND timestamp >= ?"
            );
            ps.setString(1, nick);
            ps.setLong(2, fromTimestamp);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private static long parseTimeToTimestamp(String time) {
        long now = System.currentTimeMillis();
        try {
            if (time.endsWith("mo")) {
                int val = Integer.parseInt(time.substring(0, time.length() - 2));
                return now - val * 30L * 24 * 60 * 60 * 1000;
            } else if (time.endsWith("d")) {
                int val = Integer.parseInt(time.substring(0, time.length() - 1));
                return now - val * 24L * 60 * 60 * 1000;
            } else if (time.endsWith("h")) {
                int val = Integer.parseInt(time.substring(0, time.length() - 1));
                return now - val * 60L * 60 * 1000;
            } else if (time.endsWith("m")) {
                int val = Integer.parseInt(time.substring(0, time.length() - 1));
                return now - val * 60L * 1000;
            }
        } catch (NumberFormatException ignored) {}
        return now - 24L * 60 * 60 * 1000;
    }

    private static String formatTime(long minutes) {
        if (minutes < 60) {
            return minutes + "m";
        } else if (minutes < 1440) {
            double hours = minutes / 60.0;
            return String.format("%.1fh", hours);
        } else {
            double days = minutes / 1440.0;
            return String.format("%.1fd", days);
        }
    }
}
