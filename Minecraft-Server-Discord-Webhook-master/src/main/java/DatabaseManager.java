import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class DatabaseManager {
    private Connection connection;
    private final FileConfiguration config;

    private final Logger logger;

    public DatabaseManager(FileConfiguration config, Logger logger){
        this.config = config;
        this.logger = logger;
    }

    public void connect() throws SQLException {
        String url = "jdbc:mysql://" + config.getString("database.address") + "/" + config.getString("database.database") + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
        String user = config.getString("database.username");
        String password = config.getString("database.password");
        connection = DriverManager.getConnection(url, user, password);

        createTableIfNotExists();
    }

    public void createTableIfNotExists() throws SQLException {
        String tableName = config.getString("database.table");
        if (!doesTableExist(tableName)) {
            String query = "CREATE TABLE " + tableName + "(\n" +
                    "\tid INT PRIMARY KEY AUTO_INCREMENT,\n" +
                    "    prisoner_name TEXT NOT NULL,\n" +
                    "    reason TEXT,\n" +
                    "    jailed_by TEXT NOT NULL,\n" +
                    "    unjailed_by TEXT NOT NULL,\n" +
                    "    jailed_at DATETIME NOT NULL,\n" +
                    "    jailed_to DATETIME NOT NULL,\n" +
                    "    active BOOLEAN NOT NULL DEFAULT FALSE\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
            String query2 = "ALTER DATABASE " + config.getString("database.database") + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";
            String query3 = "SET GLOBAL event_scheduler = ON;";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(query);
                statement.executeUpdate(query2);
                statement.executeUpdate(query3);
                logger.info("Database table was created!");
                logger.info("Event scheduler was set up!");
            } catch (SQLException e) {
                logger.severe("Error creating table: " + e.getMessage());
                throw e;
            }
        } else {
            logger.info("Table already exists.");
        }
    }

    private void createEvent(String eventName, String query) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        }
    }

    public void modifyEvent(String eventName, String query) throws SQLException {
        // To modify an event, you typically need to drop it and recreate it because MySQL does not support direct modifications.
        dropEvent(eventName);
        createEvent(eventName, query);
        logger.info("Event modified: " + eventName);
    }

    private void dropEvent(String eventName) throws SQLException {
        String query = "DROP EVENT IF EXISTS " + eventName;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        }
    }


    private boolean doesTableExist(String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }


    public void uploadJailData(String[] data, CommandSender sender) throws SQLException {
        String query = "INSERT INTO " + config.getString("database.table") + " (prisoner_name, reason, jailed_by, unjailed_by, jailed_at, jailed_to, active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, data[0]);
        statement.setString(2, data[1]);
        statement.setString(3, data[2]);
        statement.setString(4, data[3]);
        statement.setString(5, data[4]);
        statement.setString(6, data[5]);
        statement.setString(7, data[6]);
        int num = statement.executeUpdate();
        if (num != 1)
            sender.sendMessage("Error occurred while uploading data to the database!");
    }

    public void updateUnJailData(String unjailedBy, String prisonerName) throws SQLException {
        String query = "UPDATE " + config.getString("database.table") + " \n" +
                "SET active = 0, unjailed_by = ?\n" +
                "WHERE prisoner_name = ? AND jailed_to > NOW() AND active = 1;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, unjailedBy);
            statement.setString(2, prisonerName);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Update successful. Rows affected: " + rowsUpdated);
            } else {
                logger.warning("No rows matched the criteria.");
            }
        }
    }

    public List<Map<String, Object>> getData(String columnName, String value) throws SQLException {
        String query = "SELECT * FROM " + config.getString("database.table");
        PreparedStatement statement = connection.prepareStatement(query);
        if (!Objects.equals(columnName, " ")){
            query = "SELECT * FROM " + config.getString("database.table") + " WHERE " + columnName + " = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, value);
        }

        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSetToList(resultSet);
        }
    }

    private List<Map<String, Object>> resultSetToList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
