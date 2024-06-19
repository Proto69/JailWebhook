import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final JailWebhook plugin;

    public ReloadCommand(JailWebhook plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("jwreload")) {
            if (sender.hasPermission("jailwebhook.reload")) {
                long before = System.currentTimeMillis();
                plugin.reload();
                long after = System.currentTimeMillis();
                sender.sendMessage("Successfully reloaded JailWebhook in " + (after - before) + "ms!");
            }
            return true;
        }
        return false;
    }
}
