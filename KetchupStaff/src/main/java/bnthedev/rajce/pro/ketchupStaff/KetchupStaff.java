package bnthedev.rajce.pro.ketchupStaff;

import bnthedev.rajce.pro.ketchupStaff.Commands.KetchupCommand;
import bnthedev.rajce.pro.ketchupStaff.Listeners.AfkListeners;
import bnthedev.rajce.pro.ketchupStaff.Listeners.ChatListener;
import bnthedev.rajce.pro.ketchupStaff.Listeners.JoinQuitListener;
import bnthedev.rajce.pro.ketchupStaff.Managers.ConfigManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.DatabaseManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.HelperManager;
import bnthedev.rajce.pro.ketchupStaff.Managers.WebhookManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KetchupStaff extends JavaPlugin {

    private static KetchupStaff instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigManager.setup(this);

        DatabaseManager.connect();
        WebhookManager.init();
        HelperManager.loadHelpers();

        getCommand("ketchupstaff").setExecutor(new KetchupCommand());
        getCommand("ketchupstaff").setTabCompleter(new KetchupCommand());

        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new AfkListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        getLogger().info("Plugin KetchupStaff byl zapnut.");
    }

    @Override
    public void onDisable() {
        DatabaseManager.disconnect();
        getLogger().info("Plugn KetchupStaff byl vypnut.");
    }

    public static KetchupStaff getInstance() {
        return instance;
    }
}
