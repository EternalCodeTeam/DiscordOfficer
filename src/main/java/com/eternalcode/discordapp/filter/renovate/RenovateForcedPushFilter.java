package com.eternalcode.discordapp.filter.renovate;

import com.eternalcode.discordapp.filter.Filter;
import com.eternalcode.discordapp.filter.FilterResult;

import java.util.Set;

public class RenovateForcedPushFilter implements Filter {

    private static final Set<String> BLOCKED_PHRASES = Set.of(
        "renovate[bot]",
        "force-pushed"
    );

    @Override
    public FilterResult filter(String... sources) {
        for (String source : sources) {
            for (String blocked : BLOCKED_PHRASES) {
                if (source.contains(blocked)) {
                    return FilterResult.notPassed();
                }
            }
        }
        return FilterResult.passed();
    }
}
