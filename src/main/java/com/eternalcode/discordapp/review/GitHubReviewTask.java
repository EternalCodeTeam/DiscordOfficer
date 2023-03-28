package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.TimerTask;

public class GitHubReviewTask extends TimerTask {

    private final JDA jda;
    private final OkHttpClient httpClient;
    private final DiscordAppConfig discordAppConfig;

    public GitHubReviewTask(JDA jda, OkHttpClient httpClient, DiscordAppConfig discordAppConfig) {
        this.jda = jda;
        this.httpClient = httpClient;
        this.discordAppConfig = discordAppConfig;
    }

    @Override
    public void run() {
        Guild guild = this.jda.getGuildById(this.discordAppConfig.guildId);

        for (ForumChannel channel : guild.getForumChannels()) {
            channel.getThreadChannels().forEach(threadChannel -> threadChannel.getIterableHistory()
                    .reverse()
                    .limit(1)
                    .queue(messages -> {
                        Message message = messages.get(0);

                        try {
                            String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(message.getContentRaw(), this.httpClient, this.discordAppConfig.githubToken);

                            boolean pullRequestTitleValid = GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);

                            if (!pullRequestTitleValid) {
                                threadChannel.delete().queue();
                            }
                        }
                        catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    })
            );
        }
    }

}
