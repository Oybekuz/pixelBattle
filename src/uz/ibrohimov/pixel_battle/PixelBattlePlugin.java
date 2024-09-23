package uz.ibrohimov.pixel_battle;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public class PixelBattlePlugin extends JavaPlugin {

    private FileConfiguration config;
    private DatabaseManager databaseManager;
    private BukkitTask energyRefillTask;

    @Override
    public void onEnable() {
        getLogger().info("PixelBattle plugin has been enabled!");
        
        loadConfig();
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        // Register event listener
        getServer().getPluginManager().registerEvents(new PixelBattleListener(this), this);
        
        // Start energy refill task
        startEnergyRefillTask();
    }

    @Override
    public void onDisable() {
        getLogger().info("PixelBattle plugin has been disabled!");
        
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        if (energyRefillTask != null) {
            energyRefillTask.cancel();
        }
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        
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
        
        config.options().copyDefaults(true);
        saveConfig();
    }

    private void startEnergyRefillTask() {
        int refillTime = getConfig().getInt("refill_time");
        int maxEnergy = getConfig().getInt("max_energy");
        
        energyRefillTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String uuid = player.getUniqueId().toString();
                String playerName = player.getName();
                int currentEnergy = databaseManager.getPlayerEnergy(uuid, playerName);
                if (currentEnergy < maxEnergy) {
                    databaseManager.updatePlayerEnergy(uuid, 1);
                    player.sendMessage("Sizda energiya oshdi. Hozirgi energiya: " + (currentEnergy + 1));
                }
            }
        }, refillTime * 20L, refillTime * 20L); // Convert seconds to ticks
    }

    public FileConfiguration getPluginConfig() {
        return config;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}