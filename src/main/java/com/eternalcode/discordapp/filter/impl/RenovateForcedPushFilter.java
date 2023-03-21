package com.eternalcode.discordapp.filter.impl;

import com.eternalcode.discordapp.filter.Filter;

public class RenovateForcedPushFilter implements Filter {

    @Override
    public boolean filter(String source) {
        return source.contains("renovate[bot]") && source.contains("force-pushed");
    }

}
