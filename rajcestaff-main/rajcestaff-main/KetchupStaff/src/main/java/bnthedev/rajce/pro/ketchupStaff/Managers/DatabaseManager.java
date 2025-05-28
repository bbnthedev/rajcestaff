package bnthedev.rajce.pro.ketchupStaff.Managers;

import java.sql.*;

public class DatabaseManager {

    private static Connection connection;

    public static void connect() {
        try {
            String url = "jdbc:mysql://" + ConfigManager.getDatabaseHost() + ":" + ConfigManager.getDatabasePort() + "/" + ConfigManager.getDatabaseName();
            connection = DriverManager.getConnection(url, ConfigManager.getDatabaseUser(), ConfigManager.getDatabasePassword());
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS playtime (nickname VARCHAR(16) PRIMARY KEY, time BIGINT)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS chatlogs (nickname VARCHAR(16), timestamp BIGINT)");
        stmt.close();
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
    public static void resetDatabase() {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM playtime");
            stmt.executeUpdate("DELETE FROM chatlogs");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // TODO
    public static void removePlayer(String nickname) {
        try {
            PreparedStatement stmt1 = connection.prepareStatement("DELETE FROM playtime WHERE nickname = ?");
            stmt1.setString(1, nickname);
            stmt1.executeUpdate();
            stmt1.close();

            PreparedStatement stmt2 = connection.prepareStatement("DELETE FROM chatlogs WHERE nickname = ?");
            stmt2.setString(1, nickname);
            stmt2.executeUpdate();
            stmt2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
