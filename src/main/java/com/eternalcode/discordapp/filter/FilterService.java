package com.eternalcode.discordapp.filter;

import java.util.List;

public class FilterService {

    private final List<Filter> filters;

    public FilterService(List<Filter> filters) {
        this.filters = filters;
    }

    public boolean filterSource(String source) {
        for (Filter filter : this.filters) {
            if (!filter.filter(source)) {
                return false;
            }
        }
        return true;
    }

}
