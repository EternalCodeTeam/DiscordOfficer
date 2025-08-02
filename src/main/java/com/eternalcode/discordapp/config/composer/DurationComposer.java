package com.eternalcode.discordapp.config.composer;

import panda.std.Result;

import java.time.Duration;

public class DurationComposer implements SimpleComposer<Duration> {

    @Override
    public Result<Duration, Exception> deserialize(String value) {
        return Result.supplyThrowing(() -> Duration.parse(value));
    }

    @Override
    public Result<String, Exception> serialize(Duration value) {
        return Result.ok(value.toString());
    }
} 