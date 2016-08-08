package be.kuleuven.cs.gametheory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Value class for game results.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
@AutoValue
public abstract class GameResult {

    /**
     * @return the description of theses result in String key-value pairs.
     */
    public abstract ImmutableMap<String, String> getDescription();

    /**
     * @return the results of this game as a list of values.
     */
    public abstract ImmutableList<Double> getResults();

    /**
     * Static factory method.
     *
     * @param results
     *            The result value to construct this result object from.
     * @return A GameResult object with no description and with the specified
     *         results.
     */
    public static GameResult create(final List<Double> results) {
        return new AutoValue_GameResult(
                ImmutableMap.copyOf(new LinkedHashMap<>()),
                ImmutableList.copyOf(results));
    }

    /**
     * Creates a new result object from a specific result object but with the
     * addition of a new description key-pair.
     *
     * @param key
     *            The description key.
     * @param val
     *            the description value.
     * @return a new game result.
     */
    public GameResult withDescription(final String key, final String val) {
        final Map<String, String> newMap = Maps.newLinkedHashMap();
        newMap.putAll(getDescription());
        newMap.put(key, val);
        return create(newMap, getResults());
    }

    private static GameResult create(final Map<String, String> description,
            final List<Double> results) {
        return new AutoValue_GameResult(ImmutableMap.copyOf(description),
                ImmutableList.copyOf(results));
    }
}
