package uz.ibrohimov.pixel_battle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class PixelBattleListener implements Listener {
    private final PixelBattlePlugin plugin;

    public PixelBattleListener(PixelBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!isWithinBattleArea(loc)) {
            return;
        }

        if (!isWhitelistedBlock(event.getBlock().getType())) {
            event.setCancelled(true);
            player.sendMessage("Siz faqat belgilangan bloklarni qo'ya olasiz");
            return;
        }

        int energyCost = plugin.getConfig().getInt("place_consumes");
        if (!hasEnoughEnergy(player, energyCost)) {
            event.setCancelled(true);
            player.sendMessage("Energiyangiz yetarli emas");
            return;
        }

        consumeEnergy(player, energyCost);
        recordBlockChange(player, loc, event.getBlock().getType().name(), "PLACE");
        player.sendMessage("Blok qo'yildi. Qolgan energiya: " + getPlayerEnergy(player));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        if (!isWithinBattleArea(loc)) {
            return;
        }

        int energyCost = plugin.getConfig().getInt("break_consumes");
        if (!hasEnoughEnergy(player, energyCost)) {
            event.setCancelled(true);
            player.sendMessage("Blokni sindirishga energiyangiz yetarli emas. Qolgan energiya: " + getPlayerEnergy(player));
            return;
        }

        consumeEnergy(player, energyCost);
        recordBlockChange(player, loc, "AIR", "BREAK");
        player.sendMessage("Blok sindirildi. Qolgan energiya: " + getPlayerEnergy(player));
    }

    private boolean isWithinBattleArea(Location loc) {
        int x1 = plugin.getConfig().getInt("x1");
        int x2 = plugin.getConfig().getInt("x2");
        int z1 = plugin.getConfig().getInt("z1");
        int z2 = plugin.getConfig().getInt("z2");
        int y = plugin.getConfig().getInt("y");
        String world = plugin.getConfig().getString("world");

        return loc.getWorld().getName().equals(world) &&
               loc.getBlockX() >= Math.min(x1, x2) && loc.getBlockX() <= Math.max(x1, x2) &&
               loc.getBlockZ() >= Math.min(z1, z2) && loc.getBlockZ() <= Math.max(z1, z2) &&
               loc.getBlockY() == y;
    }

    private boolean isWhitelistedBlock(Material material) {
        List<String> whitelist = plugin.getConfig().getStringList("blocks_whitelist");
        return whitelist.contains(material.name());
    }

    private boolean hasEnoughEnergy(Player player, int cost) {
        return plugin.getDatabaseManager().getPlayerEnergy(player.getUniqueId().toString(), player.getName()) >= cost;
    }

    private void consumeEnergy(Player player, int amount) {
        plugin.getDatabaseManager().updatePlayerEnergy(player.getUniqueId().toString(), -amount);
    }

    private int getPlayerEnergy(Player player) {
        return plugin.getDatabaseManager().getPlayerEnergy(player.getUniqueId().toString(), player.getName());
    }

    private void recordBlockChange(Player player, Location loc, String blockType, String action) {
        plugin.getDatabaseManager().recordBlockChange(
            player.getUniqueId().toString(),
            loc.getWorld().getName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ(),
            blockType,
            player.getAddress().getAddress().getHostAddress(),
            action
        );
    }
}