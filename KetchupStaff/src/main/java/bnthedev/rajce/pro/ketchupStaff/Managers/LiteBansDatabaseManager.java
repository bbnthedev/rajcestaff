package bnthedev.rajce.pro.ketchupStaff.Managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import bnthedev.rajce.pro.ketchupStaff.KetchupStaff;

public class LiteBansDatabaseManager {

    private static Connection connection;

    public static void connect() {
        try {
            String host = KetchupStaff.getInstance().getConfig().getString("litebans-database.host");
            int port = KetchupStaff.getInstance().getConfig().getInt("litebans-database.port");
            String database = KetchupStaff.getInstance().getConfig().getString("litebans-database.name");
            String user = KetchupStaff.getInstance().getConfig().getString("litebans-database.user");
            String password = KetchupStaff.getInstance().getConfig().getString("litebans-database.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
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
        Map<String, Integer> punishments = new HashMap<>();

        punishments.put("bans", getCount("bans", "banned_by", staffName));
        punishments.put("mutes", getCount("mutes", "muted_by", staffName));
        punishments.put("warnings", getCount("warnings", "warned_by", staffName));
        punishments.put("kicks", getCount("kicks", "kicked_by", staffName));

        return punishments;
    }

    private static int getCount(String table, String column, String staffName) {
        int count = 0;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) AS count FROM " + table + " WHERE " + column + " = ?");
            ps.setString(1, staffName);
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
}
