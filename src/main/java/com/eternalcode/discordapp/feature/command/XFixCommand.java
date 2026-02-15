package com.eternalcode.discordapp.feature.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class XFixCommand extends SlashCommand {

    public XFixCommand() {
        this.name = "x";
        this.help = "Converts Twitter/X links to fxtwitter.com format";

        this.options = List.of(
            new OptionData(OptionType.STRING, "link", "Twitter/X link to convert")
                .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String inputLink = event.getOption("link").getAsString().trim();

        String convertedLink = switch (inputLink) {
            case String link when link.contains("twitter.com") -> link.replace("twitter.com", "fxtwitter.com");
            case String link when link.contains("x.com") -> link.replace("x.com", "fxtwitter.com");
            default -> inputLink;
        };

        event.reply(convertedLink)
            .setEphemeral(false)
            .queue();
    }
}

