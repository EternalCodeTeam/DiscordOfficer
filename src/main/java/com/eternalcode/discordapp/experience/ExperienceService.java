package com.eternalcode.discordapp.experience;

public class ExperienceService {
    private ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    public void addPoints(long userId, double points) {
        this.experienceRepository.find(userId).whenComplete((experience, expierienceThrowable) -> {
            if (expierienceThrowable != null) {
                expierienceThrowable.printStackTrace();
                return;
            }

            experience.addPoints(points);

            this.experienceRepository.saveExperience(experience).whenComplete((saveExperience, saveExperienceThrowable) -> {
                if (saveExperienceThrowable != null) {
                    saveExperienceThrowable.printStackTrace();
                }
            });
        });
    }

    public void removePoints(long userId, double points) {
        this.experienceRepository.find(userId).whenComplete((experience, expierienceThrowable) -> {
            if (expierienceThrowable != null) {
                expierienceThrowable.printStackTrace();
                return;
            }

            experience.removePoints(points);

            this.experienceRepository.saveExperience(experience).whenComplete((saveExperience, saveExperienceThrowable) -> {
                if (saveExperienceThrowable != null) {
                    saveExperienceThrowable.printStackTrace();
                }
            });
        });
    }
}