package uz.ibrohimov.pixel_battle;

import org.bukkit.plugin.java.JavaPlugin;

public class PixelBattlePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("PixelBattle plugin has been enabled!");
        
        // TODO: Initialize config
        // TODO: Setup database connection
        // TODO: Register events
        // TODO: Register commands
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PixelBattle plugin has been disabled!");
        
        // TODO: Save data and close database connection
    }
}