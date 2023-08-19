package com.eternalcode.discordapp.config.composer;

import panda.std.Result;

import java.time.Instant;

public class InstantComposer implements SimpleComposer<Instant> {

    @Override
    public Result<Instant, Exception> deserialize(String value) {
        return Result.supplyThrowing(() -> Instant.parse(value));
    }

    @Override
    public Result<String, Exception> serialize(Instant value) {
        return Result.ok(value.toString());
    }
}
