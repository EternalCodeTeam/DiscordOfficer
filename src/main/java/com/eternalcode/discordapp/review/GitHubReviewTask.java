package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;

import java.util.TimerTask;

public class GitHubReviewTask extends TimerTask {

    private final JDA jda;
    private final DiscordAppConfig discordAppConfig;

    public GitHubReviewTask(JDA jda, DiscordAppConfig discordAppConfig) {
        this.jda = jda;
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
                        System.out.println(message.getContentRaw());
                    })
            );
        }
    }

}
