package com.eternalcode.discordapp.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterService {

    private final List<Filter> filters = new ArrayList<>();

    public FilterService registerFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    public FilterResult check(String... sources) {
        for (Filter filter : this.filters) {
            FilterResult result = filter.filter(sources);

            if (!result.isPassed()) {
                return FilterResult.notPassed();
            }
        }

        return FilterResult.passed();
    }

}
