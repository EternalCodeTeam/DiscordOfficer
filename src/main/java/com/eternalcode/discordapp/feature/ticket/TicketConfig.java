package com.eternalcode.discordapp.feature.ticket;

import com.eternalcode.discordapp.config.CdnConfig;
import java.io.File;
import java.time.Duration;
import java.util.List;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

@Contextual
public class TicketConfig implements CdnConfig {

    public static final TicketCategoryConfig SUPPORT_TICKET_CATEGORY = new TicketCategoryConfig(
        "SUPPORT",
        "🩺",
        "Support",
        "Need help? Open a support ticket!",
        true,
        1
    );

    public static final TicketCategoryConfig BUG_REPORT_CATEGORY = new TicketCategoryConfig(
        "BUG_REPORT",
        "🐛",
        "Bug Report",
        "Found a bug? Report it here!",
        true,
        1
    );

    public static final TicketCategoryConfig FEATURE_REQUEST_CATEGORY = new TicketCategoryConfig(
        "FEATURE_REQUEST",
        "💡",
        "Feature Request",
        "Have an idea for a new feature?",
        true,
        1
    );

    public static final TicketCategoryConfig GENERAL_TICKET_CATEGORY = new TicketCategoryConfig(
        "GENERAL",
        "💬",
        "General",
        "General questions and conversations",
        true,
        1
    );

    @Description("# Staff role ID (can manage tickets)")
    public long staffRoleId = 1144038849211793599L;

    @Description("# Category ID where ticket channels will be created")
    public long categoryId = 1144038885320572938L;

    @Description("# Maximum number of active tickets per user")
    public int maxTicketsPerUser = 3;

    @Description("# After what time inactive tickets should be automatically closed (in hours)")
    public Duration autoCloseDuration = Duration.ofDays(7);

    @Description("# Channel ID where transcripts will be sent")
    public long transcriptChannelId = 1144038937996824779L;

    @Description("# Message configuration")
    public MessageConfig messages = new MessageConfig();

    @Description("# Embed configuration")
    public EmbedConfig embeds = new EmbedConfig();

    @Description("# Default ticket categories")
    public List<TicketCategoryConfig> defaultCategories = List.of(
        SUPPORT_TICKET_CATEGORY,
        BUG_REPORT_CATEGORY,
        FEATURE_REQUEST_CATEGORY,
        GENERAL_TICKET_CATEGORY
    );

    public Duration getAutoCloseDuration() {
        return this.autoCloseDuration;
    }

    public List<TicketCategoryConfig> getEnabledCategories() {
        return defaultCategories.stream()
            .filter(category -> category.enabled)
            .toList();
    }

    public TicketCategoryConfig getCategoryById(String id) {
        return defaultCategories.stream()
            .filter(category -> category.id.equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "ticket.yml");
    }

    @Contextual
    public static class MessageConfig {
        @Description("# Message sent when creating a ticket")
        public String ticketCreated = "🎫 Your ticket has been created! Describe your problem and our team will help you.";

        @Description("# Message sent when user has too many tickets")
        public String tooManyTickets = "❌ You already have too many active tickets. Close one of the existing ones before creating a new one.";
    }

    @Contextual
    public static class EmbedConfig {
        @Description("# Embed color (hex)")
        public String color = "#ffffff";

        @Description("# Embed thumbnail URL")
        public String thumbnail = "";

        @Description("# Footer icon URL")
        public String footerIcon = "";

        @Description("# Footer text")
        public String footerText = "EternalCodeTeam Support Center";

        @Description("# Show timestamp")
        public boolean showTimestamp = true;
    }

    @Contextual
    public static class TicketPanelEmbedConfig {
        @Description("# Ticket panel embed title")
        public String title = "EternalCode.pl - Support Center";

        @Description("# Ticket panel embed description")
        public String description = "Select a category to create a ticket. Our team will help you resolve your issue!";

        @Description("# Panel embed color (hex)")
        public String color = "#ffffff";

        @Description("# Banner URL (image at the top of the embed)")
        public String bannerUrl = "https://raw.githubusercontent.com/EternalCodeTeam/.github/refs/heads/master/assets/Ticket%20Channel.png";

        @Description("# Panel thumbnail URL")
        public String thumbnail = "";

        @Description("# Panel footer icon URL")
        public String footerIcon = "";

        @Description("# Panel footer text")
        public String footerText = "EternalCodeTeam Support Center";

        @Description("# Show timestamp in panel")
        public boolean showTimestamp = true;
    }

    @Description("# Ticket panel embed configuration")
    public TicketPanelEmbedConfig panelEmbed = new TicketPanelEmbedConfig();

    @Contextual
    public static class TicketCategoryConfig {
        @Description("# Category ID (unique identifier)")
        public String id = "TICKET";

        @Description("# Category emoji")
        public String emoji = "🎫";

        @Description("# Display name")
        public String displayName = "Ticket";

        @Description("# Category description")
        public String description = "Open a ticket in this category";

        @Description("# Whether the category is active")
        public boolean enabled = true;

        @Description("# Maximum number of tickets in this category per user")
        public int maxPerUser = 1;

        public TicketCategoryConfig() {}

        public TicketCategoryConfig(
            String id,
            String emoji,
            String displayName,
            String description,
            boolean enabled,
            int maxPerUser
        ) {
            this.id = id;
            this.emoji = emoji;
            this.displayName = displayName;
            this.description = description;
            this.enabled = enabled;
            this.maxPerUser = maxPerUser;
        }

        public String getButtonId() {
            return "ticket_" + id.toLowerCase();
        }
    }
}
