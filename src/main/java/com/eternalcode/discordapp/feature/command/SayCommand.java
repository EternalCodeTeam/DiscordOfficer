package com.eternalcode.discordapp.feature.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class SayCommand extends SlashCommand {

    public SayCommand() {
        this.name = "say";
        this.help = "Says something";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };
        this.options = List.of(
                new OptionData(OptionType.STRING, "message", "The message to say")
                        .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String message = event.getOption("message").getAsString();

        event.getChannel().sendMessage(message).queue();
        event.reply("Sent the message").setEphemeral(true).queue();
    }
}

