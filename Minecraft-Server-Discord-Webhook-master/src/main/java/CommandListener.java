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

    public CommandListener(JavaPlugin plugin, String webhookUrl, String format, String title, String reasonTitle, String durationTitle, String cellTitle, String lockedByTitle, String reasonField, String durationField, String cellField, String lockedByField, Boolean hasTimestamp, String timestampFormat, Color color, String description, String permission) {

        this.config = plugin.getConfig();
        this.webhookUrl = webhookUrl;
        this.color = color;
        this.format = format;
        this.hasTimestamp = hasTimestamp;
        this.timestampFormat = timestampFormat;

        if (lockedByTitle != null)
            this.hasLockedBy = true;
        this.lockedByTitle = lockedByTitle;
        this.lockedByField = lockedByField;

        if (cellTitle != null)
            this.hasCell = true;
        this.cellField = cellField;
        this.cellTitle = cellTitle;

        this.title = config.getString("title.text");
        if (title != null)
            this.hasTitle = true;

        this.reasonTitle = reasonTitle;
        this.reasonField = reasonField;
        if (reasonTitle != null)
            this.hasReason = true;

        this.durationTitle = durationTitle;
        this.durationField = durationField;
        if (durationTitle != null)
            this.hasDuration = true;

        this.description = description;
        if (description != null)
            this.hasDescription = true;

        this.permission = permission;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.startsWith("/" + config.getString("command"))) {
            if (Objects.equals(permission, " ") || event.getPlayer().hasPermission(permission))
                handleCommand(event.getPlayer(), message);
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String message = event.getCommand();
        if (message.startsWith(Objects.requireNonNull(config.getString("command"))))
            handleCommand(event.getSender(), message);
    }

    private void handleCommand(CommandSender sender, String message) {
        String[] args = message.split(" ");

        Player player = null;

        if (sender instanceof Player)
            player = (Player) sender;

        if (args.length <= 3) {
            return;
        }

        String nickname = args[1];
        String duration = convertDuration(args[2]);
        String cell = args[3];

        String reason = config.getString("no-reason-specified");
        if (args.length > 4)
             reason = String.join(" ", args).split("r:", 2)[1].trim();
        if (Objects.equals(reason, ""))
            reason = config.getString("no-reason-specified");

        DiscordWebhook wh = new DiscordWebhook(webhookUrl);
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        Map<String, String> values = new HashMap<>();
        values.put("nickname", nickname);
        values.put("reason", reason);
        values.put("cell", cell);
        values.put("duration", duration);
        values.put("lockedBy", (player != null) ? player.getName() : "Console");

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

        if (hasTitle) {
            formattedTitle = replacePlaceholders(title, values);
            embed.setTitle(formattedTitle);
        }
        if (format.equals("FIELDS")) {
            if (hasReason) {
                formattedReasonField = replacePlaceholders(reasonField, values);
                formattedReasonTitle = replacePlaceholders(reasonTitle, values);
                embed.addField(formattedReasonTitle, formattedReasonField, false);
            }
            if (hasDuration) {
                formattedDurationField = replacePlaceholders(durationField, values);
                formattedDurationTitle = replacePlaceholders(durationTitle, values);
                embed.addField(formattedDurationTitle, formattedDurationField, false);
            }
            if (hasCell) {
                formattedCellField = replacePlaceholders(cellField, values);
                formattedCellTitle = replacePlaceholders(cellTitle, values);
                embed.addField(formattedCellTitle, formattedCellField, false);
            }
            if (hasLockedBy) {
                formattedLockedByField = replacePlaceholders(lockedByField, values);
                formattedLockedByTitle = replacePlaceholders(lockedByTitle, values);
                embed.addField(formattedLockedByTitle, formattedLockedByField, false);
            }
        } else if (format.equals("DESCRIPTION")) {
            if (hasDescription) {
                formattedDescription = replacePlaceholders(description, values);
                embed.setDescription(formattedDescription);
            }
        }

        embed.setColor(color);

        if (hasTimestamp) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timestampFormat)
                    .withZone(ZoneId.systemDefault());
            String footerText = formatter.format(Instant.now());

            embed.setFooter(footerText, null);
        }


        wh.addEmbed(embed);
        try {
            wh.execute();
        } catch (MalformedURLException e) {
            sender.sendMessage("Invalid webhook URL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String convertDuration(String duration) {
        return duration
                .replaceAll("(?<!\\d)1d", "1 " + config.getString("timeStamp.day"))
                .replaceAll("(?<!\\d)1h", "1 " + config.getString("timeStamp.hour"))
                .replaceAll("(?<!\\d)1m", "1 " + config.getString("timeStamp.minute"))
                .replaceAll("(?<!\\d)1s", "1 " + config.getString("timeStamp.second"))
                .replaceAll("d", " " + config.getString("timeStamp.days"))
                .replaceAll("h", " " + config.getString("timeStamp.hours"))
                .replaceAll("m", " " + config.getString("timeStamp.minutes"))
                .replaceAll("s", " " + config.getString("timeStamp.seconds"));
    }

    public static String replacePlaceholders(String template, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return template;
    }

}

