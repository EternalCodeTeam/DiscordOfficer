package com.eternalcode.discordapp.review.command.child;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
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

        List<ForumTag> forumTags = event.getJDA().getForumChannelById(reviewForumId).getAvailableTags();

        event.reply("Forum Tags Id: " + forumTags).setEphemeral(true).queue();
    }
}
