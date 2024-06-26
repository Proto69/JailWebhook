import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Entry point for the template plugin. You should edit
 * this comment by explaining the main purpose of your
 * plugin
 * You should also edit these tags below.
 *
 * @author Proto68
 * @version 1.0
 * @since 1.0
 */
public class JailWebhook extends JavaPlugin {

    private DatabaseManager databaseManager;
    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exist.
        saveDefaultConfig();

        boolean enable = getConfig().getBoolean("enable");

        if (enable) {
            // Registering the events and commands
            registerEvents();
            registerCommands();

            // Database setup
            try {
                databaseEnable();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Logging the successful enabling of the plugin
            getLogger().info("JailWebhook is enabled!");
        } else {
            getLogger().warning("JailWebhook is not enabled in the config!");
        }
    }

    @Override
    public void onDisable() {
        databaseDisable();
    }

    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }


    private void registerEvents() {
        // Getting the config.yml
        FileConfiguration config = this.getConfig();

        // Retrieving all values from the config.yml
        String url = config.getString("webhookURL");

        String jailPermission = config.getString("permissions.jail");
        String unJailPermission = config.getString("permissions.unjail");

        Color color = null;
        String hexColor = config.getString("color");

        String format = config.getString("format");

        boolean titleEnable = config.getBoolean("title.enable");
        String titleText = config.getString("title.text");
        // If the title is disabled in the config.yml
        // the title is set to null
        if (!titleEnable)
            titleText = null;


        boolean reasonEnable = config.getBoolean("fields.reason.enable");
        String reasonTitle = config.getString("fields.reason.title");
        String reasonField = config.getString("fields.reason.field");
        // If the reason is disabled in the config.yml
        // the reason's title is set to null
        if (!reasonEnable)
            reasonTitle = null;

        boolean durationEnable = config.getBoolean("fields.duration.enable");
        String durationTitle = config.getString("fields.duration.title");
        String durationField = config.getString("fields.duration.field");
        // If the duration is disabled in the config.yml
        // the duration's title is set to null
        if (!durationEnable)
            durationTitle = null;

        boolean cellEnable = config.getBoolean("fields.cell.enable");
        String cellTitle = config.getString("fields.cell.title");
        String cellField = config.getString("fields.cell.field");
        // If the cell is disabled in the config.yml
        // the cell's title is set to null
        if (!cellEnable)
            cellTitle = null;

        boolean lockedEnable = config.getBoolean("fields.lockedBy.enable");
        String lockedTitle = config.getString("fields.lockedBy.title");
        String lockedField = config.getString("fields.lockedBy.field");
        // If the lockedBy is disabled in the config.yml
        // the lockedBy's title is set to null
        if (!lockedEnable)
            lockedTitle = null;

        boolean timestampEnable = config.getBoolean("timeStamp.enable");
        String timestampFormat = config.getString("timeStamp.format");

        String description = config.getString("description.text");
        if (Objects.equals(format, "FIELDS"))
            // If the format is FIELDS in the config.yml
            // the description is set to null
            description = null;
        else if (!Objects.equals(format, "DESCRIPTION"))
            // If the format is neither FIELDS nor DESCRIPTION in the config.yml
            // throws error in the console for invalid format value
            getLogger().severe("Invalid message format! Use FIELDS or DESCRIPTION! Used: " + format);

        // Decodes the color from HEX to Color type
        try {
            assert hexColor != null;
            color = Color.decode(hexColor);
        } catch (NumberFormatException e) {
            // Throws error if the value in the config.yml is not
            // the correct format
            getLogger().severe("Invalid color format, use hex format, e.g. #FF5733");
        }

        // Creates the command listener with all the values from the config.yml
        JailCommandListener jailListener = new JailCommandListener(this, url, format, titleText, reasonTitle, durationTitle, cellTitle, lockedTitle, reasonField, durationField, cellField, lockedField, timestampEnable, timestampFormat, color, description, jailPermission);
        UnJailCommandListener unJailListener = new UnJailCommandListener(this, unJailPermission);

        // Registers the listener
        getServer().getPluginManager().registerEvents(jailListener, this);
        getServer().getPluginManager().registerEvents(unJailListener, this);
    }

    private void registerCommands() {
        // Registers the reload command
        Objects.requireNonNull(this.getCommand("jw")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("jw")).setTabCompleter(new PluginTabCompleter());
    }

    private void databaseEnable() throws SQLException {
        databaseManager = new DatabaseManager(getConfig(), getLogger());
        try {
            databaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        FileConfiguration config = getConfig();
        String query = "CREATE EVENT update_" + config.getString("database.table") + "\n" +
                "ON SCHEDULE EVERY " + config.getString("database.update-period") + "\n" +
                "DO\n" +
                "  UPDATE " + config.getString("database.table") + "\n" +
                "  SET active = 0\n" +
                "  WHERE jailed_to < DATE_ADD(NOW(), INTERVAL " + (Integer.parseInt(config.getString("database.time-difference")) * 60) + " MINUTE);";
        databaseManager.modifyEvent("update_" + config.getString("database.table"), query);
    }

    private void databaseDisable(){
        try {
            databaseManager.disconnect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Reloads the plugin
    public void reload() throws SQLException {
        // Reloads the configuration
        reloadConfig();

        // Unregisters all listeners
        HandlerList.unregisterAll(this);

        // Disconnect database
        databaseDisable();

        if (getConfig().getBoolean("enable"))
        {
            // Registers all events
            registerEvents();

            // Connect the database
            databaseEnable();
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("jw")) {
            if (args.length == 0) {
                sender.sendMessage("Usage: /jw <subcommand>");
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "reload":
                    ReloadCommand reload = new ReloadCommand(this);
                    reload.onCommand(sender, command, label, args);
                    break;
                case "all":
                    ListAllCommand listAllCommand = new ListAllCommand(this);
                    listAllCommand.onCommand(sender, command, label, args);
                    break;
                default:
                    sender.sendMessage("Unknown subcommand.");
                    break;
            }

            return true;
        }

        return false;
    }
}
