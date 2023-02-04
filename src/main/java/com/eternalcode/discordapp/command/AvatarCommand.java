package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collections;

public class AvatarCommand extends SlashCommand {

    public AvatarCommand() {
        this.name = "avatar";
        this.help = "Shows the avatar of a user";

        this.options = Collections.singletonList(new OptionData(OptionType.USER, "user", "select the user").setRequired(false));
    }

    @Override
    public void execute(SlashCommandEvent event) {
        User user = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getUser();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("🖼 | " + user.getName() + "'s avatar")
                .setImage(user.getEffectiveAvatarUrl() + "?size=2048")
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embed)
                .setEphemeral(true)
                .queue();
    }
}
