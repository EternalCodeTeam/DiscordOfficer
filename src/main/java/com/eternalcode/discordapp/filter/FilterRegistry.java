package com.eternalcode.discordapp.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterRegistry {

    private final List<Filter> filters;

    public FilterRegistry() {
        this.filters = new ArrayList<>();
    }

    public FilterRegistry register(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    public FilterService build() {
        return new FilterService(this.filters);
    }

}
