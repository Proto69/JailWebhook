import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
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

    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exist.
        saveDefaultConfig();

        // Registering the events and commands
        registerEvents();
        registerCommands();

        // Logging the successful enabling of the plugin
        getLogger().info("JailWebhook is enabled!");
    }

    private void registerEvents(){
        // Getting the config.yml
        FileConfiguration config = this.getConfig();

        // Retrieving all values from the config.yml
        String url = config.getString("webhookURL");

        String permission = config.getString("permission");

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
        CommandListener listener = new CommandListener(this, url , format, titleText, reasonTitle, durationTitle, cellTitle, lockedTitle, reasonField, durationField, cellField, lockedField, timestampEnable, timestampFormat, color, description, permission);

        // Registers the listener
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommands(){
        // Registers the reload command
        Objects.requireNonNull(getCommand("jwreload")).setExecutor(new ReloadCommand(this));
    }

    // Reloads the plugin
    public void reload(){
        // Reloads the configuration
        reloadConfig();

        // Unregisters all listeners
        HandlerList.unregisterAll(this);

        // Registers all events
        registerEvents();
    }
}
