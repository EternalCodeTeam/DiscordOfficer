package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GitHubReviewService {

    private final DiscordAppConfig discordAppConfig;
    private final JDA jda;
    private final Map<String, Long> githubDiscordMap;

    public GitHubReviewService(DiscordAppConfig discordAppConfig, JDA jda) {
        this.discordAppConfig = discordAppConfig;
        this.jda = jda;

        this.githubDiscordMap = this.discordAppConfig.reviewSystem.reviewers;
    }

    public boolean checkPullRequestTitle(String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);
    }

    public long createForumPostWithPRTitleAndMention(Guild guild, String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(1090383282744590396L);

        MessageCreateData createData = MessageCreateData.fromContent(url);
        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData).complete().getThreadChannel().getIdLong();
    }

    public void removeForumPostWithPRTitleAndMention(Guild guild, String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(1090383282744590396L);

        // TODO: remove forum post
    }


    public void mentionReviewers(String url, long messageId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(GitHubReviewUtil.getGitHubPullRequestApiUrl(url), this.discordAppConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();
        for (String reviewer : assignedReviewers) {
            Long discordId = this.githubDiscordMap.get(reviewer);

            if (discordId != null) {
                User user = this.jda.getUserById(discordId);

                if (user != null) {
                    reviewersMention.append(user.getAsMention()).append(" ");
                }
            }
        }

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention, url);

        ThreadChannel threadChannel = this.jda.getThreadChannelById(messageId);
        threadChannel.sendMessage(message).queue();
    }


    public void automaticCreatePullRequests() throws IOException {
        List<String> pullRequests = GitHubReviewUtil.getPullRequests("EternalCodeTeam", List.of("vLuckyyy", "Hyd3r1"), List.of("DiscordOfficer", "EternalCore"), this.discordAppConfig.githubToken);

        for (String pullRequest : pullRequests) {

            String gitHubPullRequestApiUrl = GitHubReviewUtil.getGitHubPullRequestApiUrl(pullRequest);

            if (GitHubReviewUtil.isPullRequestTitleValid(gitHubPullRequestApiUrl)) {
                System.out.println(pullRequest);
            }

/*            String firstMessageInForumPost = getFirstMessageInForumPost(this.jda);*/

            // firstMessageInForumPost != null && !firstMessageInForumPost.contains(pullRequest)
/*            if (firstMessageInForumPost != null && firstMessageInForumPost.contains(pullRequest)) { // <- check if pull request is already in forum post
                // Is for checking if pull request is closed, beacuse will not return the repository in the api if PR is closed
                // TODO: delete all forum post if pull request is closed

                continue;
            }*/

            Guild guild = this.jda.getGuildById(this.discordAppConfig.guildId);
            long forumPostWithPRTitleAndMention = this.createForumPostWithPRTitleAndMention(guild, pullRequest);
            this.mentionReviewers(pullRequest, forumPostWithPRTitleAndMention);
        }
    }

/*    String getFirstMessageInForumPost(JDA jda) {
        Guild guild = jda.getGuildById(this.discordAppConfig.guildId);

        Optional<ForumChannel> firstForumChannel = guild.getForumChannels().stream().findFirst();
        if (firstForumChannel.isEmpty()) {
            return null;
        }

        Optional<ThreadChannel> firstThreadChannel = firstForumChannel.get().getThreadChannels().stream().findFirst();
        if (firstThreadChannel.isEmpty()) {
            return null;
        }

        List<Message> messages = firstThreadChannel.get().getIterableHistory().stream().limit(1).toList();
        if (messages.isEmpty()) {
            return null;
        }
        Message firstMessage = messages.get(0);

        return firstMessage.getContentRaw();
    }*/

}
