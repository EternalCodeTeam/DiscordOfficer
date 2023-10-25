package com.eternalcode.discordapp.leveling;

public final class LevelUtil {

    public LevelUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Calculates the points needed to achieve the next level.
     * <p>
     * This method is called when an ExperienceChangeEvent occurs. It updates the user's level based on their experience points
     * and sends a message to a designated TextChannel when the user levels up.
     * <p>
     * It calculates the next level using the formula: N = P * L^2 + P * L + P
     * where:
     * <p> - N represents the points needed to reach the next level,
     * <p> - P is the points needed for one level (provided by LevelConfig),
     * <p> - L is the current level.
     * <p>
     * Example level calculations:
     * <p> Level 1: N = 100 * 1^2 + 100 * 1 + 100 = 300
     * <p> Level 2: N = 100 * 2^2 + 100 * 2 + 100 = 700
     * <p> Level 3: N = 100 * 3^2 + 100 * 3 + 100 = 1200
     * <p> Level 4: N = 100 * 4^2 + 100 * 4 + 100 = 1700
     * <p> Level 5: N = 100 * 5^2 + 100 * 5 + 100 = 2300
     *
     * @param currentLevel Current level.
     * @param pointsNeededForOneLevel Points needed for one level.
     * @return The calculated points needed for the next level.
     */
    public static int calculatePointsForNextLevel(int currentLevel, double pointsNeededForOneLevel) {
        return (int) (pointsNeededForOneLevel * Math.pow(currentLevel + 1, 2)
            + pointsNeededForOneLevel * (currentLevel + 1)
            + pointsNeededForOneLevel);
    }
}
