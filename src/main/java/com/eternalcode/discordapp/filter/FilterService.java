package com.eternalcode.discordapp.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterService {

    private final List<Filter> filters = new ArrayList<>();

    public FilterService register(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    public FilterResult check(String... sources) {
        return this.filters.stream()
            .map(filter -> filter.filter(sources))
            .filter(result -> !result.isPassed())
            .findFirst()
            .orElse(FilterResult.passed());
    }
}
