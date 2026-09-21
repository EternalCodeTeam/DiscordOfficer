package com.eternalcode.discordapp.feature.releasewatch;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import panda.std.Result;

public final class GitHubRepositoryRef {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)$");
    private static final String API_RELEASES_LATEST_URL = "https://api.github.com/repos/%s/%s/releases/latest";
    private static final String API_RELEASES_BY_TAG_URL = "https://api.github.com/repos/%s/%s/releases/tags/%s";
    private static final String API_REPOSITORY_URL = "https://api.github.com/repos/%s/%s";
    private static final String HTML_URL = "https://github.com/%s/%s";

    private final String owner;
    private final String repository;

    public GitHubRepositoryRef(String owner, String repository) {
        this.owner = owner;
        this.repository = repository;
    }

    public static Result<GitHubRepositoryRef, IllegalArgumentException> fromSlug(String slug) {
        if (slug == null) {
            return Result.error(new IllegalArgumentException("Repository slug cannot be null"));
        }

        Matcher matcher = SLUG_PATTERN.matcher(slug.trim());
        if (!matcher.matches()) {
            return Result.error(new IllegalArgumentException("Expected format owner/repo, got: " + slug));
        }

        return Result.ok(new GitHubRepositoryRef(matcher.group(1), matcher.group(2)));
    }

    public String getOwner() {
        return this.owner;
    }

    public String getRepository() {
        return this.repository;
    }

    public String toSlug() {
        return this.owner + "/" + this.repository;
    }

    public String toHtmlUrl() {
        return String.format(HTML_URL, this.owner, this.repository);
    }

    public String toApiRepositoryUrl() {
        return String.format(API_REPOSITORY_URL, this.owner, this.repository);
    }

    public String toApiLatestReleaseUrl() {
        return String.format(API_RELEASES_LATEST_URL, this.owner, this.repository);
    }

    public String toApiReleaseByTagUrl(String tag) {
        return String.format(API_RELEASES_BY_TAG_URL, this.owner, this.repository, tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GitHubRepositoryRef that)) {
            return false;
        }
        return Objects.equals(this.owner, that.owner) && Objects.equals(this.repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owner, this.repository);
    }

    @Override
    public String toString() {
        return this.toSlug();
    }
}
