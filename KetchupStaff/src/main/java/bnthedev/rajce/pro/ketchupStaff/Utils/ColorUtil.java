package bnthedev.rajce.pro.ketchupStaff.Utils;

import org.bukkit.ChatColor;

public class ColorUtil {
    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}