package be.kuleuven.cs.gametheory;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class GameInstanceParams implements Serializable {
    public abstract GameInstanceConfiguration getGameInstanceConfiguration();

    public abstract long getSeed();

    public static GameInstanceParams create(GameInstanceConfiguration conf, long seed) {
        return new AutoValue_GameInstanceParams(conf, seed);
    }
}
