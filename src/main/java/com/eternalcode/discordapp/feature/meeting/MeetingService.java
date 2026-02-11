package com.eternalcode.discordapp.feature.meeting;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MeetingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingService.class);

    private final AppConfig appConfig;
    private final MeetingPollRepository pollRepository;
    private final MeetingVoteRepository voteRepository;

    public MeetingService(
        AppConfig appConfig,
        MeetingPollRepository pollRepository,
        MeetingVoteRepository voteRepository) {
        this.appConfig = appConfig;
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
    }

    private static String trimTo(String string) {
        final int EMBED_FIELD_VALUE_LIMIT = 1024;
        if (string.length() <= EMBED_FIELD_VALUE_LIMIT) {
            return string;
        }
        return string.substring(0, Math.max(0, EMBED_FIELD_VALUE_LIMIT - 3)) + "...";
    }

    public void createMeeting(MessageChannel channel, long guildId, String topic, Instant meetingAt) {
        MessageEmbed embed = this.buildMeetingEmbed(topic, meetingAt, 0, 0, 0, List.of(), List.of(), List.of());

        channel.sendMessageEmbeds(embed)
            .setComponents(
                ActionRow.of(
                    Button.secondary("meeting_yes", "✅ Będę"),
                    Button.secondary("meeting_no", "❌ Nie będę"),
                    Button.secondary("meeting_maybe", "🤷 Jeszcze nie wiem")
                )
            )
            .queue(message -> {
                MeetingPollWrapper poll = new MeetingPollWrapper(
                    message.getIdLong(),
                    guildId,
                    channel.getIdLong(),
                    topic,
                    Instant.now(),
                    meetingAt
                );
                this.pollRepository.savePoll(poll);
            });
    }

    public void handleVote(
        long messageId,
        long userId,
        MeetingStatus status,
        MeetingButtonUpdater updater
    ) {
        this.voteRepository.findUserVote(messageId, userId).thenAccept(currentOpt -> {
            if (currentOpt.isEmpty()) {
                MeetingVoteWrapper vote = new MeetingVoteWrapper(messageId, userId, status);
                this.voteRepository.saveVote(vote).thenRun(() -> this.refreshMessage(messageId, updater));
                return;
            }

            MeetingVoteWrapper current = currentOpt.get();
            MeetingStatus currentStatus = current.getStatus();

            if (currentStatus == status) {
                this.voteRepository.deleteById(current.getId()).thenRun(() -> {
                    this.refreshMessage(messageId, updater);
                    updater.replyEphemeral("Wycofano Twój głos: " + humanize(currentStatus) + ".");
                });
                return;
            }

            MeetingVoteWrapper updated = new MeetingVoteWrapper(messageId, userId, status);
            this.voteRepository.saveVote(updated).thenRun(() -> this.refreshMessage(messageId, updater));
        });
    }

    private void refreshMessage(long messageId, MeetingButtonUpdater updater) {
        this.voteRepository.findByMessageId(messageId)
            .thenCombine(
                this.pollRepository.select(messageId), (votes, pollOpt) -> {
                    if (pollOpt.isEmpty()) {
                        return null;
                    }

                    Map<MeetingStatus, List<String>> mentionsByStatus = votes.stream()
                        .collect(Collectors.groupingBy(
                            MeetingVoteWrapper::getStatus,
                            Collectors.mapping(vote -> "<@" + vote.getUserId() + ">", Collectors.toList())
                        ));

                    List<String> yesMentions = mentionsByStatus.getOrDefault(MeetingStatus.YES, List.of());
                    List<String> noMentions = mentionsByStatus.getOrDefault(MeetingStatus.NO, List.of());
                    List<String> maybeMentions = mentionsByStatus.getOrDefault(MeetingStatus.MAYBE, List.of());

                    MeetingPollWrapper poll = pollOpt.get();
                    MessageEmbed updated = this.buildMeetingEmbed(
                        poll.getTopic(),
                        Instant.ofEpochSecond(poll.getMeetingAt()),
                        yesMentions.size(),
                        noMentions.size(),
                        maybeMentions.size(),
                        yesMentions,
                        noMentions,
                        maybeMentions
                    );

                    updater.editMessage(new MessageEditBuilder().setEmbeds(updated).build());
                    updater.acknowledgeEphemeral("Głos zaktualizowany. Dzięki!");
                    return null;
                });
    }

    private MessageEmbed buildMeetingEmbed(
        String topic,
        Instant meetingAt,
        int yes,
        int no,
        int maybe,
        List<String> yesMentions,
        List<String> noMentions,
        List<String> maybeMentions
    ) {
        String yesList = yesMentions.isEmpty()
            ? "—"
            : trimTo(yesMentions.stream().collect(Collectors.joining(", ")));

        String noList = noMentions.isEmpty()
            ? "—"
            : trimTo(noMentions.stream().collect(Collectors.joining(", ")));

        String maybeList = maybeMentions.isEmpty()
            ? "—"
            : trimTo(maybeMentions.stream().collect(Collectors.joining(", ")));

        long epoch = meetingAt.getEpochSecond();
        String when = "<t:" + epoch + ":F> (" + "<t:" + epoch + ":R>)";

        return new EmbedBuilder()
            .setTitle("📅 Spotkaniomierz: " + topic)
            .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
            .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
            .setDescription("Zagłosuj poniżej, aby potwierdzić obecność.")
            .addField("🗓 Kiedy", when, false)
            .addField("✅ Obecnych", String.valueOf(yes), true)
            .addField("❌ Nieobecnych", String.valueOf(no), true)
            .addField("🤷 Niezdecydowanych", String.valueOf(maybe), true)
            .addField("Lista obecnych", yesList, false)
            .addField("Lista nieobecnych", noList, false)
            .addField("Lista niezdecydowanych", maybeList, false)
            .setTimestamp(Instant.now())
            .build();
    }

    private String humanize(MeetingStatus status) {
        return switch (status) {
            case YES -> "„✅ Będę”";
            case NO -> "„❌ Nie będę”";
            case MAYBE -> "„🤷 Jeszcze nie wiem”";
        };
    }

    public void cleanupExpired(JDA jda, Instant now) {
        Instant cutoff = now.minus(Duration.ofDays(1));

        this.pollRepository.findExpiredPolls(cutoff).thenAccept(polls -> {
            for (MeetingPollWrapper poll : polls) {
                TextChannel channel = jda.getTextChannelById(poll.getChannelId());
                if (channel != null) {
                    channel.deleteMessageById(poll.getMessageId()).queue(
                        success -> {
                            this.voteRepository.deleteByMessageId(poll.getMessageId());
                            this.pollRepository.deleteByMessageId(poll.getMessageId());
                            LOGGER.debug(
                                "Deleted expired meeting message and cleaned DB for messageId={}",
                                poll.getMessageId());
                        },
                        failure -> {
                            LOGGER.warn(
                                "Failed to delete meeting messageId={} in channelId={}. Cleaning DB anyway.",
                                poll.getMessageId(), poll.getChannelId(), failure);
                            this.voteRepository.deleteByMessageId(poll.getMessageId());
                            this.pollRepository.deleteByMessageId(poll.getMessageId());
                        }
                    );
                }
                else {
                    LOGGER.info(
                        "Channel not found for expired meeting messageId={}, channelId={}. Cleaning DB.",
                        poll.getMessageId(), poll.getChannelId());
                    this.voteRepository.deleteByMessageId(poll.getMessageId());
                    this.pollRepository.deleteByMessageId(poll.getMessageId());
                }
            }
        }).exceptionally(ex -> {
            LOGGER.error("Failed to fetch expired polls", ex);
            return null;
        });
    }

    public interface MeetingButtonUpdater {
        void editMessage(MessageEditData newData);
        void replyEphemeral(String content);
        void acknowledgeEphemeral(String content);
    }
}
