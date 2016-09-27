package be.kuleuven.cs.gametheory;

import com.google.auto.value.AutoValue;

import java.util.Map;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
@AutoValue
public abstract class GameInstanceResult {

    public abstract GameInstanceConfiguration getGameInstanceConfig();

    public abstract Map<Integer, Long> getPayoffs();

    public static GameInstanceResult create(GameInstanceConfiguration config, Map<Integer, Long> po) {
        return new AutoValue_GameInstanceResult(config, po);
    }
}
