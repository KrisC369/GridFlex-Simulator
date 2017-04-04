package be.kuleuven.cs.gametheory.stats;

/**
 * Different confidence interval levels.
 */
public enum ConfidenceLevel {
    _90pc(1.645d, 0.90d),
    _95pc(1.96, 0.95d),
    _99pc(2.575d, 0.99);

    private double z_aby2;
    private double level;

    private ConfidenceLevel(double z_aby2, double level) {
        this.z_aby2 = z_aby2;
        this.level = level;
    }

    /**
     * @return The coeffecient pertaining to this level.
     */
    public double getConfideneCoeff() {
        return this.z_aby2;
    }

    /**
     * @return The level as a double value.
     */
    public double getConfidenceLevel() {
        return level;
    }
}
