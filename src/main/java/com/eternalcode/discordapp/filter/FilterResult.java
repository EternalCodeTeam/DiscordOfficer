package com.eternalcode.discordapp.filter;

public final class FilterResult {

    private static final FilterResult PASSED = new FilterResult(true);
    private static final FilterResult NOT_PASSED = new FilterResult(false);

    private final boolean isPassed;

    private FilterResult(boolean isPassed) {
        this.isPassed = isPassed;
    }

    public static FilterResult passed() {
        return PASSED;
    }

    public static FilterResult notPassed() {
        return NOT_PASSED;
    }

    public boolean isPassed() {
        return this.isPassed;
    }

}
