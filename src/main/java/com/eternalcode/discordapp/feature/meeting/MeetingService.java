package com.eternalcode.discordapp.feature.meeting;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeetingService {

    public static final String DEFAULT_SEPARATOR = "-";
    public static final String DELIMITER = ", ";
    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingService.class);
    private static final Color MEETING_ACCENT_COLOR = Color.decode("#3B82F6");
    private final MeetingPollRepository pollRepository;
    private final MeetingVoteRepository voteRepository;

    public MeetingService(
        MeetingPollRepository pollRepository,
        MeetingVoteRepository voteRepository
    ) {
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
    }

    private static String trimTo(String string) {
        final int MAX_COMPONENT_TEXT_LENGTH = 1024;
        if (string.length() <= MAX_COMPONENT_TEXT_LENGTH) {
            return string;
        }
        return string.substring(0, Math.max(0, MAX_COMPONENT_TEXT_LENGTH - 3)) + "...";
    }

    public void createMeeting(MessageChannel channel, long guildId, String topic, Instant meetingAt) {
        Container container = this.buildMeetingContainer(topic, meetingAt, 0, 0, 0, List.of(), List.of(), List.of());

        channel.sendMessage(new MessageCreateBuilder()
                .setComponents(container)
                .useComponentsV2()
                .build())
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
                    Container updated = this.buildMeetingContainer(
                        poll.getTopic(),
                        Instant.ofEpochSecond(poll.getMeetingAt()),
                        yesMentions.size(),
                        noMentions.size(),
                        maybeMentions.size(),
                        yesMentions,
                        noMentions,
                        maybeMentions
                    );

                    updater.editMessage(new MessageEditBuilder()
                        .setComponents(updated)
                        .useComponentsV2()
                        .build());
                    updater.acknowledgeEphemeral("Głos zaktualizowany. Dzięki!");
                    return null;
                });
    }

    private Container buildMeetingContainer(
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
            ? DEFAULT_SEPARATOR
            : trimTo(String.join(DELIMITER, yesMentions));

        String noList = noMentions.isEmpty()
            ? DEFAULT_SEPARATOR
            : trimTo(String.join(DELIMITER, noMentions));

        String maybeList = maybeMentions.isEmpty()
            ? DEFAULT_SEPARATOR
            : trimTo(String.join(DELIMITER, maybeMentions));

        long epoch = meetingAt.getEpochSecond();
        String when = "<t:" + epoch + ":F> (" + "<t:" + epoch + ":R>)";

        return Container.of(
            TextDisplay.of("## 📅 Spotkaniomierz: " + topic),
            TextDisplay.of("Zagłosuj poniżej, aby potwierdzić obecność."),
            Separator.createDivider(Separator.Spacing.SMALL),
            TextDisplay.of("**🗓 Kiedy:** " + when),
            Section.of(
                Button.success("meeting_yes", "✅ Będę"),
                TextDisplay.of("**Obecnych:** " + yes),
                TextDisplay.of("Lista: " + yesList)
            ),
            Section.of(
                Button.danger("meeting_no", "❌ Nie będę"),
                TextDisplay.of("**Nieobecnych:** " + no),
                TextDisplay.of("Lista: " + noList)
            ),
            Section.of(
                Button.secondary("meeting_maybe", "🤷 Jeszcze nie wiem"),
                TextDisplay.of("**Niezdecydowanych:** " + maybe),
                TextDisplay.of("Lista: " + maybeList)
            ),
            Separator.createDivider(Separator.Spacing.SMALL),
            TextDisplay.of("-# Ostatnia aktualizacja: <t:" + Instant.now().getEpochSecond() + ":T>")
        ).withAccentColor(MEETING_ACCENT_COLOR);
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
                    channel.retrieveMessageById(poll.getMessageId()).queue(
                        message -> message.editMessageComponents(message.getComponentTree().asDisabled())
                            .useComponentsV2(true)
                            .queue(
                                success -> {
                                    this.cleanupPollData(poll);
                                    LOGGER.debug(
                                        "Locked expired meeting message and cleaned DB for messageId={}",
                                        poll.getMessageId());
                                },
                                failure -> {
                                    LOGGER.warn(
                                        "Failed to lock meeting messageId={} in channelId={}. Cleaning DB anyway.",
                                        poll.getMessageId(), poll.getChannelId(), failure);
                                    this.cleanupPollData(poll);
                                }
                            ),
                        failure -> {
                            LOGGER.warn(
                                "Failed to retrieve meeting messageId={} in channelId={}. Cleaning DB anyway.",
                                poll.getMessageId(), poll.getChannelId(), failure);
                            this.cleanupPollData(poll);
                        }
                    );
                }
                else {
                    LOGGER.info(
                        "Channel not found for expired meeting messageId={}, channelId={}. Cleaning DB.",
                        poll.getMessageId(), poll.getChannelId());
                    this.cleanupPollData(poll);
                }
            }
        }).exceptionally(ex -> {
            LOGGER.error("Failed to fetch expired polls", ex);
            return null;
        });
    }

    private void cleanupPollData(MeetingPollWrapper poll) {
        this.voteRepository.deleteByMessageId(poll.getMessageId());
        this.pollRepository.deleteByMessageId(poll.getMessageId());
    }

    public interface MeetingButtonUpdater {
        void editMessage(MessageEditData newData);
        void replyEphemeral(String content);
        void acknowledgeEphemeral(String content);
    }
}
