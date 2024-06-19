import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;

public class CommandListener implements Listener {
    private final FileConfiguration config;
    private final String webhookUrl;
    private final String permission;
    private final String format;
    private final String title;
    private Boolean hasTitle = false;
    private final String reasonTitle;
    private final String reasonField;
    private Boolean hasReason = false;
    private final String durationTitle;
    private final String durationField;
    private Boolean hasDuration = false;
    private final String description;
    private Boolean hasDescription = false;
    private final Color color;
    private final Boolean hasTimestamp;
    private final String timestampFormat;
    private Boolean hasLockedBy = false;
    private final String lockedByTitle;
    private final String lockedByField;
    private Boolean hasCell = false;
    private final String cellTitle;
    private final String cellField;

    // Object constructor with all variables from the config.yml
    public CommandListener(JavaPlugin plugin, String webhookUrl, String format, String title, String reasonTitle, String durationTitle, String cellTitle, String lockedByTitle, String reasonField, String durationField, String cellField, String lockedByField, Boolean hasTimestamp, String timestampFormat, Color color, String description, String permission) {

        this.config = plugin.getConfig();
        this.webhookUrl = webhookUrl;
        this.color = color;
        this.format = format;
        this.hasTimestamp = hasTimestamp;
        this.timestampFormat = timestampFormat;

        // If the title is null, means that this section
        // is disabled in the config.yml
        if (lockedByTitle != null) this.hasLockedBy = true;
        this.lockedByTitle = lockedByTitle;
        this.lockedByField = lockedByField;

        // If the title is null, means that this section
        // is disabled in the config.yml
        if (cellTitle != null) this.hasCell = true;
        this.cellField = cellField;
        this.cellTitle = cellTitle;

        // If the title is null, means that this section
        // is disabled in the config.yml
        if (title != null) this.hasTitle = true;
        this.title = config.getString("title.text");

        // If the title is null, means that this section
        // is disabled in the config.yml
        if (reasonTitle != null) this.hasReason = true;
        this.reasonTitle = reasonTitle;
        this.reasonField = reasonField;

        // If the title is null, means that this section
        // is disabled in the config.yml
        if (durationTitle != null) this.hasDuration = true;
        this.durationTitle = durationTitle;
        this.durationField = durationField;

        // If the description is null, means that the
        // format in the config.yml is set to FIELDS
        if (description != null) this.hasDescription = true;
        this.description = description;

        this.permission = permission;
    }

    // Listens from the command executed from a player
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();

        // Checking if the command is the command from the config.yml
        if (message.startsWith("/" + config.getString("command"))) {

            // Checking if the player has the permission from the config.yml
            if (Objects.equals(permission, " ") || event.getPlayer().hasPermission(permission))
                handleCommand(event.getPlayer(), message);
        }
    }

    // Listens from the command executed from the console
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String message = event.getCommand();

        // Checking if the command is the command from the config.yml
        if (message.startsWith(Objects.requireNonNull(config.getString("command"))))
            handleCommand(event.getSender(), message);
    }

    private void handleCommand(CommandSender sender, String message) {
        String[] args = message.split(" ");

        // Checking if the command was valid
        if (args.length <= 3) {
            return;
        }

        // Checking if a player executed the command or the console
        // If it is the console, the variable is left null
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;

        // Getting all the arguments from the command
        String nickname = args[1];
        String duration = convertDuration(args[2]);
        String cell = args[3];

        // Setting the reason to the default from config.yml
        String reason = config.getString("no-reason-specified");

        // Checking if the command has the reason in its args
        // and changes the reason
        if (args.length > 4) reason = String.join(" ", args).split("r:", 2)[1].trim();

        // Checks if the reason from the command is empty and sets the default from config.yml
        if (Objects.equals(reason, "")) reason = config.getString("no-reason-specified");

        // Creating the webhook and the embed object
        DiscordWebhook wh = new DiscordWebhook(webhookUrl);
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        // Map with all placeholders and their variables
        // Used for replacing the placeholder with its value
        Map<String, String> values = new HashMap<>();
        values.put("nickname", nickname);
        values.put("reason", reason);
        values.put("cell", cell);
        values.put("duration", duration);

        // If the player variable is null it means that the
        // command was executed from the console
        values.put("lockedBy", (player != null) ? player.getName() : "Console");

        // Initializing the variables which will have placeholders replaced
        String formattedTitle;
        String formattedReasonTitle;
        String formattedReasonField;
        String formattedDurationTitle;
        String formattedDurationField;
        String formattedCellTitle;
        String formattedCellField;
        String formattedLockedByTitle;
        String formattedLockedByField;
        String formattedDescription;

        // Sets the embed's title if it is enabled in the config.yml
        if (hasTitle) {
            formattedTitle = replacePlaceholders(title, values);
            embed.setTitle(formattedTitle);
        }

        // Changes the embed depending on the config.yml format
        if (format.equals("FIELDS")) {

            // Adds reason if it is enabled in the config.yml
            if (hasReason) {
                formattedReasonField = replacePlaceholders(reasonField, values);
                formattedReasonTitle = replacePlaceholders(reasonTitle, values);
                embed.addField(formattedReasonTitle, formattedReasonField, false);
            }

            // Adds jail duration if it is enabled in the config.yml
            if (hasDuration) {
                formattedDurationField = replacePlaceholders(durationField, values);
                formattedDurationTitle = replacePlaceholders(durationTitle, values);
                embed.addField(formattedDurationTitle, formattedDurationField, false);
            }

            // Adds cell number if it is enabled in the config.yml
            if (hasCell) {
                formattedCellField = replacePlaceholders(cellField, values);
                formattedCellTitle = replacePlaceholders(cellTitle, values);
                embed.addField(formattedCellTitle, formattedCellField, false);
            }

            // Adds the nickname of the player who executed the command if it is enabled in the config.yml
            if (hasLockedBy) {
                formattedLockedByField = replacePlaceholders(lockedByField, values);
                formattedLockedByTitle = replacePlaceholders(lockedByTitle, values);
                embed.addField(formattedLockedByTitle, formattedLockedByField, false);
            }

        } else if (format.equals("DESCRIPTION")) {

            // Sets description if the format is set to "DESCRIPTION" in the config.yml
            if (hasDescription) {
                formattedDescription = replacePlaceholders(description, values);
                embed.setDescription(formattedDescription);
            }
        }

        // Sets embed color
        embed.setColor(color);

        // Adds timestamp if it is enabled in the config.yml
        if (hasTimestamp) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timestampFormat).withZone(ZoneId.systemDefault());
            String footerText = formatter.format(Instant.now());

            embed.setFooter(footerText, null);
        }

        // Adding embed to the webhook content
        wh.addEmbed(embed);

        // Executing the webhook
        try {
            wh.execute();
        } catch (MalformedURLException e) {
            sender.sendMessage("Invalid webhook URL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Replacing d, h, m and s with the words from the config.yml
    // The " " at the end is for handling 3d1h for example to be:
    // 3 days 1 hour instead of 3 days1 hour
    public String convertDuration(String duration) {
        return duration.replaceAll("(?<!\\d)1d", "1 " + config.getString("timeStamp.day" + " ")).replaceAll("(?<!\\d)1h", "1 " + config.getString("timeStamp.hour" + " ")).replaceAll("(?<!\\d)1m", "1 " + config.getString("timeStamp.minute" + " ")).replaceAll("(?<!\\d)1s", "1 " + config.getString("timeStamp.second" + " ")).replaceAll("d", " " + config.getString("timeStamp.days" + " ")).replaceAll("h", " " + config.getString("timeStamp.hours" + " ")).replaceAll("m", " " + config.getString("timeStamp.minutes" + " ")).replaceAll("s", " " + config.getString("timeStamp.seconds" + " "));
    }


    //    Replacing the placeholders like %nickname% with their values
    public static String replacePlaceholders(String template, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return template;
    }

}

