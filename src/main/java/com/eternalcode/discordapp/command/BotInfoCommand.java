package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
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
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();

        MessageEmbed build = new EmbedBuilder()
                .setTitle("ℹ️ | Bot Information")
                .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                .setThumbnail(guild.getIconUrl())
                .addField("🏰 Guilds", String.valueOf(jda.getGuilds().size()), false)
                .addField("👥 Users", String.valueOf(jda.getUsers().size()), false)
                .addField("🔖 Channels", String.valueOf(jda.getTextChannels().size()), false)
                .addField("💾 OS", System.getProperty("os.name"), false)
                .addField("🍺 Java", System.getProperty("java.version"), false)
                .addField("🏓 Gateway Ping", String.valueOf(jda.getGatewayPing()), false)
                .addField("🛫 Rest Ping", String.valueOf(jda.getRestPing().complete()), false)
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}