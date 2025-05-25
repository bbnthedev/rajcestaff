package bnthedev.rajce.pro.ketchupStaff.Listeners;

import bnthedev.rajce.pro.ketchupStaff.Managers.HelperManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.WebhookManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (HelperManager.isHelper(e.getPlayer())) {
            WebhookManager.sendMessage("✅ Helper **" + e.getPlayer().getName() + "** se připojil na server.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (HelperManager.isHelper(e.getPlayer())) {
            WebhookManager.sendMessage("❌ Helper **" + e.getPlayer().getName() + "** se odpojil ze serveru.");
        }
    }
}

