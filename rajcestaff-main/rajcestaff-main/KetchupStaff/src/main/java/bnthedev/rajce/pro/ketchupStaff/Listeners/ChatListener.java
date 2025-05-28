package bnthedev.rajce.pro.ketchupStaff.Listeners;

import bnthedev.rajce.pro.ketchupStaff.Managers.DatabaseManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.HelperManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.PreparedStatement;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!HelperManager.isHelper(e.getPlayer())) return;

        try {
            PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                    "INSERT INTO chatlogs (nickname, timestamp) VALUES (?, ?)"
            );
            stmt.setString(1, e.getPlayer().getName());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

