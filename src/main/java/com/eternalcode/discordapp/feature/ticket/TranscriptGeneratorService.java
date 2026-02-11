package com.eternalcode.discordapp.feature.ticket;

import dev.skywolfxp.transcript.Transcript;
import java.io.File;
import java.io.IOException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscriptGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptGeneratorService.class);

    private final JDA jda;
    private final TicketConfig config;

    public TranscriptGeneratorService(JDA jda, TicketConfig config) {
        this.jda = jda;
        this.config = config;
    }

    public void generateAndSendTranscript(TicketWrapper ticket, long staffId, String reason) {
        if (this.config.transcriptChannelId == 0L) {
            return;
        }

        MessageChannel transcriptChannel =
            this.jda.getChannelById(MessageChannel.class, this.config.transcriptChannelId);
        TextChannel ticketChannel = this.jda.getTextChannelById(ticket.getChannelId());

        if (transcriptChannel == null || ticketChannel == null) {
            LOGGER.warn(
                "Cannot generate transcript: channel not found (transcript: {}, ticket: {})",
                transcriptChannel == null, ticketChannel == null);
            return;
        }

        try {
            File transcriptFile = this.generateTranscriptFile(ticketChannel, ticket.getId());
            String message = this.createTranscriptMessage(ticket.getId(), staffId, reason);

            transcriptChannel.sendMessage(message)
                .addFiles(FileUpload.fromData(transcriptFile))
                .queue(
                    success -> this.deleteFile(transcriptFile),
                    failure -> this.deleteFile(transcriptFile));
        }
        catch (Exception exception) {
            LOGGER.error("Failed to generate transcript for ticket #{}", ticket.getId(), exception);
        }
    }

    private File generateTranscriptFile(TextChannel channel, long ticketId) throws IOException {
        Transcript transcript = new Transcript();
        transcript.render(channel);

        File file = File.createTempFile("transcript-" + ticketId, ".html");
        transcript.writeToFile(file);

        return file;
    }

    private String createTranscriptMessage(long ticketId, long staffId, String reason) {
        return String.format(
            "📄 **Transcript Ticket #%d**%n> Closed by: %s%n> Reason: %s",
            ticketId,
            staffId == 0L ? "Automatic" : "<@" + staffId + ">",
            reason);
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                LOGGER.warn("Failed to delete transcript file: {}", file.getAbsolutePath());
            }
        }
    }
}
