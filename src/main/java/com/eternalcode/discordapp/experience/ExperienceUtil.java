package com.eternalcode.discordapp.experience;

public class ExperienceUtil {

    public static void addPoints(ExperienceRepository experienceRepository, long userId, double points)
    {
        experienceRepository.find(userId).whenComplete((experience, expierienceThrowable) -> {
            if (expierienceThrowable != null) {
                expierienceThrowable.printStackTrace();
                return;
            }

            experience.addPoints(points);

            experienceRepository.saveExperience(experience).whenComplete((saveExperience, saveExperienceThrowable) -> {
                if (saveExperienceThrowable != null) {
                    saveExperienceThrowable.printStackTrace();
                }
            });
        });
    }
}