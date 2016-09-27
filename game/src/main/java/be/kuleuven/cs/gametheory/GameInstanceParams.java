package be.kuleuven.cs.gametheory;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class GameInstanceParams {
    public abstract GameInstanceConfiguration getGameInstanceConfiguration();

    public abstract long getSeed();
}
