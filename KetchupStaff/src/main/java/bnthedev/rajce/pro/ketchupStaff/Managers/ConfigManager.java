package bnthedev.rajce.pro.ketchupStaff.Managers;

import bnthedev.rajce.pro.ketchupStaff.KetchupStaff;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private static FileConfiguration config;

    public static void setup(KetchupStaff plugin) {
        config = plugin.getConfig();
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&6[KetchupStaff]&r "));
    }

    public static String getWebhookUrl() {
        return config.getString("webhook", "");
    }

    public static String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public static int getDatabasePort() {
        return config.getInt("database.port", 3306);
    }

    public static String getDatabaseName() {
        return config.getString("database.name", "ketchupstaff");
    }

    public static String getDatabaseUser() {
        return config.getString("database.user", "root");
    }

    public static String getDatabasePassword() {
        return config.getString("database.password", "");
    }

    public static long getAfkDelayMillis() {
        return config.getLong("afk_delay_minutes", 5) * 60 * 1000;
    }

    public static String getPermission(String node) {
        return config.getString("permissions." + node, "ketchupstuff." + node);
    }
}
