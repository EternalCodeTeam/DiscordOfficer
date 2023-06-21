package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.experience.Experience;
import com.eternalcode.discordapp.experience.ExperienceChangeEvent;
import com.eternalcode.discordapp.observer.Observer;

public class LevelController implements Observer<ExperienceChangeEvent> {

    @Override
    public void update(ExperienceChangeEvent experienceChangeEvent) {
        Experience experience = experienceChangeEvent.experience();

        // TODO: Implement leveling system
    }

}
