package uz.ibrohimov.pixel_battle;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PixelBattlePlugin extends JavaPlugin {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("PixelBattle plugin has been enabled!");
        
        // Load configuration
        loadConfig();
        
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

    private void loadConfig() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Load the config
        reloadConfig();
        config = getConfig();
        
        // Set default values if they don't exist
        config.addDefault("db_file", "db.sqlite");
        config.addDefault("world", "world");
        config.addDefault("x1", -100);
        config.addDefault("x2", 100);
        config.addDefault("z1", -100);
        config.addDefault("z2", 100);
        config.addDefault("y", 256);
        config.addDefault("starting_energy", 10);
        config.addDefault("refill_time", 120);
        config.addDefault("max_energy", 10);
        config.addDefault("break_consumes", 2);
        config.addDefault("place_consumes", 1);
        config.addDefault("blocks_whitelist", Arrays.asList("COBBLESTONE", "IRON_BLOCK", "COAL_BLOCK", "SNOW_BLOCK"));
        
        // Save the config
        config.options().copyDefaults(true);
        saveConfig();
    }

    // Getter for the config
    public FileConfiguration getPluginConfig() {
        return config;
    }
}