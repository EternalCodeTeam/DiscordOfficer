package com.eternalcode.discordapp.feature.leveling.experience;

import java.util.function.LongSupplier;

public record ExperienceChangeEvent(Experience experience, LongSupplier channelId) {
}
