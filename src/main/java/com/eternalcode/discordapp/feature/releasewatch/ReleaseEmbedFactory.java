package com.eternalcode.discordapp.feature.releasewatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public final class ReleaseEmbedFactory {

    private static final int COMPONENT_V2_TEXT_LIMIT = 4000;
    private static final int BUTTON_LABEL_LIMIT = 80;
    private static final int MAX_BUTTONS = 10;
    private static final int TEXT_SAFETY_MARGIN = 128;

    private static final String RELEASE_ICON_URL =
        "https://github.com/EternalCodeTeam.png";
    private static final String THUMBNAIL_DESCRIPTION = "Release";

    private static final String HEADER_TITLE_FORMAT = "# %s `%s`";
    private static final String HEADER_METADATA_FORMAT = "-# **%s** • <t:%d:R>";
    private static final String FOOTER_FORMAT = "-# EternalCodeTeam • Published <t:%d:f>";

    private static final String GITHUB_BUTTON_LABEL = "GitHub";
    private static final String DEFAULT_LINK_LABEL = "Open link";

    private static final String TRUNCATION_MARKER = "\n…";
    private static final String CODE_FENCE_MARKER = "```";

    private static final Pattern MARKDOWN_IMAGE =
        Pattern.compile("!\\[[^\\]]*]\\(([^)\\s]+)[^)]*\\)");
    public static final String NO_CHANGELOG_PROVIDED = "*No changelog provided.*";

    private ReleaseEmbedFactory() {
    }

    public static MessageCreateData create(
        GitHubRepositoryRef repository,
        String displayName,
        GitHubRelease release,
        List<ResolvedLink> links
    ) {
        BodyContent bodyContent = extractBanner(release.body());
        LinkButtons linkButtons = buildLinkButtons(repository, release, links);

        long publishedAt = release.publishedAt().getEpochSecond();

        String headerTitle = HEADER_TITLE_FORMAT.formatted(
            displayName,
            release.tagName()
        );

        String headerMetadata = HEADER_METADATA_FORMAT.formatted(
            repository.toSlug(),
            publishedAt
        );

        String footer = FOOTER_FORMAT.formatted(
            publishedAt
        );

        int changelogLimit = calculateChangelogLimit(
            headerTitle,
            headerMetadata,
            footer,
            linkButtons.labelLength()
        );

        String changelog = truncateMarkdown(
            bodyContent.text(),
            changelogLimit
        );

        List<ContainerChildComponent> components = new ArrayList<>();

        components.add(
            buildHeaderSection(
                headerTitle,
                headerMetadata
            )
        );

        components.add(
            Separator.createDivider(Separator.Spacing.LARGE)
        );

        if (bodyContent.bannerUrl() != null) {
            components.add(
                MediaGallery.of(
                    MediaGalleryItem.fromUrl(bodyContent.bannerUrl())
                )
            );

            components.add(
                Separator.createInvisible(Separator.Spacing.SMALL)
            );
        }

        components.add(
            TextDisplay.of(
                changelog
            )
        );

        components.add(
            Separator.createDivider(Separator.Spacing.SMALL)
        );

        components.addAll(
            ActionRow.partitionOf(linkButtons.buttons())
        );

        components.add(
            Separator.createInvisible(Separator.Spacing.SMALL)
        );

        components.add(
            TextDisplay.of(footer)
        );

        Container container = Container.of(components);

        return new MessageCreateBuilder()
            .useComponentsV2()
            .setComponents(container)
            .build();
    }

    private static Section buildHeaderSection(
        String title,
        String metadata
    ) {
        return Section.of(
            Thumbnail.fromUrl(RELEASE_ICON_URL)
                .withDescription(THUMBNAIL_DESCRIPTION),
            TextDisplay.of(title),
            TextDisplay.of(metadata)
        );
    }

    private static LinkButtons buildLinkButtons(
        GitHubRepositoryRef repository,
        GitHubRelease release,
        List<ResolvedLink> links
    ) {
        String githubUrl = release.htmlUrl() == null || release.htmlUrl().isBlank()
            ? repository.toHtmlUrl()
            : release.htmlUrl();

        List<Button> buttons = new ArrayList<>();

        buttons.add(Button.link(githubUrl, GITHUB_BUTTON_LABEL));

        int labelLength = GITHUB_BUTTON_LABEL.length();

        if (links != null) {
            for (ResolvedLink link : links) {
                if (buttons.size() >= MAX_BUTTONS) {
                    break;
                }

                if (link.url() == null || link.url().isBlank()) {
                    continue;
                }

                String label = formatButtonLabel(link.label());

                buttons.add(
                    Button.link(link.url(), label)
                );

                labelLength += label.length();
            }
        }

        return new LinkButtons(buttons, labelLength);
    }

    private static String formatButtonLabel(String label) {
        String normalized = label == null || label.isBlank()
            ? DEFAULT_LINK_LABEL
            : label.trim();

        int maximumLabelLength = BUTTON_LABEL_LIMIT;

        if (normalized.length() > maximumLabelLength) {
            normalized = normalized
                .substring(0, maximumLabelLength)
                .stripTrailing();
        }

        return normalized;
    }

    private static int calculateChangelogLimit(
        String header,
        String metadata,
        String footer,
        int buttonLabelLength
    ) {
        int reservedCharacters =
            header.length()
                + metadata.length()
                + footer.length()
                + buttonLabelLength
                + TEXT_SAFETY_MARGIN;

        return Math.max(
            256,
            COMPONENT_V2_TEXT_LIMIT - reservedCharacters
        );
    }

    private static BodyContent extractBanner(String body) {
        if (body == null || body.isBlank()) {
            return new BodyContent(
                null,
                NO_CHANGELOG_PROVIDED
            );
        }

        Matcher matcher = MARKDOWN_IMAGE.matcher(body);

        if (!matcher.find()) {
            return new BodyContent(
                null,
                body.trim()
            );
        }

        String bannerUrl = matcher.group(1);

        String remainingText = (
            body.substring(0, matcher.start())
                + body.substring(matcher.end())
        ).trim();

        return new BodyContent(
            bannerUrl,
            remainingText.isBlank()
                ? NO_CHANGELOG_PROVIDED
                : remainingText
        );
    }

    private static String truncateMarkdown(
        String body,
        int limit
    ) {
        if (body == null || body.isBlank()) {
            return NO_CHANGELOG_PROVIDED;
        }

        String normalized = body.trim();

        if (normalized.length() <= limit) {
            return normalized;
        }

        int maximumContentLength = Math.max(
            1,
            limit - 8
        );

        int cutIndex = normalized.lastIndexOf(
            '\n',
            maximumContentLength
        );

        if (cutIndex < maximumContentLength * 0.75) {
            cutIndex = maximumContentLength;
        }

        String truncated = normalized
            .substring(0, cutIndex)
            .stripTrailing();

        truncated += TRUNCATION_MARKER;

        if (countOccurrences(truncated, CODE_FENCE_MARKER) % 2 != 0) {
            truncated += "\n" + CODE_FENCE_MARKER;
        }

        return truncated;
    }

    private static int countOccurrences(
        String input,
        String sequence
    ) {
        int count = 0;
        int index = 0;

        while ((index = input.indexOf(sequence, index)) != -1) {
            count++;
            index += sequence.length();
        }

        return count;
    }

    private record BodyContent(
        String bannerUrl,
        String text
    ) {
    }

    private record LinkButtons(
        List<Button> buttons,
        int labelLength
    ) {
    }
}
