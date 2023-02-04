package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

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
                .setImage(event.getGuild().getIconUrl())
                .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                .addField("Guilds", String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField("Users", String.valueOf(event.getJDA().getUsers().size()), true)
                .addField("Gateway Ping", String.valueOf(event.getJDA().getGatewayPing()), true)
                .addField("Rest Ping", String.valueOf(event.getJDA().getRestPing().complete()), true)
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }

}