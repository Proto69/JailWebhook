import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PluginTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        Player player = null;
        if (sender instanceof Player) player = (Player) sender;

        if (command.getName().equalsIgnoreCase("jw")) {
            if (args.length == 1) {
                if (player == null){
                    suggestions.addAll(Arrays.asList("reload", "all"));
                } else {
                    if (player.hasPermission("jailwebhook.reload")) {
                        suggestions.add("reload");
                    }
                    if (player.hasPermission("jailwebhook.all")) {
                        suggestions.add("all");
                    }
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("all")) {
                    suggestions.addAll(Arrays.asList("active", "prisoner_name", "jailed_by"));
                }
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("active")) {
                    suggestions.addAll(Arrays.asList("true", "false"));
                } else if (args[1].equalsIgnoreCase("prisoner_name") || args[1].equalsIgnoreCase("jailed_by")) {
                    suggestions.addAll(getAllOnlinePlayers());
                }
            }
        }

        // Filter suggestions based on the current input
        return filterSuggestions(suggestions, args);
    }

    private List<String> filterSuggestions(List<String> suggestions, String[] args) {
        List<String> filtered = new ArrayList<>();
        String currentArg = args[args.length - 1];
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(currentArg.toLowerCase())) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }

    public static List<String> getAllOnlinePlayers() {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }
}
