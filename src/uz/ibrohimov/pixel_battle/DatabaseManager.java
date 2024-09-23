package uz.ibrohimov.pixel_battle;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final PixelBattlePlugin plugin;
    private Connection connection;

    public DatabaseManager(PixelBattlePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbFile = plugin.getPluginConfig().getString("db_file", "db.sqlite");
        String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + dbFile;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            Bukkit.getLogger().info("SQLite connection established");

            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "uuid TEXT PRIMARY KEY,"
                + "username TEXT NOT NULL,"
                + "energy INTEGER NOT NULL,"
                + "last_energy_update LONG NOT NULL,"
                + "last_ip_address TEXT"
                + ");";

        String createBlockChangesTableSQL = "CREATE TABLE IF NOT EXISTS block_changes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uuid TEXT NOT NULL,"
                + "world TEXT NOT NULL,"
                + "x INTEGER NOT NULL,"
                + "y INTEGER NOT NULL,"
                + "z INTEGER NOT NULL,"
                + "block_type TEXT NOT NULL,"
                + "change_time LONG NOT NULL,"
                + "ip_address TEXT NOT NULL,"
                + "action TEXT NOT NULL"
                + ");";

        try (PreparedStatement pstmt = connection.prepareStatement(createUserTableSQL)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error creating users table: " + e.getMessage());
            e.printStackTrace();
        }

        try (PreparedStatement pstmt = connection.prepareStatement(createBlockChangesTableSQL)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error creating block_changes table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void recordBlockChange(String uuid, String world, int x, int y, int z, String blockType, String ipAddress, String action) {
        String sql = "INSERT INTO block_changes (uuid, world, x, y, z, block_type, change_time, ip_address, action) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, world);
            pstmt.setInt(3, x);
            pstmt.setInt(4, y);
            pstmt.setInt(5, z);
            pstmt.setString(6, blockType);
            pstmt.setLong(7, System.currentTimeMillis());
            pstmt.setString(8, ipAddress);
            pstmt.setString(9, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error recording block change: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                Bukkit.getLogger().info("SQLite connection closed");
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Error closing SQLite connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public int getPlayerEnergy(String uuid, String playerName) {
        String sql = "SELECT energy FROM users WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("energy");
            } else {
                // If player doesn't exist, create a new entry with starting energy
                int startingEnergy = plugin.getConfig().getInt("starting_energy");
                createNewPlayer(uuid, startingEnergy, playerName);
                return startingEnergy;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting player energy: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public void updatePlayerEnergy(String uuid, int change) {
        String sql = "UPDATE users SET energy = energy + ?, last_energy_update = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, change);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating player energy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createNewPlayer(String uuid, int startingEnergy, String playerName) {
        String sql = "INSERT INTO users (uuid, username, energy, last_energy_update) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, playerName);
            pstmt.setInt(3, startingEnergy);
            pstmt.setLong(4, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating new player: " + e.getMessage());
            e.printStackTrace();
        }
    }
}