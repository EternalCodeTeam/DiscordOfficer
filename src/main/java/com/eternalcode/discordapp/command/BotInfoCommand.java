package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;

public class BotInfoCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public BotInfoCommand(DiscordAppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;

        this.name = "botinfo";
        this.help = "Shows information about the bot";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        MessageEmbed build = new EmbedBuilder()
                .setTitle("ℹ️ | Bot Information")
                .setThumbnail(event.getGuild().getIconUrl())
                .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                .addField("🏰 Guilds", String.valueOf(event.getJDA().getGuilds().size()), false)
                .addField("👥 Users", String.valueOf(event.getJDA().getUsers().size()), false)
                .addField("🔖 Channels", String.valueOf(event.getJDA().getTextChannels().size()), false)
                .addField("💾 OS", System.getProperty("os.name"), false)
                .addField("🍺 Java", System.getProperty("java.version"), false)
                .addField("🏓 Gateway Ping", String.valueOf(event.getJDA().getGatewayPing()), false)
                .addField("🛫 Rest Ping", String.valueOf(event.getJDA().getRestPing().complete()), false)
                .setTimestamp(Instant.now())
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}