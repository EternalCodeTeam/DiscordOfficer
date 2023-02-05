package com.eternalcode.discordapp.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class EmbedCommand extends SlashCommand {

    public EmbedCommand() {
        this.name = "embed";
        this.help = "Sends an customized embed";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.options = List.of(
                new OptionData(OptionType.STRING, "title", "The title of the embed")
                        .setRequired(true),
                new OptionData(OptionType.STRING, "description", "The description of the embed")
                        .setRequired(true),
                new OptionData(OptionType.STRING, "color", "The color of the embed")
                        .setRequired(false),
                new OptionData(OptionType.STRING, "footer", "The footer of the embed")
                        .setRequired(false),
                new OptionData(OptionType.STRING, "author", "The thumbnail of the embed")
                        .setRequired(false),
                new OptionData(OptionType.STRING, "image", "The thumbnail of the embed")
                        .setRequired(false),
                new OptionData(OptionType.STRING, "thumbnail", "The thumbnail of the embed")
                        .setRequired(false),
                new OptionData(OptionType.BOOLEAN, "timestamp", "Do you want to set timestamp in the embed?")
                        .setRequired(false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String title = event.getOption("title").getAsString();
        String description = event.getOption("description").getAsString();
        String color = event.getOption("color") != null ? event.getOption("color").getAsString() : "#000000";
        String footer = event.getOption("footer") != null ? event.getOption("footer").getAsString() : null;
        String author = event.getOption("author") != null ? event.getOption("author").getAsString() : null;
        String image = event.getOption("image") != null ? event.getOption("image").getAsString() : null;
        String thumbnail = event.getOption("thumbnail") != null ? event.getOption("thumbnail").getAsString() : null;
        boolean timestamp = event.getOption("timestamp") != null && event.getOption("timestamp").getAsBoolean();

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(author)
                .setTitle(title)
                .setColor(Color.decode(color))
                .setThumbnail(thumbnail)
                .setDescription(description)
                .setImage(image)
                .setFooter(footer)
                .setTimestamp(timestamp ? Instant.now() : null)
                .build();

        event.replyEmbeds(embed)
                .setEphemeral(true)
                .queue();
    }
}