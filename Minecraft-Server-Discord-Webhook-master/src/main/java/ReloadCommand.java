import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class ReloadCommand implements CommandExecutor {
    private final JailWebhook plugin;

    public ReloadCommand(JailWebhook plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checking the sender for required permission
        if (sender.hasPermission("jailwebhook.reload")) {
            // Getting the time before the reload
            long before = System.currentTimeMillis();
            // Reloading the plugin
            try {
                plugin.reload();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // Getting the time after the reload
            long after = System.currentTimeMillis();
            // Sending message with the total time for the reload
            sender.sendMessage("Successfully reloaded JailWebhook in " + (after - before) + "ms!");
        }
        return true;
    }
}
