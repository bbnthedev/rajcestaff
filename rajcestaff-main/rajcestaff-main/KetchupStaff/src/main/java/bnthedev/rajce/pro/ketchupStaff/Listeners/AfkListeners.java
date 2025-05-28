package bnthedev.rajce.pro.ketchupStaff.Listeners;

import bnthedev.rajce.pro.ketchupStaff.Managers.HelperManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AfkListeners implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (HelperManager.isHelper(e.getPlayer())) {
            HelperManager.recordActivity(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (HelperManager.isHelper(e.getPlayer())) {
            HelperManager.recordActivity(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (HelperManager.isHelper(e.getPlayer())) {
            HelperManager.recordActivity(e.getPlayer().getUniqueId());
        }
    }
}