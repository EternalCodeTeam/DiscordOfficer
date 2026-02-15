package com.eternalcode.discordapp.feature.review.command.child;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;

import java.util.List;

public class ForumTagsIdChild extends SlashCommand {

    private final AppConfig appConfig;

    public ForumTagsIdChild(AppConfig appConfig) {
        this.name = "get-forum-tags-id";
        this.help = "Get Review Forum Tags Id";

        this.userPermissions = new Permission[]{ Permission.ADMINISTRATOR };

        this.appConfig = appConfig;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        long reviewForumId = this.appConfig.reviewSystem.reviewForumId;
        ForumChannel forumChannel = event.getJDA().getForumChannelById(reviewForumId);
        if (forumChannel == null) {
            event.reply("Forum channel not found for configured id: " + reviewForumId)
                .setEphemeral(true)
                .queue();
            return;
        }

        List<ForumTag> forumTags = forumChannel.getAvailableTags();

        event.reply("Forum Tags Id: " + forumTags).setEphemeral(true).queue();
    }
}

