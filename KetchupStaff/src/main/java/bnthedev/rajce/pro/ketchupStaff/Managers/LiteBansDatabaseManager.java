package bnthedev.rajce.pro.ketchupStaff.Managers;

import bnthedev.rajce.pro.ketchupStaff.KetchupStaff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class LiteBansDatabaseManager {

    private static Connection connection;

    public static void connect() {
        try {
            String host = KetchupStaff.getInstance().getConfig().getString("litebans-database.host");
            int port = KetchupStaff.getInstance().getConfig().getInt("litebans-database.port");
            String database = KetchupStaff.getInstance().getConfig().getString("litebans-database.name");
            String user = KetchupStaff.getInstance().getConfig().getString("litebans-database.user");
            String password = KetchupStaff.getInstance().getConfig().getString("litebans-database.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&characterEncoding=utf8";
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static Map<String, Integer> getPunishmentCounts(String staffName) {
        return getPunishmentCounts(staffName, null);
    }

    public static Map<String, Integer> getPunishmentCounts(String staffName, String timeRange) {
        Map<String, Integer> punishments = new HashMap<>();

        punishments.put("bans", getCount("litebans_bans", staffName, timeRange));
        punishments.put("mutes", getCount("litebans_mutes", staffName, timeRange));
        punishments.put("warnings", getCount("litebans_warnings", staffName, timeRange));
        punishments.put("kicks", getCount("litebans_kicks", staffName, timeRange));

        return punishments;
    }

    private static int getCount(String table, String staffName, String timeRange) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(*) AS count FROM " + table + " WHERE banned_by_name = ?";
            PreparedStatement ps;

            if (timeRange != null) {
                long timestamp = System.currentTimeMillis() / 1000L - parseTimeRange(timeRange);
                sql += " AND time >= ?";
                ps = connection.prepareStatement(sql);
                ps.setString(1, staffName);
                ps.setLong(2, timestamp);
            } else {
                ps = connection.prepareStatement(sql);
                ps.setString(1, staffName);
            }

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

    private static long parseTimeRange(String input) {
        try {
            input = input.trim().toLowerCase();
            if (input.endsWith("d")) {
                int days = Integer.parseInt(input.replace("d", ""));
                return days * 24L * 60L * 60L;
            } else if (input.endsWith("h")) {
                int hours = Integer.parseInt(input.replace("h", ""));
                return hours * 60L * 60L;
            }
        } catch (Exception ignored) {}
        return 0L;
    }
}
