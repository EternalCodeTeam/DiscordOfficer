package com.eternalcode.discordapp.meeting;

import com.eternalcode.discordapp.config.AppConfig;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class MeetingService {

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
            .setActionRow(
                Button.primary("meeting_yes", "✅ Będę"),
                Button.danger("meeting_no", "❌ Nie będę"),
                Button.secondary("meeting_maybe", "🤷 Jeszcze nie wiem")
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

            String msg = "Masz już zaznaczoną opcję: " + humanize(currentStatus) + ".\n"
                + "Aby zmienić głos na: " + humanize(status) + ", najpierw kliknij ponownie przycisk: " + humanize(
                currentStatus) + " (to wycofa Twój aktualny głos),\n"
                + "a następnie wybierz: " + humanize(status) + ".";
            updater.replyEphemeral(msg);
        });
    }

    private void refreshMessage(long messageId, MeetingButtonUpdater updater) {
        this.voteRepository.findByMessageId(messageId).thenAccept(votes -> {
            int yes = (int) votes.stream().filter(v -> v.getStatus() == MeetingStatus.YES).count();
            int no = (int) votes.stream().filter(v -> v.getStatus() == MeetingStatus.NO).count();
            int maybe = (int) votes.stream().filter(v -> v.getStatus() == MeetingStatus.MAYBE).count();

            List<String> yesMentions = votes.stream()
                .filter(v -> v.getStatus() == MeetingStatus.YES)
                .map(v -> "<@" + v.getUserId() + ">")
                .toList();

            List<String> noMentions = votes.stream()
                .filter(v -> v.getStatus() == MeetingStatus.NO)
                .map(v -> "<@" + v.getUserId() + ">")
                .toList();

            List<String> maybeMentions = votes.stream()
                .filter(v -> v.getStatus() == MeetingStatus.MAYBE)
                .map(v -> "<@" + v.getUserId() + ">")
                .toList();

            this.pollRepository.select(messageId).thenAccept(pollOpt -> {
                String topic = pollOpt.map(MeetingPollWrapper::getTopic).orElse("Spotkanie");
                Instant meetingAt = pollOpt.map(p -> Instant.ofEpochSecond(p.getMeetingAt())).orElse(Instant.now());

                MessageEmbed updated = this.buildMeetingEmbed(
                    topic, meetingAt, yes, no, maybe, yesMentions, noMentions, maybeMentions
                );

                updater.editMessage(new MessageEditBuilder().setEmbeds(updated).build());
                updater.acknowledgeEphemeral("Głos zaktualizowany. Dzięki!");
            });
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
            .addField("✅ Będzie", String.valueOf(yes), true)
            .addField("❌ Nie będzie", String.valueOf(no), true)
            .addField("🤷 Nie wiem", String.valueOf(maybe), true)
            .addField("Lista obecnych", yesList, false)
            .addField("Lista nieobecnych", noList, false)
            .addField("Lista niezdecydowanych", maybeList, false)
            .setFooter(
                "Kliknij tę samą opcję ponownie, aby wycofać głos. Zmiana opcji wymaga najpierw wycofania obecnego głosu.")
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
        this.pollRepository.selectAllPolls().thenAccept(polls -> {
            for (MeetingPollWrapper poll : polls) {
                Instant meetingAt = Instant.ofEpochSecond(poll.getMeetingAt());
                if (now.isAfter(meetingAt.plus(Duration.ofDays(1)))) {
                    TextChannel channel = jda.getTextChannelById(poll.getChannelId());
                    if (channel != null) {
                        channel.deleteMessageById(poll.getMessageId()).queue(
                            success -> {},
                            failure -> {}
                        );
                    }
                    this.voteRepository.deleteByMessageId(poll.getMessageId());
                    this.pollRepository.deleteByMessageId(poll.getMessageId());
                }
            }
        });
    }

    public interface MeetingButtonUpdater {
        void editMessage(MessageEditData newData);
        void replyEphemeral(String content);
        void acknowledgeEphemeral(String content);
    }
}
