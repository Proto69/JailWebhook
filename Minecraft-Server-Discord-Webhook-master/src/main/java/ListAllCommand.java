import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListAllCommand implements CommandExecutor {
    private final JailWebhook plugin;

    public ListAllCommand(JailWebhook plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checking the sender for required permission
        if (sender.hasPermission("jailwebhook.all")) {
            String columnName = " ";
            String value = " ";
            if (args.length > 2) {
                columnName = args[1];
                value = args[2];
                if (Objects.equals(columnName.toLowerCase(), "active")){
                    value = Objects.equals(value.toLowerCase(), "true") ? "1" : "0";
                }
            }
            List<Map<String, Object>> results = null;
            try {
                results = plugin.getDatabaseManager().getData(columnName, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (results != null && !results.isEmpty()){
                printFormattedResults(results, sender, columnName);
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "No data in the database!");
        }
        return true;
    }

    public void printFormattedResults(List<Map<String, Object>> results, CommandSender sender, String columnName) {


        Map<String, Object> row = results.get(0);

            String prisonerName = (String) row.get("prisoner_name");
            String jailedBy = (String) row.get("jailed_by");
            String active = row.get("active").toString();

            if (columnName.equalsIgnoreCase("active")) {
                sender.sendMessage(ChatColor.GREEN + "Fetching all jails with active status: " + ChatColor.AQUA + ChatColor.BOLD + active);
                PrintList(results, sender);
                return;
            }

            if (columnName.equalsIgnoreCase("prisoner_name")) {
                sender.sendMessage(ChatColor.GREEN + "Fetching all jails for prisoner: " + ChatColor.AQUA + ChatColor.BOLD + prisonerName);
                PrintList(results, sender);
                return;
            }

            if (columnName.equalsIgnoreCase("jailed_by")) {
                sender.sendMessage(ChatColor.GREEN + "Fetching all jails by: " + ChatColor.AQUA + ChatColor.BOLD + jailedBy);
                PrintList(results, sender);
                return;
            }

            if (columnName.equalsIgnoreCase(" ")){
                sender.sendMessage(ChatColor.GREEN + "Fetching all jails: ");
                PrintList(results, sender);
                return;
            }

            sender.sendMessage(ChatColor.RED + "Invalid filter!");

    }

    private void PrintList(List<Map<String, Object>> results, CommandSender sender){
        if (results.isEmpty()){
            sender.sendMessage(ChatColor.GREEN + "No jails!");
            return;
        }
        for (Map<String, Object> row : results) {
            String prisonerName = (String) row.get("prisoner_name");
            String reason = row.get("reason") != null ? (String) row.get("reason") : plugin.getConfig().getString("no-reason-specified");
            String jailedBy = (String) row.get("jailed_by");
            String jailedAt = row.get("jailed_at").toString().replace("T", " ");
            String active = row.get("active").toString();

            String formattedRow = String.format("" + ChatColor.GOLD + ChatColor.BOLD + "Username: " + ChatColor.WHITE + "%s \n" + ChatColor.GOLD + ChatColor.BOLD + "Reason: " + ChatColor.WHITE + "%s \n" + ChatColor.GOLD + ChatColor.BOLD + "Jailed By: " + ChatColor.WHITE + "%s\n" + ChatColor.GOLD + ChatColor.BOLD + "Jailed At: " + ChatColor.WHITE + "%s\n" + ChatColor.GOLD + ChatColor.BOLD + "Active: " + ChatColor.WHITE + "%s",
                    prisonerName, reason, jailedBy, jailedAt, active);

            sender.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "---------------");
            sender.sendMessage(formattedRow);
            sender.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "---------------");
        }
    }
}
