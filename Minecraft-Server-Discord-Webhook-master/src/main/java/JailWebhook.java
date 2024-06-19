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

        registerEvents();
        registerCommands();

        getLogger().info("JailWebhook is enabled!");
    }

    private void registerEvents(){
        FileConfiguration config = this.getConfig();
        String url = config.getString("webhookURL");


        String permission = config.getString("permission");

        Color color = null;
        String hexColor = config.getString("color");

        String format = config.getString("format");

        boolean titleEnable = config.getBoolean("title.enable");
        String titleText = config.getString("title.text");
        if (!titleEnable)
            titleText = null;


        boolean reasonEnable = config.getBoolean("fields.reason.enable");
        String reasonTitle = config.getString("fields.reason.title");
        String reasonField = config.getString("fields.reason.field");
        if (!reasonEnable)
            reasonTitle = null;

        boolean durationEnable = config.getBoolean("fields.duration.enable");
        String durationTitle = config.getString("fields.duration.title");
        String durationField = config.getString("fields.duration.field");
        if (!durationEnable)
            durationTitle = null;

        boolean cellEnable = config.getBoolean("fields.cell.enable");
        String cellTitle = config.getString("fields.cell.title");
        String cellField = config.getString("fields.cell.field");
        if (!cellEnable)
            cellTitle = null;

        boolean lockedEnable = config.getBoolean("fields.lockedBy.enable");
        String lockedTitle = config.getString("fields.lockedBy.title");
        String lockedField = config.getString("fields.lockedBy.field");
        if (!lockedEnable)
            lockedTitle = null;

        boolean timestampEnable = config.getBoolean("timeStamp.enable");
        String timestampFormat = config.getString("timeStamp.format");

        String description = config.getString("description.text");
        if (Objects.equals(format, "FIELDS"))
            description = null;
        else if (!Objects.equals(format, "DESCRIPTION"))
            getLogger().severe("Invalid message format! Use FIELDS or DESCRIPTION! Used: " + format);

        try {
            assert hexColor != null;
            color = Color.decode(hexColor);
        } catch (NumberFormatException e) {
            getLogger().severe("Invalid color format, use hex format, e.g. #FF5733");
        }

        CommandListener listener = new CommandListener(this, url , format, titleText, reasonTitle, durationTitle, cellTitle, lockedTitle, reasonField, durationField, cellField, lockedField, timestampEnable, timestampFormat, color, description, permission);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommands(){
        Objects.requireNonNull(getCommand("jwreload")).setExecutor(new ReloadCommand(this));
    }

    public void reload(){
        reloadConfig();

        HandlerList.unregisterAll(this);

        registerEvents();
    }
}
