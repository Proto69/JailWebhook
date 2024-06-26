import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.awt.*;
import java.util.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UnJailCommandListener implements Listener {
    private final FileConfiguration config;
    private final JailWebhook plugin;
    private final String permission;
    private String command;

    // Object constructor with all variables from the config.yml
    public UnJailCommandListener(JailWebhook plugin, String permission) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.command = config.getString("commands.unjail");

        this.permission = permission;
    }

    // Listens from the command executed from a player
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();

        // Checking if the command is the command from the config.yml
        if (message.startsWith("/" + command)) {

            // Checking if the player has the permission from the config.yml
            if (Objects.equals(permission, " ") || event.getPlayer().hasPermission(permission)){
                this.command = this.command.replace("/", "");
                handleCommand(event.getPlayer(), message.replace("/", ""));
            }
        }
    }

    // Listens from the command executed from the console
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String message = event.getCommand();

        // Checking if the command is the command from the config.yml
        if (message.startsWith(Objects.requireNonNull(command)))
            handleCommand(event.getSender(), message);
    }

    private void handleCommand(CommandSender sender, String message) {
        String[] args = message.split(" ");

        // Checking if the command was valid
        if (args.length <= 1) {
            return;
        }

        // Checking if a player executed the command or the console
        // If it is the console, the variable is left null
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;

        // Splitting the command into an array by " "
        String[] commandWords = command.split(" ");

        // Get the last word from the command
        String lastWord = commandWords[commandWords.length - 1];

        // Getting the index of the last word
        int index = getIndexOfLastWord(args, lastWord);

        // Getting all the arguments from the command
        String nickname = args[index + 1];

        try {
            plugin.getDatabaseManager().updateUnJailData((player != null) ? player.getName() : "Console", nickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Gets the index of the last word from the command
    public static int getIndexOfLastWord(String[] wordsArray, String lastWord) {
        for (int i = 0; i < wordsArray.length; i++) {
            if (wordsArray[i].equals(lastWord)) {
                return i;
            }
        }
        return -1; // Return -1 if the word is not found
    }
}

