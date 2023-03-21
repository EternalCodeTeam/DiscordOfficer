package com.eternalcode.discordapp.filter.renovate;

import com.eternalcode.discordapp.filter.Filter;
import com.eternalcode.discordapp.filter.FilterResult;

import java.util.HashSet;
import java.util.Set;

public class RenovateForcedPushFilter implements Filter {

    private static final Set<String> WORDS_TO_NOT_PASS = Set.of(
            "renovate[bot]",
            "force-pushed"
    );

    @Override
    public FilterResult filter(String... sources) {
        Set<String> toNotPassed = new HashSet<>(WORDS_TO_NOT_PASS);

        for (String source : sources) {
            for (String word : WORDS_TO_NOT_PASS) {
                if (source.contains(word)) {
                    toNotPassed.remove(word);
                }
            }
        }

        return toNotPassed.isEmpty() ? FilterResult.notPassed() : FilterResult.passed();
    }

}
